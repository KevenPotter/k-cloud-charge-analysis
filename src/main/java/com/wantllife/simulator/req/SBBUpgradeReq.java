package com.wantllife.simulator.req;

import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 远程更新 [0X94]
 *
 * @author KevenPotter
 * @date 2026-06-05 14:11:20
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SBBUpgradeReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*设备类型(1.直流 2.交流)*/
    private Integer deviceType;
    /*设备功率(1.直流:30KW、60KW、120KW、160KW、240KW、480KW 2.交流:7、14KW、21KW、42KW)*/
    private Integer devicePower;
    /*升级服务器地址(公网IP:如:121.199.192.223)*/
    private String address;
    /*升级服务器端口(如:8768)*/
    private Integer port;
    /*用户名*/
    private String username;
    /*密码*/
    private String password;
    /*文件路径(如:UPGRADE-7KW/20260429)*/
    private String filePath;
    /*执行控制(1.立即执行 2.空闲执行)*/
    private Integer execMethod;
    /*下载超时时间(分钟)*/
    private Integer timeout;

    /* 有参构造 */
    public SBBUpgradeReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        if (CloudChargeHolder.isSimulatorLogOutput()) log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-06-05 14:11:45
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 设备类型 [1字节] [BIN]
        this.setDeviceType(data[index] & 0xFF);
        index += 1;
        // 设备功率 [2字节] [BIN]
        this.setDevicePower((data[index] & 0xFF) | ((data[index + 1] & 0xFF) << 8));
        index += 2;
        // 升级服务器地址 [16字节] [ASCII]
        this.setAddress(new String(data, index, 16, StandardCharsets.US_ASCII).trim());
        index += 16;
        // 升级服务器端口 [2字节] [BIN]
        this.setPort((data[index] & 0xFF) | ((data[index + 1] & 0xFF) << 8));
        index += 2;
        // 用户名 [16字节] [ASCII]
        this.setUsername(new String(data, index, 16, StandardCharsets.US_ASCII).trim());
        index += 16;
        // 密码 [16字节] [ASCII]
        this.setPassword(new String(data, index, 16, StandardCharsets.US_ASCII).trim());
        index += 16;
        // 文件路径 [32字节] [ASCII]
        this.setFilePath(new String(data, index, 32, StandardCharsets.US_ASCII).trim());
        index += 32;
        // 执行控制 [1字节] [BIN]
        this.setExecMethod(data[index] & 0xFF);
        index += 1;
        // 下载超时时间 [1字节] [BIN]
        this.setTimeout(data[index] & 0xFF);
        index += 1;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-05 14:12:23
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇑ 【0x94】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 远程更新操作  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 远程更新操作  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 远程更新操作  设备类型    deviceType                   : %s\n", devLabel, deviceType == 1 ? "直流" : "交流"));
        sb.append(String.format("👩‍🚀%s 远程更新操作  设备功率    devicePower                  : %s\n", devLabel, devicePower));
        sb.append(String.format("👩‍🚀%s 远程更新操作  升级地址    address                      : %s\n", devLabel, address));
        sb.append(String.format("👩‍🚀%s 远程更新操作  升级端口    port                         : %s\n", devLabel, port));
        sb.append(String.format("👩‍🚀%s 远程更新操作  用户名称    username                     : %s\n", devLabel, username));
        sb.append(String.format("👩‍🚀%s 远程更新操作  输入密码    password                     : %s\n", devLabel, password));
        sb.append(String.format("👩‍🚀%s 远程更新操作  文件路径    filePath                     : %s\n", devLabel, filePath));
        sb.append(String.format("👩‍🚀%s 远程更新操作  执行控制    execMethod                   : %s\n", devLabel, execMethod == 1 ? "立即执行" : "空闲执行"));
        sb.append(String.format("👩‍🚀%s 远程更新操作  超时时间    timeout                      : %s\n", devLabel, timeout));
        log.info(sb.toString());
    }
}
