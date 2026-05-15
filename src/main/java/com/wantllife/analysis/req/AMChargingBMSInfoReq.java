package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电过程BMS信息 [0X25]
 *
 * @author KevenPotter
 * @date 2026-04-25 09:50:21
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AMChargingBMSInfoReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*最高单体电压所在编号*/
    private Integer maxVoltageNo;
    /*最高动力蓄电池温度*/
    private Integer maxBatteryTemperature;
    /*最高温度检测点编号*/
    private Integer maxTemperatureCheckNo;
    /*最低蓄电池温度*/
    private Integer minBatteryTemperature;
    /*最低温度检测点编号*/
    private Integer minTemperatureCheckNo;
    /*状态位原始值*/
    private String statusBits;
    /*单体电压过高/过低描述*/
    private String voltageStatusDesc;
    /*SOC过高/过低描述*/
    private String socStatusDesc;
    /*充电过流描述*/
    private String chargeOverCurrentDesc;
    /*电池温度过高描述*/
    private String batteryTemperatureOverDesc;
    /*绝缘状态描述*/
    private String insulationStatusDesc;
    /*连接器连接状态描述*/
    private String connectorStatusDesc;
    /*充电禁止描述*/
    private String chargeForbidDesc;
    /*预留位描述*/
    private String reservedDesc;


    /* 有参构造 */
    public AMChargingBMSInfoReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-24 23:36:05
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        this.tradeNo = StringUtil.bcd2String(data, index, 16);
        index += 16;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BCD]
        this.gunNo = data[index++] & 0xFF;
        // BMS最高单体动力蓄电池电压所在编号 [1字节] [BIN]
        this.maxVoltageNo = (data[index++] & 0xFF) + 1;
        // BMS最高动力蓄电池温度 [1字节] [BIN]
        this.maxBatteryTemperature = (data[index++] & 0xFF) - 50;
        // 最高温度检测点编号 [1字节] [BIN]
        this.maxTemperatureCheckNo = (data[index++] & 0xFF) + 1;
        // 最低动力蓄电池温度 [1字节] [BIN]
        this.minBatteryTemperature = (data[index++] & 0xFF) - 50;
        // 最低动力蓄电池温度检测点编号 [1字节] [BIN]
        this.minTemperatureCheckNo = (data[index++] & 0xFF) + 1;
        // 状态位组合 [2字节] [BIN]
        int statusRaw = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.statusBits = String.format("%16s", Integer.toBinaryString(statusRaw)).replace(' ', '0');
        String bit9 = statusBits.substring(0, 2);       // 单体电压
        String bit10 = statusBits.substring(2, 4);      // SOC
        String bit11 = statusBits.substring(4, 6);      // 充电过流
        String bit12 = statusBits.substring(6, 8);      // 电池温度过高
        String bit13 = statusBits.substring(8, 10);     // 绝缘状态
        String bit14 = statusBits.substring(10, 12);    // 连接器状态
        String bit15 = statusBits.substring(12, 14);    // 充电禁止
        String bit16 = statusBits.substring(14, 16);    // 预留
        // 解析描述
        this.voltageStatusDesc = parseVoltageStatusDesc(bit9);
        this.socStatusDesc = parseSocStatusDesc(bit10);
        this.chargeOverCurrentDesc = parseChargeOverCurrentDesc(bit11);
        this.batteryTemperatureOverDesc = parseBatteryTemperatureOverDesc(bit12);
        this.insulationStatusDesc = parseInsulationStatusDesc(bit13);
        this.connectorStatusDesc = parseConnectorStatusDesc(bit14);
        this.chargeForbidDesc = parseChargeForbidDesc(bit15);
        this.reservedDesc = "00";
    }

    /**
     * 解析单体电压过高/过低描述
     *
     * @author KevenPotter
     * @date 2026-04-26 00:21:20
     */
    private String parseVoltageStatusDesc(String bit) {
        switch (bit) {
            case "00":
                return "正常";
            case "01":
                return "过高";
            case "10":
                return "过低";
            default:
                return "异常";
        }
    }

    /**
     * 解析SOC过高/过低描述
     *
     * @author KevenPotter
     * @date 2026-04-26 00:21:25
     */
    private String parseSocStatusDesc(String bit) {
        switch (bit) {
            case "00":
                return "正常";
            case "01":
                return "过高";
            case "10":
                return "过低";
            default:
                return "异常";
        }
    }

    /**
     * 解析充电过流描述
     *
     * @author KevenPotter
     * @date 2026-04-26 00:21:39
     */
    private String parseChargeOverCurrentDesc(String bit) {
        switch (bit) {
            case "00":
                return "正常";
            case "01":
                return "过流";
            case "10":
                return "不可信状态";
            default:
                return "异常";
        }
    }

    /**
     * 解析电池温度过高描述
     *
     * @author KevenPotter
     * @date 2026-04-26 00:21:50
     */
    private String parseBatteryTemperatureOverDesc(String bit) {
        switch (bit) {
            case "00":
                return "正常";
            case "01":
                return "过温";
            case "10":
                return "不可信状态";
            default:
                return "异常";
        }
    }

    /**
     * 解析绝缘状态描述
     *
     * @author KevenPotter
     * @date 2026-04-26 00:22:07
     */
    private String parseInsulationStatusDesc(String bit) {
        switch (bit) {
            case "00":
                return "正常";
            case "01":
                return "绝缘异常";
            case "10":
                return "不可信状态";
            default:
                return "异常";
        }
    }

    /**
     * 解析连接器连接状态描述
     *
     * @author KevenPotter
     * @date 2026-04-26 00:22:34
     */
    private String parseConnectorStatusDesc(String bit) {
        switch (bit) {
            case "00":
                return "连接正常";
            case "01":
                return "连接异常";
            case "10":
                return "不可信状态";
            default:
                return "异常";
        }
    }

    /**
     * 解析充电禁止描述
     *
     * @author KevenPotter
     * @date 2026-04-26 00:23:19
     */
    private String parseChargeForbidDesc(String bit) {
        switch (bit) {
            case "00":
                return "禁止充电";
            case "01":
                return "允许充电";
            default:
                return "异常";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 13:19:19
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x25】 {} 充电过程BMS信息 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 单压编号    maxVoltageNo                 : {}", deviceId, maxVoltageNo);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 最高温度    maxBatteryTemperature        : {}", deviceId, maxBatteryTemperature);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 检测高温    maxTemperatureCheckNo        : {}", deviceId, maxTemperatureCheckNo);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 最低温度    minBatteryTemperature        : {}", deviceId, minBatteryTemperature);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 检测低温    minTemperatureCheckNo        : {}", deviceId, minTemperatureCheckNo);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 状态原值    statusBits                   : {}", deviceId, statusBits);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 电压高低    voltageStatusDesc            : {}", deviceId, voltageStatusDesc);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 率值高低    socStatusDesc                : {}", deviceId, socStatusDesc);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 充电过流    chargeOverCurrentDesc        : {}", deviceId, chargeOverCurrentDesc);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 温度过高    batteryTemperatureOverDesc   : {}", deviceId, batteryTemperatureOverDesc);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 绝缘状态    insulationStatusDesc         : {}", deviceId, insulationStatusDesc);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 连接状态    connectorStatusDesc          : {}", deviceId, connectorStatusDesc);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 充电禁止    chargeForbidDesc             : {}", deviceId, chargeForbidDesc);
        log.info("🟢 【0x25】 {} 充电过程BMS信息 预留位值    reservedDesc                 : {}", deviceId, reservedDesc);
        System.out.println();
    }

}
