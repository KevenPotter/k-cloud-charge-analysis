package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 充电过程BMS需求与充电机输出 [0X23]
 *
 * @author KevenPotter
 * @date 2026-04-25 09:29:20
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ALChargingBMSDemandReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*BMS电压需求*/
    private BigDecimal voltageDemand;
    /*BMS电流需求*/
    private BigDecimal currentDemand;
    /*BMS充电模式*/
    private Integer chargeMode;
    /*BMS充电模式描述*/
    private String chargeModeDesc;
    /*BMS充电电压测量值*/
    private BigDecimal voltageMeasure;
    /*BMS充电电流测量值*/
    private BigDecimal currentMeasure;
    /*BMS最高单体电压+组号*/
    private String maxVoltageGroup;
    /*最高单体电压*/
    private BigDecimal maxVoltage;
    /*最高电压所在组号*/
    private Integer maxVoltageGroupNo;
    /*BMS当前SOC*/
    private Integer soc;
    /*BMS估算剩余充电时间*/
    private Integer remainingChargingTime;
    /*电桩电压输出值*/
    private BigDecimal voltageOutput;
    /*电桩电流输出值*/
    private BigDecimal currentOutput;
    /*累计充电时间*/
    private Integer accumulatedChargingTime;


    /* 有参构造 */
    public ALChargingBMSDemandReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-25 09:50:30
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
        // BMS电压需求 [2字节] [BIN]
        int voltageDemandVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.voltageDemand = new BigDecimal(voltageDemandVal).movePointLeft(1);
        // BMS电流需求 [2字节] [BIN]
        int currentDemandVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.currentDemand = new BigDecimal(currentDemandVal).movePointLeft(1).subtract(new BigDecimal("400"));
        // BMS充电模式 [1字节] [BIN]
        this.chargeMode = data[index++] & 0xFF;
        this.chargeModeDesc = parseChargeMode(chargeMode);
        // BMS充电电压测量值 [2字节] [BIN]
        int voltageMeasureVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.voltageMeasure = new BigDecimal(voltageMeasureVal).movePointLeft(1);
        // BMS充电电流测量值 [2字节] [BIN]
        int currentMeasureVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.currentMeasure = new BigDecimal(currentMeasureVal).movePointLeft(1).subtract(new BigDecimal("400"));
        // BMS最高单体动力蓄电池电压及组号 [2字节] [BIN]
        int maxVoltageGroupVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.maxVoltageGroup = String.format("%16s", Integer.toBinaryString(maxVoltageGroupVal)).replace(' ', '0');
        int cellVoltageRaw = (maxVoltageGroupVal >> 4) & 0x0FFF;
        this.maxVoltage = new BigDecimal(cellVoltageRaw).movePointLeft(2);
        this.maxVoltageGroupNo = maxVoltageGroupVal & 0x0F;
        // BMS当前荷电状态SOC（%） [1字节] [BIN]
        this.soc = data[index++] & 0xFF;
        // BMS估算剩余充电时间 [2字节] [BIN]
        this.remainingChargingTime = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        // 电桩电压输出值 [2字节] [BIN]
        int voltageOutputVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.voltageOutput = new BigDecimal(voltageOutputVal).movePointLeft(1);
        // 电桩电流输出值 [2字节] [BIN]
        int currentOutputVal = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.currentOutput = new BigDecimal(currentOutputVal).movePointLeft(1).subtract(new BigDecimal("400"));
        // 累计充电时间 [2字节] [BIN]
        this.accumulatedChargingTime = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
    }

    /**
     * 充电模式解析
     *
     * @author KevenPotter
     * @date 2026-04-25 23:28:10
     */
    private String parseChargeMode(int val) {
        if (val == 0x01) return "恒压充电";
        if (val == 0x02) return "恒流充电";
        return "未知";
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 10:18:30
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 电压需求    voltageDemand                : {}", deviceId, voltageDemand);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 电流需求    currentDemand                : {}", deviceId, currentDemand);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 充电模式    chargeModeDesc               : {}", deviceId, chargeModeDesc);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 电压测值    voltageMeasure               : {}", deviceId, voltageMeasure);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 电流测值    currentMeasure               : {}", deviceId, currentMeasure);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 最高单压    maxVoltage                   : {}", deviceId, maxVoltage);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 电压组号    maxVoltageGroupNo            : {}", deviceId, maxVoltageGroupNo);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 充电率值    SOC                          : {}", deviceId, soc);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 剩余时间    remainingChargingTime        : {}", deviceId, remainingChargingTime);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 电压输出    voltageOutput                : {}", deviceId, voltageOutput);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 电流输出    currentOutput                : {}", deviceId, currentOutput);
        log.info("🟢 【0x23】 {} 充电过程BMS需求与充电机输出 累充时间    accumulatedChargingTime      : {}", deviceId, accumulatedChargingTime);
        System.out.println();
    }

}
