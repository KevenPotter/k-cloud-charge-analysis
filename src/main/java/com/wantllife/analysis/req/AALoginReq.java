package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电桩登录认证 [0X01]
 *
 * @author KevenPotter
 * @date 2026-04-20 14:27:32
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AALoginReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*设备类型*/
    private Integer deviceType;
    /*设备类型描述*/
    private String deviceTypeDesc;
    /*充电枪数量*/
    private Integer gunNum;
    /*通信协议版本*/
    private String protocolVersion;
    /*程序版本*/
    private String programVersion;
    /*网络连接类型*/
    private Integer networkLinkType;
    /*网络连接类型描述*/
    private String networkLinkTypeDesc;
    /*sim卡*/
    private String simNo;
    /*运营商*/
    private Integer carrier;
    /*运营商描述*/
    private String carrierDesc;

    /* 有参构造 */
    public AALoginReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-04-21 10:41:08
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 设备类型 [1字节] [BIN]
        this.setDeviceType(data[index++] & 0xFF);
        this.setDeviceTypeDesc(parseDeviceTypeDesc(this.deviceType));
        // 充电枪数量 [1字节] [BIN]
        this.setGunNum(data[index++] & 0xFF);
        // 通信协议版本 [1字节] [BIN]
        byte ver = data[index++];
        int version = ver & 0xFF;
        int major = version / 10;
        int minor = version % 10;
        this.setProtocolVersion("V" + major + "." + minor);
        // 程序版本 [8字节] [ASCII]
        this.setProgramVersion(new String(data, index, 8).replace("\0", "").trim());
        index += 8;
        // 网络连接类型 [1字节] [BIN]
        this.setNetworkLinkType(data[index++] & 0xFF);
        this.setNetworkLinkTypeDesc(parseNetworkLinkTypeDesc(this.networkLinkType));
        // SIM卡 [10字节] [BCD]
        this.setSimNo(StringUtil.bcd2String(data, index, 10));
        index += 10;
        // 运营商 [1字节] [BIN]
        int carrierCode = data[index++] & 0xFF;
        this.setCarrier(carrierCode);
        this.setCarrierDesc(parseCarrierDesc(carrierCode));
    }

    /**
     * 获取设备类型描述
     *
     * @author KevenPotter
     * @date 2026-04-21 11:00:32
     */
    private String parseDeviceTypeDesc(Integer deviceType) {
        if (deviceType == null) return "未知";
        return deviceType == 0 ? "直流桩" : "交流桩";
    }

    /**
     * 获取网络连接类型描述
     *
     * @author KevenPotter
     * @date 2026-04-21 11:00:45
     */
    private String parseNetworkLinkTypeDesc(Integer networkLinkType) {
        if (networkLinkType == null) return "未知";
        switch (networkLinkType) {
            case 0:
                return "SIM卡";
            case 1:
                return "LAN";
            case 2:
                return "WAN";
            case 3:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 获取运营商描述
     *
     * @author KevenPotter
     * @date 2026-04-21 11:01:27
     */
    private String parseCarrierDesc(Integer carrier) {
        if (carrier == null) return "未知";
        switch (carrier) {
            case 0:
                return "移动";
            case 2:
                return "电信";
            case 3:
                return "联通";
            case 4:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 14:01:40
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x01】 {} 充电桩登录认证 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x01】 {} 充电桩登录认证 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🟢 【0x01】 {} 充电桩登录认证 设备类型    deviceTypeDesc       : {}", deviceId, deviceTypeDesc);
        log.info("🟢 【0x01】 {} 充电桩登录认证 充电枪数    gunNum               : {}", deviceId, gunNum);
        log.info("🟢 【0x01】 {} 充电桩登录认证 协议版本    protocolVersion      : {}", deviceId, protocolVersion);
        log.info("🟢 【0x01】 {} 充电桩登录认证 程序版本    programVersion       : {}", deviceId, programVersion);
        log.info("🟢 【0x01】 {} 充电桩登录认证 网络类型    networkLinkTypeDesc  : {}", deviceId, networkLinkTypeDesc);
        log.info("🟢 【0x01】 {} 充电桩登录认证 芯片卡号    simNo                : {}", deviceId, simNo);
        log.info("🟢 【0x01】 {} 充电桩登录认证 运营商家    carrierDesc          : {}", deviceId, carrierDesc);
        System.out.println();
    }

}
