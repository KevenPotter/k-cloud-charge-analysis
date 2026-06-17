package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardParamConfig;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_PARAM_CONFIG;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 参数配置 [0X17]
 *
 * @author KevenPotter
 * @date 2026-06-10 09:48:32
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAGParamConfigRes extends FrameHeader {

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

    /**
     * 构建下发指令
     *
     * @param standardParamConfig 参数配置
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-10 09:48:55
     */
    public static byte[] buildCommand(StandardParamConfig standardParamConfig) {
        SAGParamConfigRes res = new SAGParamConfigRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_PARAM_CONFIG);
        res.setTradeNo(standardParamConfig.getTradeNo());
        res.setDeviceId(standardParamConfig.getDeviceId());
        res.setGunNo(standardParamConfig.getGunNo());
        res.setMaxAllowVoltage(standardParamConfig.getMaxAllowVoltage());
        res.setMaxChargeCurrent(standardParamConfig.getMaxChargeCurrent());
        res.setRatedEnergy(standardParamConfig.getRatedEnergy());
        res.setMaxTotalVoltage(standardParamConfig.getMaxTotalVoltage());
        res.setMaxTemperature(standardParamConfig.getMaxTemperature());
        res.setSoc(standardParamConfig.getSoc());
        res.setCurrentTotalVoltage(standardParamConfig.getCurrentTotalVoltage());
        res.setMaxOutVoltage(standardParamConfig.getMaxOutVoltage());
        res.setMinOutVoltage(standardParamConfig.getMinOutVoltage());
        res.setMaxOutCurrent(standardParamConfig.getMaxOutCurrent());
        res.setMinOutCurrent(standardParamConfig.getMinOutCurrent());

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
     * @date 2026-06-10 09:49:24
     */
    private byte[] buildBody() {
        byte[] body = new byte[45];

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
        // BMS单体最高允许充电电压 [2字节] [BIN]
        int v1 = maxAllowVoltage.multiply(new BigDecimal(100)).intValue();
        body[24] = (byte) ((v1 >> 8) & 0xFF);
        body[25] = (byte) (v1 & 0xFF);
        // BMS最高允许充电电流 [2字节] [BIN]
        int curr1 = maxChargeCurrent.multiply(new BigDecimal(10)).add(new BigDecimal(4000)).intValue();
        body[26] = (byte) ((curr1 >> 8) & 0xFF);
        body[27] = (byte) (curr1 & 0xFF);
        // BMS蓄电池标称总能量 [2字节] [BIN]
        int energy = ratedEnergy.multiply(new BigDecimal(10)).intValue();
        body[28] = (byte) ((energy >> 8) & 0xFF);
        body[29] = (byte) (energy & 0xFF);
        // BMS最高允许充电总电压 [2字节] [BIN]
        int v2 = maxTotalVoltage.multiply(new BigDecimal(10)).intValue();
        body[30] = (byte) ((v2 >> 8) & 0xFF);
        body[31] = (byte) (v2 & 0xFF);
        // BMS最高允许温度 [1字节] [BIN]
        body[32] = (byte) (maxTemperature + 50);
        // SOC [2字节] [BIN]
        int socVal = soc.multiply(new BigDecimal(10)).intValue();
        body[33] = (byte) ((socVal >> 8) & 0xFF);
        body[34] = (byte) (socVal & 0xFF);
        // BMS当前总电压 [2字节] [BIN]
        int v3 = currentTotalVoltage.multiply(new BigDecimal(10)).intValue();
        body[35] = (byte) ((v3 >> 8) & 0xFF);
        body[36] = (byte) (v3 & 0xFF);
        // 电桩最高输出电压 [2字节] [BIN]
        int v4 = maxOutVoltage.multiply(new BigDecimal(10)).intValue();
        body[37] = (byte) ((v4 >> 8) & 0xFF);
        body[38] = (byte) (v4 & 0xFF);
        // 电桩最低输出电压 [2字节] [BIN]
        int v5 = minOutVoltage.multiply(new BigDecimal(10)).intValue();
        body[39] = (byte) ((v5 >> 8) & 0xFF);
        body[40] = (byte) (v5 & 0xFF);
        // 电桩最大输出电流 [2字节] [BIN]
        int curr2 = maxOutCurrent.multiply(new BigDecimal(10)).add(new BigDecimal(4000)).intValue();
        body[41] = (byte) ((curr2 >> 8) & 0xFF);
        body[42] = (byte) (curr2 & 0xFF);
        // 电桩最小输出电流 [2字节] [BIN]
        int curr3 = minOutCurrent.multiply(new BigDecimal(10)).add(new BigDecimal(4000)).intValue();
        body[43] = (byte) ((curr3 >> 8) & 0xFF);
        body[44] = (byte) (curr3 & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-10 09:52:49
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x17】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 参数配置上传  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 参数配置上传  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 参数配置上传  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 参数配置上传  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 参数配置上传  允许电压    maxAllowVoltage              : %s\n", devLabel, maxAllowVoltage));
        sb.append(String.format("👩‍🚀%s 参数配置上传  允许电流    maxChargeCurrent             : %s\n", devLabel, maxChargeCurrent));
        sb.append(String.format("👩‍🚀%s 参数配置上传  标称能量    ratedEnergy                  : %s\n", devLabel, ratedEnergy));
        sb.append(String.format("👩‍🚀%s 参数配置上传  最高电压    maxTotalVoltage              : %s\n", devLabel, maxTotalVoltage));
        sb.append(String.format("👩‍🚀%s 参数配置上传  最高温度    maxTemperature               : %s\n", devLabel, maxTemperature));
        sb.append(String.format("👩‍🚀%s 参数配置上传  充电率值    SOC                          : %s\n", devLabel, soc));
        sb.append(String.format("👩‍🚀%s 参数配置上传  当前电压    currentTotalVoltage          : %s\n", devLabel, currentTotalVoltage));
        sb.append(String.format("👩‍🚀%s 参数配置上传  最高输压    maxOutVoltage                : %s\n", devLabel, maxOutVoltage));
        sb.append(String.format("👩‍🚀%s 参数配置上传  最低输压    minOutVoltage                : %s\n", devLabel, minOutVoltage));
        sb.append(String.format("👩‍🚀%s 参数配置上传  最大输流    maxOutCurrent                : %s\n", devLabel, maxOutCurrent));
        sb.append(String.format("👩‍🚀%s 参数配置上传  最小输流    minOutCurrent                : %s\n", devLabel, minOutCurrent));
        log.info(sb.toString());
    }
}
