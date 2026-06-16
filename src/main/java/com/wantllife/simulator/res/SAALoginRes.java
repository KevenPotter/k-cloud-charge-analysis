package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardDevice;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_LOGIN;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电桩登录认证 [0X01]
 *
 * @author KevenPotter
 * @date 2026-05-26 16:48:51
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAALoginRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*设备类型*/
    private Integer deviceType;
    /*充电枪数量*/
    private Integer gunNum;
    /*通信协议版本*/
    private String protocolVersion;
    /*程序版本*/
    private String programVersion;
    /*网络连接类型*/
    private Integer networkLinkType;
    /*sim卡*/
    private String simNo;
    /*运营商*/
    private Integer carrier;

    /**
     * 构建下发指令
     *
     * @param device 设备
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-05-26 16:50:32
     */
    public static byte[] buildCommand(StandardDevice device) {
        SAALoginRes res = new SAALoginRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_LOGIN);
        res.setDeviceId(device.getDeviceId());
        res.setDeviceType(device.getDeviceType());
        res.setGunNum(device.getGunNum());
        res.setProtocolVersion(device.getProtocolVersion());
        res.setProgramVersion(device.getProgramVersion());
        res.setNetworkLinkType(device.getNetworkLinkType());
        res.setSimNo(device.getSimNo());
        res.setCarrier(device.getCarrier());

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, false);

        // 记录日志
        if (CloudChargeHolder.isSimulatorLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-05-26 17:04:20
     */
    private byte[] buildBody() {
        byte[] body = new byte[30];
        // 1.设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 2.设备类型 [1字节] [BIN]
        body[7] = (byte) (this.deviceType & 0xFF);
        // 3.充电枪数量 [1字节] [BIN]
        body[8] = (byte) (this.gunNum & 0xFF);
        // 4.通信协议版本 [1字节] [BIN]
        String ver = this.protocolVersion.replace("V", "");
        String[] verArr = ver.split("\\.");
        int major = Integer.parseInt(verArr[0]);
        int minor = Integer.parseInt(verArr[1]);
        body[9] = (byte) (major * 10 + minor);
        // 5.程序版本 [8字节] [ASCII]
        byte[] programBytes = this.programVersion.getBytes();
        int copyLen = Math.min(programBytes.length, 8);
        System.arraycopy(programBytes, 0, body, 10, copyLen);
        // 不足部分补 0x00
        for (int i = 10 + copyLen; i < 18; i++) {
            body[i] = 0x00;
        }
        // 6.网络连接类型 [1字节] [BIN]
        body[18] = (byte) (this.networkLinkType & 0xFF);
        // 7.sim卡 [10字节] [BCD]
        String simNoFull = StrUtil.padPre(this.simNo, 20, '0');
        byte[] simBcd = StringUtil.string2bcd(simNoFull);
        System.arraycopy(simBcd, 0, body, 19, 10);
        // 8.运营商 [1字节] [BIN]
        body[29] = (byte) (this.carrier & 0xFF);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-26 17:09:30
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x01】 {} 设备登录认证  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x01】 {} 设备登录认证  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x01】 {} 设备登录认证  设备类型    deviceType                   : {}", PURPLE + deviceId + RESET, deviceType);
        log.info("🚀 【0x01】 {} 设备登录认证  充电枪数    gunNum                       : {}", PURPLE + deviceId + RESET, gunNum);
        log.info("🚀 【0x01】 {} 设备登录认证  协议版本    protocolVersion              : {}", PURPLE + deviceId + RESET, protocolVersion);
        log.info("🚀 【0x01】 {} 设备登录认证  程序版本    programVersion               : {}", PURPLE + deviceId + RESET, programVersion);
        log.info("🚀 【0x01】 {} 设备登录认证  网络类型    networkLinkType              : {}", PURPLE + deviceId + RESET, networkLinkType);
        log.info("🚀 【0x01】 {} 设备登录认证  芯片卡号    simNo                        : {}", PURPLE + deviceId + RESET, simNo);
        log.info("🚀 【0x01】 {} 设备登录认证  运营商家    carrier                      : {}", PURPLE + deviceId + RESET, carrier);
        System.out.println();
    }
}
