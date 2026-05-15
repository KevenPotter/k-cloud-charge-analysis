package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 充电结束 [0X19]
 *
 * @author KevenPotter
 * @date 2026-04-24 16:07:33
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AHChargeFinishedReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*BMC中止荷电状态SOC*/
    private Integer stopSoc;
    /*BMS动力蓄电池单体最低电压*/
    private BigDecimal minVoltage;
    /*BMS动力蓄电池单体最高电压*/
    private BigDecimal maxVoltage;
    /*BMS动力蓄电池最低温度*/
    private Integer minTemperature;
    /*BMS动力蓄电池最高温度*/
    private Integer maxTemperature;
    /*电桩累计充电时间*/
    private Integer chargeTime;
    /*电桩输出能量*/
    private BigDecimal outputEnergy;
    /*电桩充电机编号*/
    private String chargeNo;

    /* 有参构造 */
    public AHChargeFinishedReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-24 16:07:25
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
        // BMS中止荷电状态SOC [1字节] [BIN]
        this.stopSoc = data[index++] & 0xFF;
        // BMS动力蓄电池单体最低电压 [2字节] [BIN]
        int minV = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.minVoltage = new BigDecimal(minV).divide(new BigDecimal("100"));
        index += 2;
        // BMS动力蓄电池单体最高电压 [2字节] [BIN]
        int maxV = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.maxVoltage = new BigDecimal(maxV).divide(new BigDecimal("100"));
        index += 2;
        // BMS动力蓄电池最低温度 [1字节] [BIN]
        this.minTemperature = (data[index++] & 0xFF) - 50;
        // BMS动力蓄电池最高温度 [1字节] [BIN]
        this.maxTemperature = (data[index++] & 0xFF) - 50;
        // 电桩累计充电时间 [2字节] [BIN]
        this.chargeTime = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        // 电桩输出能量 [2字节] [BIN]
        int energy = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.outputEnergy = new BigDecimal(energy).divide(new BigDecimal("10"));
        index += 2;
        // 电桩充电机编号 [4字节] [BIN]
        byte[] noBuf = new byte[4];
        System.arraycopy(data, index, noBuf, 0, 4);
        this.chargeNo = StringUtil.bcd2String(noBuf, 0, 4);
        index += 4;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 10:06:20
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x19】 {} 充电结束 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x19】 {} 充电结束 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x19】 {} 充电结束 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x19】 {} 充电结束 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x19】 {} 充电结束 荷电状态    stopSoc                      : {}", deviceId, stopSoc);
        log.info("🟢 【0x19】 {} 充电结束 最低电压    minVoltage                   : {}", deviceId, minVoltage);
        log.info("🟢 【0x19】 {} 充电结束 最高电压    maxVoltage                   : {}", deviceId, maxVoltage);
        log.info("🟢 【0x19】 {} 充电结束 最低温度    minTemperature               : {}", deviceId, minTemperature);
        log.info("🟢 【0x19】 {} 充电结束 最高温度    maxTemperature               : {}", deviceId, maxTemperature);
        log.info("🟢 【0x19】 {} 充电结束 累充时间    chargeTime                   : {}", deviceId, chargeTime);
        log.info("🟢 【0x19】 {} 充电结束 输出能量    outputEnergy                 : {}", deviceId, outputEnergy);
        log.info("🟢 【0x19】 {} 充电结束 电机编号    chargeNo                     : {}", deviceId, chargeNo);
        System.out.println();
    }

}
