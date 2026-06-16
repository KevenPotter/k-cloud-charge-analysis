package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardChargingBMSDemand;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_CHARGING_BMS_DEMAND;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电过程BMS需求与充电机输出 [0X23]
 *
 * @author KevenPotter
 * @date 2026-06-11 10:42:33
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SALChargingBMSDemandRes extends FrameHeader {

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
    /*BMS充电电压测量值*/
    private BigDecimal voltageMeasure;
    /*BMS充电电流测量值*/
    private BigDecimal currentMeasure;
    /*BMS最高单体电压+组号*/
    private String maxVoltageGroup;
    /** 最高单体电压 */
    private BigDecimal maxVoltage;
    /** 最高电压所在组号 */
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

    /**
     * 构建下发指令
     *
     * @param standardChargingBMSDemand 充电过程BMS需求与充电机输出
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-11 10:43:20
     */
    public static byte[] buildCommand(StandardChargingBMSDemand standardChargingBMSDemand) {
        SALChargingBMSDemandRes res = new SALChargingBMSDemandRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_CHARGING_BMS_DEMAND);
        res.setTradeNo(standardChargingBMSDemand.getTradeNo());
        res.setDeviceId(standardChargingBMSDemand.getDeviceId());
        res.setGunNo(standardChargingBMSDemand.getGunNo());
        res.setVoltageDemand(standardChargingBMSDemand.getVoltageDemand());
        res.setCurrentDemand(standardChargingBMSDemand.getCurrentDemand());
        res.setChargeMode(standardChargingBMSDemand.getChargeMode());
        res.setVoltageMeasure(standardChargingBMSDemand.getVoltageMeasure());
        res.setCurrentMeasure(standardChargingBMSDemand.getCurrentMeasure());
        res.setMaxVoltage(standardChargingBMSDemand.getMaxVoltage());
        res.setMaxVoltageGroupNo(standardChargingBMSDemand.getMaxVoltageGroupNo());
        res.setSoc(standardChargingBMSDemand.getSoc());
        res.setRemainingChargingTime(standardChargingBMSDemand.getRemainingChargingTime());
        res.setVoltageOutput(standardChargingBMSDemand.getVoltageOutput());
        res.setCurrentOutput(standardChargingBMSDemand.getCurrentOutput());
        res.setAccumulatedChargingTime(standardChargingBMSDemand.getAccumulatedChargingTime());

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
     * @date 2026-06-11 10:43:57
     */
    private byte[] buildBody() {
        byte[] body = new byte[44];

        // 交易流水号 [16字节] [BCD]
        String tradeNoFull = StrUtil.padPre(this.tradeNo, 32, '0');
        byte[] tradeNoBcd = StringUtil.string2bcd(tradeNoFull);
        System.arraycopy(tradeNoBcd, 0, body, 0, 16);
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 16, 7);
        // 枪号 [1字节] [BCD]
        body[23] = StringUtil.string2bcd(StrUtil.padPre(gunNo.toString(), 2, '0'))[0];
        // BMS电压需求 [2字节] [BIN]
        int vDemand = voltageDemand.multiply(new BigDecimal(10)).intValue();
        body[24] = (byte) ((vDemand >> 8) & 0xFF);
        body[25] = (byte) (vDemand & 0xFF);
        // BMS电流需求 [2字节] [BIN]
        int iDemand = currentDemand.multiply(new BigDecimal(10)).add(new BigDecimal(4000)).intValue();
        body[26] = (byte) ((iDemand >> 8) & 0xFF);
        body[27] = (byte) (iDemand & 0xFF);
        // BMS充电模式 [1字节] [BIN]
        body[28] = (byte) (chargeMode & 0xFF);
        // BMS充电电压测量值 [2字节] [BIN]
        int vMeasure = voltageMeasure.multiply(new BigDecimal(10)).intValue();
        body[29] = (byte) ((vMeasure >> 8) & 0xFF);
        body[30] = (byte) (vMeasure & 0xFF);
        // BMS充电电流测量值 [2字节] [BIN]
        int iMeasure = currentMeasure.multiply(new BigDecimal(10)).add(new BigDecimal(4000)).intValue();
        body[31] = (byte) ((iMeasure >> 8) & 0xFF);
        body[32] = (byte) (iMeasure & 0xFF);
        // BMS最高单体电压+组号 [2字节] [BIN]
        int cellRaw = maxVoltage.multiply(new BigDecimal(100)).intValue();
        int groupCombine = (cellRaw << 4) | (maxVoltageGroupNo & 0x0F);
        body[33] = (byte) ((groupCombine >> 8) & 0xFF);
        body[34] = (byte) (groupCombine & 0xFF);
        // BMS当前SOC [1字节] [BIN]
        body[35] = (byte) (soc & 0xFF);
        // BMS估算剩余充电时间 [2字节] [BIN]
        body[36] = (byte) ((remainingChargingTime >> 8) & 0xFF);
        body[37] = (byte) (remainingChargingTime & 0xFF);
        // 电桩电压输出值 [2字节] [BIN]
        int vOutput = voltageOutput.multiply(new BigDecimal(10)).intValue();
        body[38] = (byte) ((vOutput >> 8) & 0xFF);
        body[39] = (byte) (vOutput & 0xFF);
        // 电桩电流输出值 [2字节] [BIN]
        int iOutput = currentOutput.multiply(new BigDecimal(10)).add(new BigDecimal(4000)).intValue();
        body[40] = (byte) ((iOutput >> 8) & 0xFF);
        body[41] = (byte) (iOutput & 0xFF);
        // 累计充电时间 [2字节] [BIN]
        body[42] = (byte) ((accumulatedChargingTime >> 8) & 0xFF);
        body[43] = (byte) (accumulatedChargingTime & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-11 10:44:20
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x23】 {} 电池需求输出  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x23】 {} 电池需求输出  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x23】 {} 电池需求输出  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0x23】 {} 电池需求输出  交易编号    tradeNo                      : {}", PURPLE + deviceId + RESET, tradeNo);
        log.info("🚀 【0x23】 {} 电池需求输出  电压需求    voltageDemand                : {}", PURPLE + deviceId + RESET, voltageDemand);
        log.info("🚀 【0x23】 {} 电池需求输出  电流需求    currentDemand                : {}", PURPLE + deviceId + RESET, currentDemand);
        log.info("🚀 【0x23】 {} 电池需求输出  充电模式    chargeMode                   : {}", PURPLE + deviceId + RESET, chargeMode);
        log.info("🚀 【0x23】 {} 电池需求输出  电压测值    voltageMeasure               : {}", PURPLE + deviceId + RESET, voltageMeasure);
        log.info("🚀 【0x23】 {} 电池需求输出  电流测值    currentMeasure               : {}", PURPLE + deviceId + RESET, currentMeasure);
        log.info("🚀 【0x23】 {} 电池需求输出  电流测值    maxVoltage                   : {}", PURPLE + deviceId + RESET, maxVoltage);
        log.info("🚀 【0x23】 {} 电池需求输出  电流测值    maxVoltageGroupNo            : {}", PURPLE + deviceId + RESET, maxVoltageGroupNo);
        log.info("🚀 【0x23】 {} 电池需求输出  充电率值    SOC                          : {}", PURPLE + deviceId + RESET, soc);
        log.info("🚀 【0x23】 {} 电池需求输出  剩余时间    remainingChargingTime        : {}", PURPLE + deviceId + RESET, remainingChargingTime);
        log.info("🚀 【0x23】 {} 电池需求输出  电压输出    voltageOutput                : {}", PURPLE + deviceId + RESET, voltageOutput);
        log.info("🚀 【0x23】 {} 电池需求输出  电流输出    currentOutput                : {}", PURPLE + deviceId + RESET, currentOutput);
        log.info("🚀 【0x23】 {} 电池需求输出  累充时间    accumulatedChargingTime      : {}", PURPLE + deviceId + RESET, accumulatedChargingTime);
        System.out.println();
    }
}
