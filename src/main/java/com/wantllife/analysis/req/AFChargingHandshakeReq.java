package com.wantllife.analysis.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 充电握手 [0x15]
 *
 * @author KevenPotter
 * @date 2026-04-24 15:01:33
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AFChargingHandshakeReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*BMS通信协议版本号*/
    private String communicationProtocolVersion;
    /*电池类型*/
    private Integer batteryType;
    /*电池类型描述*/
    private String batteryDesc;
    /*整车动力蓄电池系统额定容量*/
    private BigDecimal batteryRated;
    /*整车动力蓄电池系统额定总电压*/
    private BigDecimal batteryTotalVoltage;
    /*电池生产厂商名称*/
    private String batteryManufacturer;
    /*电池组序号*/
    private String batterySerialNo;
    /*电池组生产日期年*/
    private Integer batteryProductionYear;
    /*电池组生产日期月*/
    private Integer batteryProductionMonth;
    /*电池组生产日期日*/
    private Integer batteryProductionDay;
    /*电池组充电次数*/
    private Integer batteryChargeCounts;
    /*电池组产权标识*/
    private Integer batteryPropertyIdentification;
    /*电池组产权标识描述*/
    private String batteryPropertyIdentificationDesc;
    /*预留位*/
    private String reserved;
    /*车辆识别码*/
    private String vin;
    /*软件版本号*/
    private String softwareVersion;
    /*软件版本号描述*/
    private String softwareVersionDesc;

    /* 有参构造 */
    public AFChargingHandshakeReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-24 15:07:32
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        this.setTradeNo(StringUtil.bcd2String(data, index, 16));
        index += 16;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BCD]
        this.setGunNo(data[index++] & 0xFF);
        // BMS通信协议版本号 [3字节] [BIN]
        byte[] verBytes = new byte[3];
        System.arraycopy(data, index, verBytes, 0, 3);
        int mainVer = (verBytes[1] & 0xFF);
        int subVer = (verBytes[2] & 0xFF);
        this.setCommunicationProtocolVersion(String.format("V%d.%d", mainVer, subVer));
        index += 3;
        // BMS电池类型 [1字节] [BIN]
        this.setBatteryType(data[index++] & 0xFF);
        this.setBatteryDesc(parseBatteryTypeDesc(this.batteryType));
        // 整车动力蓄电池系统额定容量 [2字节] [BIN]
        int ratedVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setBatteryRated(BigDecimal.valueOf(ratedVal).movePointLeft(1));
        index += 2;
        // 整车动力蓄电池系统额定总电压 [2字节] [BIN]
        int voltVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setBatteryTotalVoltage(BigDecimal.valueOf(voltVal).movePointLeft(1));
        index += 2;
        // BMS电池生产厂商名称 [4字节] [BIN]
        this.setBatteryManufacturer(new String(data, index, 4).trim());
        index += 4;
        // BMS电池组序号 [4字节] [BIN]
        byte[] serialBuf = new byte[4];
        System.arraycopy(data, index, serialBuf, 0, 4);
        this.setBatterySerialNo(HexUtil.encodeHexStr(serialBuf, false).toUpperCase());
        index += 4;
        // BMS电池组生产日期年 [1字节] [BIN]
        this.setBatteryProductionYear((data[index++] & 0xFF) + 1985);
        // BMS电池组生产日期月 [1字节] [BIN]
        this.setBatteryProductionMonth(data[index++] & 0xFF);
        // BMS电池组生产日期日 [1字节] [BIN]
        this.setBatteryProductionDay(data[index++] & 0xFF);
        // BMS电池组充电次数 [3字节] [BIN]
        int chargeCount = ((data[index] & 0xFF) << 16) | ((data[index + 1] & 0xFF) << 8) | (data[index + 2] & 0xFF);
        this.setBatteryChargeCounts(chargeCount);
        index += 3;
        // BMS电池组产权标识 [1字节] [BIN]
        this.setBatteryPropertyIdentification(data[index++] & 0xFF);
        this.setBatteryPropertyIdentificationDesc(parsePropertyDesc(this.batteryPropertyIdentification));
        // 预留位 [1字节] [BIN]
        this.setReserved(String.format("%02X", data[index++] & 0xFF));
        // BMS车辆识别码 [17字节] [BIN]
        this.setVin(new String(data, index, 17).trim());
        index += 17;
        // BMS软件版本号 [8字节] [BIN]
        byte[] verBuf = new byte[8];
        System.arraycopy(data, index, verBuf, 0, 8);
        this.setSoftwareVersion(HexUtil.encodeHexStr(verBuf, false).toUpperCase());
        // 文档解析：Byte8 Byte7 Byte6 | Byte5 Byte4 | Byte3 | Byte2 | Byte1
        int year = ((verBuf[4] & 0xFF) << 8) | (verBuf[3] & 0xFF);
        int month = verBuf[5] & 0xFF;
        int day = verBuf[6] & 0xFF;
        int serialNo = verBuf[7] & 0xFF;
        this.setSoftwareVersionDesc(String.format("V%d.%d.%d.%d", year, month, day, serialNo));
        index += 8;
    }

    /**
     * 电池类型描述
     *
     * @author KevenPotter
     * @date 2026-4-24 15:07:52
     */
    private String parseBatteryTypeDesc(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1:
                return "铅酸电池";
            case 2:
                return "氢电池";
            case 3:
                return "磷酸铁锂电池";
            case 4:
                return "锰酸锂电池";
            case 5:
                return "钴酸锂电池";
            case 6:
                return "三元材料电池";
            case 7:
                return "聚合物锂离子电池";
            case 8:
                return "钛酸锂电池";
            case 0xFF:
                return "其他";
            default:
                return "未知";
        }
    }

    /**
     * 电池产权描述
     *
     * @author KevenPotter
     * @date 2026-4-24 15:08:27
     */
    private String parsePropertyDesc(Integer flag) {
        if (flag == null) return "未知";
        switch (flag) {
            case 0:
                return "租赁";
            case 1:
                return "车自有";
            default:
                return "未知";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 21:26:52
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x15】 {} 充电握手 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x15】 {} 充电握手 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x15】 {} 充电握手 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x15】 {} 充电握手 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x15】 {} 充电握手 通信版本    communicationProtocolVersion : {}", deviceId, communicationProtocolVersion);
        log.info("🟢 【0x15】 {} 充电握手 电池类型    batteryDesc                  : {}", deviceId, batteryDesc);
        log.info("🟢 【0x15】 {} 充电握手 额定容量    batteryRated                 : {}", deviceId, batteryRated);
        log.info("🟢 【0x15】 {} 充电握手 额定电压    batteryTotalVoltage          : {}", deviceId, batteryTotalVoltage);
        log.info("🟢 【0x15】 {} 充电握手 厂商名称    batteryManufacturer          : {}", deviceId, batteryManufacturer);
        log.info("🟢 【0x15】 {} 充电握手 电池序号    batterySerialNo              : {}", deviceId, batterySerialNo);
        log.info("🟢 【0x15】 {} 充电握手 生产日期    ProductionDate               : {}-{}-{}", deviceId, batteryProductionYear, batteryProductionMonth, batteryProductionDay);
        log.info("🟢 【0x15】 {} 充电握手 充电次数    batteryChargeCounts          : {}", deviceId, batteryChargeCounts);
        log.info("🟢 【0x15】 {} 充电握手 产权标识    propertyIdentificationDesc   : {}", deviceId, batteryPropertyIdentificationDesc);
        log.info("🟢 【0x15】 {} 充电握手 预留位值    reserved                     : {}", deviceId, reserved);
        log.info("🟢 【0x15】 {} 充电握手 车识别码    VIN                          : {}", deviceId, vin);
        log.info("🟢 【0x15】 {} 充电握手 软件版本    softwareVersionDesc          : {}", deviceId, softwareVersionDesc);
        System.out.println();
    }

}
