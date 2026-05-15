package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 参数配置 [0X17]
 *
 * @author KevenPotter
 * @date 2026-04-24 15:08:59
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AGParamConfigReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*BMS单体最高允许充电电压*/
    private BigDecimal maxAllowVoltage;
    /*BMS最高允许充电电流*/
    private BigDecimal maxChargeCurrent;
    /*BMS蓄电池标称总能量*/
    private BigDecimal ratedEnergy;
    /*BMS最高允许充电总电压*/
    private BigDecimal maxTotalVoltage;
    /*BMS最高允许温度*/
    private Integer maxTemperature;
    /*SOC*/
    private BigDecimal soc;
    /*BMS当前总电压*/
    private BigDecimal currentTotalVoltage;
    /*电桩最高输出电压*/
    private BigDecimal maxOutVoltage;
    /*电桩最低输出电压*/
    private BigDecimal minOutVoltage;
    /*电桩最大输出电流*/
    private BigDecimal maxOutCurrent;
    /*电桩最小输出电流*/
    private BigDecimal minOutCurrent;

    /* 有参构造 */
    public AGParamConfigReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-24 15:24:08
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
        // BMS单体动力蓄电池最高允许充电电压 [2字节] [BIN]
        int cellMax = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setMaxAllowVoltage(new BigDecimal(cellMax).divide(new BigDecimal("100")));
        index += 2;
        // BMS最高允许充电电流 [2字节] [BIN]
        int bmsCurr = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setMaxChargeCurrent(new BigDecimal(bmsCurr - 4000).divide(new BigDecimal("10")));
        index += 2;
        // BMS动力蓄电池标称总能量 [2字节] [BIN]
        int energy = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setRatedEnergy(new BigDecimal(energy).divide(new BigDecimal("10")));
        index += 2;
        // BMS最高允许充电总电压 [2字节] [BIN]
        int totalMaxV = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setMaxTotalVoltage(new BigDecimal(totalMaxV).divide(new BigDecimal("10")));
        index += 2;
        // BMS最高允许温度 [1字节] [BIN]
        this.setMaxTemperature((data[index++] & 0xFF) - 50);
        // BMS整车动力蓄电池荷电状态(soc) [2字节] [BIN]
        int soc = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setSoc(new BigDecimal(soc).divide(new BigDecimal("10")));
        index += 2;
        // BMS整车动力蓄电池当前电池电压 [2字节] [BIN]
        int currV = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setCurrentTotalVoltage(new BigDecimal(currV).divide(new BigDecimal("10")));
        index += 2;
        // 电桩最高输出电压 [2字节] [BIN]
        int pileMaxV = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setMaxOutVoltage(new BigDecimal(pileMaxV).divide(new BigDecimal("10")));
        index += 2;
        // 电桩最低输出电压 [2字节] [BIN]
        int pileMinV = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setMinOutVoltage(new BigDecimal(pileMinV).divide(new BigDecimal("10")));
        index += 2;
        // 电桩最大输出电流 [2字节] [BIN]
        int pileMaxCurr = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setMaxOutCurrent(new BigDecimal(pileMaxCurr - 4000).divide(new BigDecimal("10")));
        index += 2;
        // 电桩最小输出电流 [2字节] [BIN]
        int pileMinCurr = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        this.setMinOutCurrent(new BigDecimal(pileMinCurr - 4000).divide(new BigDecimal("10")));
        index += 2;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 10:00:04
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x17】 {} 参数配置 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x17】 {} 参数配置 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x17】 {} 参数配置 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x17】 {} 参数配置 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x17】 {} 参数配置 允许电压    maxAllowVoltage              : {}", deviceId, maxAllowVoltage);
        log.info("🟢 【0x17】 {} 参数配置 允许电流    maxChargeCurrent             : {}", deviceId, maxChargeCurrent);
        log.info("🟢 【0x17】 {} 参数配置 标称能量    ratedEnergy                  : {}", deviceId, ratedEnergy);
        log.info("🟢 【0x17】 {} 参数配置 最高电压    maxTotalVoltage              : {}", deviceId, maxTotalVoltage);
        log.info("🟢 【0x17】 {} 参数配置 最高温度    maxTemperature               : {}", deviceId, maxTemperature);
        log.info("🟢 【0x17】 {} 参数配置 充电率值    SOC                          : {}", deviceId, soc);
        log.info("🟢 【0x17】 {} 参数配置 当前电压    currentTotalVoltage          : {}", deviceId, currentTotalVoltage);
        log.info("🟢 【0x17】 {} 参数配置 最高输压    maxOutVoltage                : {}", deviceId, maxOutVoltage);
        log.info("🟢 【0x17】 {} 参数配置 最低输压    minOutVoltage                : {}", deviceId, minOutVoltage);
        log.info("🟢 【0x17】 {} 参数配置 最大输流    maxOutCurrent                : {}", deviceId, maxOutCurrent);
        log.info("🟢 【0x17】 {} 参数配置 最小输流    minOutCurrent                : {}", deviceId, minOutCurrent);
        System.out.println();
    }

}
