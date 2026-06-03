package com.wantllife.simulator.client;

import cn.hutool.core.thread.ThreadUtil;
import com.wantllife.constant.SimulatorConstants;
import com.wantllife.domain.vo.StandardDevice;
import com.wantllife.simulator.manager.TcpConnectionManager;
import com.wantllife.simulator.process.SimDevMsgProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_PROJECT_NAME;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_TIP_ICON;
import static com.wantllife.constant.SimulatorConstants.RECONNECT_INTERVAL_SECOND;

/**
 * 单设备TCP客户端
 * 负责单个充电桩的TCP连接、消息收发、自动重连、心跳维护
 *
 * @author KevenPotter
 * @date 2026-05-26 13:41:51
 */
@Slf4j
public class TcpClient {

    /*设备编号*/
    @Getter
    private final String deviceId;
    /*设备对象*/
    @Getter
    private final StandardDevice device;

    /*TCP服务器IP地址*/
    private final String serverIP;
    /*TCP服务器端口号*/
    private final int serverPort;
    /*消息处理器*/
    private final SimDevMsgProcessor simDevMsgProcessor;
    /*TCP套接字*/
    private Socket socket;
    /*输出流*/
    private OutputStream outputStream;
    /*运行状态*/
    private volatile boolean running = false;

    /**
     * 构造函数:初始化TCP客户端
     *
     * @param serverIP   服务器IP
     * @param serverPort 服务器端口
     * @param device     设备对象
     * @author KevenPotter
     * @date 2026-05-26 15:58:30
     */
    public TcpClient(String serverIP, int serverPort, StandardDevice device) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.device = device;
        this.deviceId = device.getDeviceId();
        this.simDevMsgProcessor = new SimDevMsgProcessor();
    }

    /**
     * 启动TCP客户端
     * 初始化运行状态,注册到连接管理器,并启动连接线程和接收线程
     *
     * @author KevenPotter
     * @date 2026-05-26 15:59:20
     */
    public void start() {
        if (running) return;
        running = true;
        TcpConnectionManager.register(deviceId, this);
        ThreadUtil.execAsync(this::doConnect);
        ThreadUtil.execAsync(this::receiveMessage);
    }

    /**
     * 建立TCP连接(自带自动重连)
     * 循环尝试连接服务器,连接成功后发送登录帧
     *
     * @author KevenPotter
     * @date 2026-05-26 15:59:50
     */
    private void doConnect() {
        // 持续循环,保证断线自动重连
        while (running) {
            try {
                // 如果连接已存在且有效,直接等待
                if (socket != null && !socket.isClosed() && socket.isConnected()) {
                    TimeUnit.SECONDS.sleep(1);
                    continue;
                }
                // 开始建立TCP连接
                log.info("{} {} {} try to connect → {}:{}", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, serverIP, serverPort);
                socket = new Socket(serverIP, serverPort);
                socket.setSoTimeout(SimulatorConstants.SO_TIMEOUT_MILLISECOND);

                // 获取输出流并交给处理器,由处理器全权负责报文发送
                OutputStream outputStream = socket.getOutputStream();
                simDevMsgProcessor.bindOutputStream(device, outputStream, this);

                // 连接成功,通知处理器执行登录逻辑
                log.info("{} {} {} TCP connection successful", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId);
                simDevMsgProcessor.onConnected(deviceId);

            } catch (Exception e) {
                // 连接失败,按配置间隔重试
                if (!running) break;
                log.error("{} {} {} connection failed, try again in {} seconds", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId, RECONNECT_INTERVAL_SECOND);
                try {
                    TimeUnit.SECONDS.sleep(RECONNECT_INTERVAL_SECOND);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    /**
     * 接收服务器下行消息
     * 持续监听Socket输入流,接收数据后交给消息处理器处理,并自动回复上行报文
     *
     * @author KevenPotter
     * @date 2026-05-26 16:01:04
     */
    private void receiveMessage() {
        // 持续循环接收服务器下行数据
        while (running) {
            try {
                // 连接未就绪,等待后重试
                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    continue;
                }
                // 获取输入流,读取服务器下行报文
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[2048];
                int len;
                try {
                    len = in.read(buffer);
                } catch (SocketTimeoutException e) {
                    // 读超时,继续循环
                    continue;
                }
                // 连接断开,执行重连
                if (len <= 0) {
                    reconnect();
                    continue;
                }
                // 复制读取到的有效报文数据
                byte[] data = new byte[len];
                System.arraycopy(buffer, 0, data, 0, len);
                // 将原始下行报文交给处理器,不做任何业务处理
                simDevMsgProcessor.process(data);
            } catch (Exception e) {
                // 接收异常,执行重连
                if (running) {
                    log.error("{} {} {} receive signal exception, preparing to reconnect", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId);
                    reconnect();
                }
            }
        }
    }

    /**
     * 执行重连逻辑
     * 关闭当前Socket,重新启动连接线程
     *
     * @author KevenPotter
     * @date 2026-05-26 16:03:40
     */
    private void reconnect() {
        closeSocket();
        if (running) {
            ThreadUtil.execAsync(this::doConnect);
        }
    }

    /**
     * 关闭Socket连接
     * 安全关闭,忽略异常
     *
     * @author KevenPotter
     * @date 2026-05-26 16:04:29
     */
    private void closeSocket() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 停止TCP客户端
     * 修改运行状态,关闭连接,从管理器注销
     *
     * @author KevenPotter
     * @date 2026-05-26 16:04:58
     */
    public void stop() {
        running = false;
        closeSocket();
        TcpConnectionManager.remove(deviceId, this);
        log.info("{} {} {} stopped", SIM_TIP_ICON, SIM_PROJECT_NAME, deviceId);
    }
}
