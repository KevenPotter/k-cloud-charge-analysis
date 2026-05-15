package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_UPGRADE;


/**
 * 远程更新 [0X94]
 *
 * @author KevenPotter
 * @date 2026-04-29 10:43:07
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BBUpgradeRes extends FrameHeader {

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

    /**
     * 构建下发指令
     *
     * @param deviceId   设备编号
     * @param execMethod 执行控制(1.立即执行 2.空闲执行)
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-29 10:44:25
     */
    public static byte[] buildCommand(
            String deviceId, Integer deviceType, Integer devicePower,
            String address, Integer port, String username, String password,
            String filePath, Integer execMethod, Integer timeout
    ) {
        BBUpgradeRes res = new BBUpgradeRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_UPGRADE);
        res.setDeviceId(deviceId).setDeviceType(deviceType).setDevicePower(devicePower);
        res.setAddress(address).setPort(port).setUsername(username).setPassword(password);
        res.setFilePath(filePath).setExecMethod(execMethod).setTimeout(timeout);

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body);

        // 记录日志
        res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-29 10:44:54
     */
    private byte[] buildBody() {
        byte[] body = new byte[94];
        // 设备编号 [7字节] [BCD]
        byte[] devBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(devBcd, 0, body, 0, 7);
        // 桩型号 [1字节] [BIN]
        body[7] = (byte) (this.deviceType & 0xFF);
        // 桩功率 [2字节] [BIN]
        int power = this.devicePower;
        body[8] = (byte) (power & 0xFF);
        body[9] = (byte) ((power >> 8) & 0xFF);
        // 升级服务器地址 [16字节] [ASCII]
        byte[] addrBytes = this.address.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(addrBytes, 0, body, 10, Math.min(addrBytes.length, 16));
        // 服务器端口 [2字节] [BIN]
        int portVal = this.port;
        body[26] = (byte) (portVal & 0xFF);
        body[27] = (byte) ((portVal >> 8) & 0xFF);
        // 用户名 [16字节] [ASCII]
        byte[] userBytes = this.username.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(userBytes, 0, body, 28, Math.min(userBytes.length, 16));
        // 密码 [16字节] [ASCII]
        byte[] pwdBytes = this.password.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(pwdBytes, 0, body, 44, Math.min(pwdBytes.length, 16));
        // 文件路径 [32字节] [ASCII]
        byte[] pathBytes = this.filePath.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(pathBytes, 0, body, 60, Math.min(pathBytes.length, 32));
        // 执行控制 [1字节] [BIN]
        body[92] = (byte) (this.execMethod & 0xFF);
        // 下载超时时间 [1字节] [BIN]
        body[93] = (byte) (this.timeout & 0xFF);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 16:30:50
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x94】 {} 远程更新 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x94】 {} 远程更新 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x94】 {} 远程更新 设备类型    deviceType           : {}", deviceId, deviceType == 1 ? "直流" : "交流");
        log.info("🔶 【0x94】 {} 远程更新 设备功率    devicePower          : {}", deviceId, devicePower);
        log.info("🔶 【0x94】 {} 远程更新 升级地址    address              : {}", deviceId, address);
        log.info("🔶 【0x94】 {} 远程更新 升级端口    port                 : {}", deviceId, port);
        log.info("🔶 【0x94】 {} 远程更新 用户名称    username             : {}", deviceId, username);
        log.info("🔶 【0x94】 {} 远程更新 输入密码    password             : {}", deviceId, password);
        log.info("🔶 【0x94】 {} 远程更新 文件路径    filePath             : {}", deviceId, filePath);
        log.info("🔶 【0x94】 {} 远程更新 执行控制    execMethod           : {}", deviceId, execMethod == 1 ? "立即执行" : "空闲执行");
        log.info("🔶 【0x94】 {} 远程更新 超时时间    timeout              : {}", deviceId, timeout);
        System.out.println();
    }

}
