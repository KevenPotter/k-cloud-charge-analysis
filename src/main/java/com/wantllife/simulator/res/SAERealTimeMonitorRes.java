package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardRealTimeMonitor;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_REAL_TIME_MONITOR;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 上传实时监测数据 [0X13]
 *
 * @author KevenPotter
 * @date 2026-06-01 17:08:22
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAERealTimeMonitorRes extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*状态*/
    private Integer status;
    /*枪是否归位*/
    private Integer haveReturn;
    /*是否插枪*/
    private Integer haveInsert;
    /*输出电压*/
    private BigDecimal voltage;
    /*输出电流*/
    private BigDecimal current;
    /*枪线温度*/
    private Integer temperature;
    /*枪线编码*/
    private String gunCode;
    /*SOC*/
    private Integer soc;
    /*电池组最高温度*/
    private Integer highestTemperature;
    /*累计充电时间*/
    private Integer accumulatedChargingTime;
    /*剩余时间*/
    private Integer remainingChargingTime;
    /*充电度数*/
    private BigDecimal chargingDegree;
    /*计损充电度数*/
    private BigDecimal calculatedChargingDegree;
    /*已充金额*/
    private BigDecimal chargedAmount;
    /*硬件故障*/
    private String hardwareFailure;

    /**
     * 构建下发指令
     *
     * @param realTimeMonitor 实时监控
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-01 17:13:24
     */
    public static byte[] buildCommand(StandardRealTimeMonitor realTimeMonitor) {
        SAERealTimeMonitorRes res = new SAERealTimeMonitorRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_REAL_TIME_MONITOR);
        res.setTradeNo(realTimeMonitor.getTradeNo());
        res.setDeviceId(realTimeMonitor.getDeviceId());
        res.setGunNo(realTimeMonitor.getGunNo());
        res.setStatus(realTimeMonitor.getStatus());
        res.setHaveReturn(realTimeMonitor.getHaveReturn());
        res.setHaveInsert(realTimeMonitor.getHaveInsert());
        res.setVoltage(realTimeMonitor.getVoltage());
        res.setCurrent(realTimeMonitor.getCurrent());
        res.setTemperature(realTimeMonitor.getTemperature());
        res.setGunCode(realTimeMonitor.getGunCode());
        res.setSoc(realTimeMonitor.getSoc());
        res.setHighestTemperature(realTimeMonitor.getHighestTemperature());
        res.setAccumulatedChargingTime(realTimeMonitor.getAccumulatedChargingTime());
        res.setRemainingChargingTime(realTimeMonitor.getRemainingChargingTime());
        res.setChargingDegree(realTimeMonitor.getChargingDegree());
        res.setCalculatedChargingDegree(realTimeMonitor.getCalculatedChargingDegree());
        res.setChargedAmount(realTimeMonitor.getChargedAmount());
        res.setHardwareFailure(realTimeMonitor.getHardwareFailure());

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
     * @date 2026-06-02 09:36:21
     */
    private byte[] buildBody() {
        byte[] body = new byte[60];
        // 交易流水号 [16字节] [BCD]
        String tradeNoFull = StrUtil.padPre(this.tradeNo, 32, '0');
        byte[] tradeNoBcd = StringUtil.string2bcd(tradeNoFull);
        System.arraycopy(tradeNoBcd, 0, body, 0, 16);
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 16, 7);
        // 枪号 [1字节] [BIN]
        body[23] = (byte) (this.gunNo & 0xFF);
        // 状态 [1字节] [BIN]
        body[24] = (byte) (this.status & 0xFF);
        // 枪是否归位 [1字节] [BIN]
        body[25] = (byte) (this.haveReturn & 0xFF);
        // 是否插枪 [1字节] [BIN]
        body[26] = (byte) (this.haveInsert & 0xFF);
        // 输出电压 [2字节] [BIN]
        int voltageVal = this.voltage.multiply(new BigDecimal("10")).intValue();
        body[27] = (byte) (voltageVal & 0xFF);
        body[28] = (byte) ((voltageVal >> 8) & 0xFF);
        // 输出电流 [2字节] [BIN]
        int currentVal = this.current.multiply(new BigDecimal("10")).intValue();
        body[29] = (byte) (currentVal & 0xFF);
        body[30] = (byte) ((currentVal >> 8) & 0xFF);
        // 枪线温度 [1字节] [BIN]
        body[31] = (byte) ((this.temperature + 50) & 0xFF);
        // 枪线编码 [8字节] [BIN]
        String gunCodeHex = StrUtil.padPre(this.gunCode, 16, '0');
        gunCodeHex = gunCodeHex.toUpperCase();
        byte[] gunCodeBytes = HexUtil.decodeHex(gunCodeHex);
        System.arraycopy(gunCodeBytes, 0, body, 32, 8);
        // SOC [1字节] [BIN]
        body[40] = (byte) (this.soc & 0xFF);
        // 电池组最高温度 [1字节] [BIN]
        body[41] = (byte) ((this.highestTemperature + 50) & 0xFF);
        // 累计充电时间 [2字节] [BIN]
        body[42] = (byte) (this.accumulatedChargingTime & 0xFF);
        body[43] = (byte) ((this.accumulatedChargingTime >> 8) & 0xFF);
        // 剩余时间 [2字节] [BIN]
        body[44] = (byte) (this.remainingChargingTime & 0xFF);
        body[45] = (byte) ((this.remainingChargingTime >> 8) & 0xFF);
        // 充电度数 [4字节] [BIN]
        long chargeDegreeVal = this.chargingDegree.multiply(new BigDecimal("10000")).longValue();
        body[46] = (byte) (chargeDegreeVal & 0xFF);
        body[47] = (byte) ((chargeDegreeVal >> 8) & 0xFF);
        body[48] = (byte) ((chargeDegreeVal >> 16) & 0xFF);
        body[49] = (byte) ((chargeDegreeVal >> 24) & 0xFF);
        // 计损充电度数 [4字节] [BIN]
        long calcDegreeVal = this.calculatedChargingDegree.multiply(new BigDecimal("10000")).longValue();
        body[50] = (byte) (calcDegreeVal & 0xFF);
        body[51] = (byte) ((calcDegreeVal >> 8) & 0xFF);
        body[52] = (byte) ((calcDegreeVal >> 16) & 0xFF);
        body[53] = (byte) ((calcDegreeVal >> 24) & 0xFF);
        // 已充金额 [4字节] [BIN]
        long chargedAmountVal = this.chargedAmount.multiply(new BigDecimal("10000")).longValue();
        body[54] = (byte) (chargedAmountVal & 0xFF);
        body[55] = (byte) ((chargedAmountVal >> 8) & 0xFF);
        body[56] = (byte) ((chargedAmountVal >> 16) & 0xFF);
        body[57] = (byte) ((chargedAmountVal >> 24) & 0xFF);
        // 硬件故障 [2字节] [BIN]
        int faultVal = Integer.parseInt(this.hardwareFailure, 16);
        body[58] = (byte) (faultVal & 0xFF);
        body[59] = (byte) ((faultVal >> 8) & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-02 09:36:54
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(4096);
        String devLabel = PURPLE + "⇓ 【0x13】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 实时监测数据  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 实时监测数据  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 实时监测数据  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 实时监测数据  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 实时监测数据  设备状态    status                       : %s\n", devLabel, status));
        sb.append(String.format("👩‍🚀%s 实时监测数据  插枪归位    haveReturn                   : %s\n", devLabel, haveReturn));
        sb.append(String.format("👩‍🚀%s 实时监测数据  是否插枪    haveInsert                   : %s\n", devLabel, haveInsert == 0 ? "未插枪" : "已插枪"));
        sb.append(String.format("👩‍🚀%s 实时监测数据  输出电压    voltage                      : %s\n", devLabel, voltage));
        sb.append(String.format("👩‍🚀%s 实时监测数据  输出电流    current                      : %s\n", devLabel, current));
        sb.append(String.format("👩‍🚀%s 实时监测数据  枪线温度    temperature                  : %s\n", devLabel, temperature));
        sb.append(String.format("👩‍🚀%s 实时监测数据  枪线编码    gunCode                      : %s\n", devLabel, gunCode));
        sb.append(String.format("👩‍🚀%s 实时监测数据  充电率值    SOC                          : %s\n", devLabel, soc));
        sb.append(String.format("👩‍🚀%s 实时监测数据  最高温度    highestTemperature           : %s\n", devLabel, highestTemperature));
        sb.append(String.format("👩‍🚀%s 实时监测数据  累充时间    accumulatedChargingTime      : %s\n", devLabel, accumulatedChargingTime));
        sb.append(String.format("👩‍🚀%s 实时监测数据  剩余时间    remainingChargingTime        : %s\n", devLabel, remainingChargingTime));
        sb.append(String.format("👩‍🚀%s 实时监测数据  充电度数    chargingDegree               : %s\n", devLabel, chargingDegree));
        sb.append(String.format("👩‍🚀%s 实时监测数据  计损度数    calculatedChargingDegree     : %s\n", devLabel, calculatedChargingDegree));
        sb.append(String.format("👩‍🚀%s 实时监测数据  已充金额    chargedAmount                : %s\n", devLabel, chargedAmount));
        sb.append(String.format("👩‍🚀%s 实时监测数据  故障描述    hardwareFailure              : %s\n", devLabel, hardwareFailure));
        log.info(sb.toString());
    }
}
