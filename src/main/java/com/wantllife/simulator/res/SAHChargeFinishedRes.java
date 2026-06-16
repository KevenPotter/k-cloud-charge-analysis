package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardChargeFinished;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_CHARGE_FINISHED;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电结束 [0X19]
 *
 * @author KevenPotter
 * @date 2026-06-10 10:52:21
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAHChargeFinishedRes extends FrameHeader {

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

    /**
     * 构建下发指令
     *
     * @param standardChargeFinished 充电结束
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-10 10:52:39
     */
    public static byte[] buildCommand(StandardChargeFinished standardChargeFinished) {
        SAHChargeFinishedRes res = new SAHChargeFinishedRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_CHARGE_FINISHED);
        res.setTradeNo(standardChargeFinished.getTradeNo());
        res.setDeviceId(standardChargeFinished.getDeviceId());
        res.setGunNo(standardChargeFinished.getGunNo());
        res.setStopSoc(standardChargeFinished.getStopSoc());
        res.setMinVoltage(standardChargeFinished.getMinVoltage());
        res.setMaxVoltage(standardChargeFinished.getMaxVoltage());
        res.setMinTemperature(standardChargeFinished.getMinTemperature());
        res.setMaxTemperature(standardChargeFinished.getMaxTemperature());
        res.setChargeTime(standardChargeFinished.getChargeTime());
        res.setOutputEnergy(standardChargeFinished.getOutputEnergy());
        res.setChargeNo(standardChargeFinished.getChargeNo());

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
     * @date 2026-06-10 10:53:17
     */
    private byte[] buildBody() {
        byte[] body = new byte[39];

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
        // BMS中止荷电状态SOC [1字节] [BIN]
        body[24] = (byte) (stopSoc & 0xFF);
        // BMS动力蓄电池单体最低电压 [2字节] [BIN]
        int minV = minVoltage.multiply(new BigDecimal(100)).intValue();
        body[25] = (byte) ((minV >> 8) & 0xFF);
        body[26] = (byte) (minV & 0xFF);
        // BMS动力蓄电池单体最高电压 [2字节] [BIN]
        int maxV = maxVoltage.multiply(new BigDecimal(100)).intValue();
        body[27] = (byte) ((maxV >> 8) & 0xFF);
        body[28] = (byte) (maxV & 0xFF);
        // BMS动力蓄电池最低温度 [1字节] [BIN]
        body[29] = (byte) (minTemperature + 50);
        // BMS动力蓄电池最高温度 [1字节] [BIN]
        body[30] = (byte) (maxTemperature + 50);
        // 电桩累计充电时间 [2字节] [BIN]
        body[31] = (byte) ((chargeTime >> 8) & 0xFF);
        body[32] = (byte) (chargeTime & 0xFF);
        // 电桩输出能量 [2字节] [BIN]
        int energy = outputEnergy.multiply(new BigDecimal(10)).intValue();
        body[33] = (byte) ((energy >> 8) & 0xFF);
        body[34] = (byte) (energy & 0xFF);
        // 电桩充电机编号 [4字节] [BCD]
        byte[] chargeNoBcd = StringUtil.string2bcd(StrUtil.padPre(chargeNo, 8, '0'));
        System.arraycopy(chargeNoBcd, 0, body, 35, 4);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-10 10:53:59
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x19】 {} 充电结束上传  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x19】 {} 充电结束上传  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x19】 {} 充电结束上传  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0x19】 {} 充电结束上传  交易编号    tradeNo                      : {}", PURPLE + deviceId + RESET, tradeNo);
        log.info("🚀 【0x19】 {} 充电结束上传  荷电状态    stopSoc                      : {}", PURPLE + deviceId + RESET, stopSoc);
        log.info("🚀 【0x19】 {} 充电结束上传  最低电压    minVoltage                   : {}", PURPLE + deviceId + RESET, minVoltage);
        log.info("🚀 【0x19】 {} 充电结束上传  最高电压    maxVoltage                   : {}", PURPLE + deviceId + RESET, maxVoltage);
        log.info("🚀 【0x19】 {} 充电结束上传  最低温度    minTemperature               : {}", PURPLE + deviceId + RESET, minTemperature);
        log.info("🚀 【0x19】 {} 充电结束上传  最高温度    maxTemperature               : {}", PURPLE + deviceId + RESET, maxTemperature);
        log.info("🚀 【0x19】 {} 充电结束上传  累充时间    chargeTime                   : {}", PURPLE + deviceId + RESET, chargeTime);
        log.info("🚀 【0x19】 {} 充电结束上传  输出能量    outputEnergy                 : {}", PURPLE + deviceId + RESET, outputEnergy);
        log.info("🚀 【0x19】 {} 充电结束上传  电机编号    chargeNo                     : {}", PURPLE + deviceId + RESET, chargeNo);
        System.out.println();
    }
}
