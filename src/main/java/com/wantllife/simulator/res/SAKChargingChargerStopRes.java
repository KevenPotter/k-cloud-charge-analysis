package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardChargingChargerStop;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_CHARGING_CHARGER_STOP;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电阶段充电机中止 [0X21]
 *
 * @author KevenPotter
 * @date 2026-06-11 09:53:22
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAKChargingChargerStopRes extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*充电机中止充电原因*/
    private String chargerStopReason;
    /*充电机中止充电故障原因*/
    private String chargerStopFailure;
    /*充电机中止充电错误原因*/
    private String chargerStopErrorReason;

    /**
     * 构建下发指令
     *
     * @param standardChargingChargerStop 充电阶段充电机中止
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-11 09:53:49
     */
    public static byte[] buildCommand(StandardChargingChargerStop standardChargingChargerStop) {
        SAKChargingChargerStopRes res = new SAKChargingChargerStopRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_CHARGING_CHARGER_STOP);
        res.setTradeNo(standardChargingChargerStop.getTradeNo());
        res.setDeviceId(standardChargingChargerStop.getDeviceId());
        res.setGunNo(standardChargingChargerStop.getGunNo());
        res.setChargerStopReason(standardChargingChargerStop.getChargerStopReason());
        res.setChargerStopFailure(standardChargingChargerStop.getChargerStopFailure());
        res.setChargerStopErrorReason(standardChargingChargerStop.getChargerStopErrorReason());

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
     * @date 2026-06-11 09:55:09
     */
    private byte[] buildBody() {
        byte[] body = new byte[28];

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
        // 充电机中止充电原因 [1字节] [BIN]
        body[24] = (byte) Integer.parseInt(chargerStopReason, 2);
        // 充电机中止充电故障原因 [2字节] [BIN]
        int failureVal = Integer.parseInt(chargerStopFailure, 2);
        body[25] = (byte) ((failureVal >> 8) & 0xFF);
        body[26] = (byte) (failureVal & 0xFF);
        // 充电机中止充电错误原因 [1字节] [BIN]
        body[27] = (byte) Integer.parseInt(chargerStopErrorReason, 2);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-11 09:55:27
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x21】 {} 充电机器中止  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x21】 {} 充电机器中止  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x21】 {} 充电机器中止  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0x21】 {} 充电机器中止  交易编号    tradeNo                      : {}", PURPLE + deviceId + RESET, tradeNo);
        log.info("🚀 【0x21】 {} 充电机器中止  中止原因    chargerStopReason            : {}", PURPLE + deviceId + RESET, chargerStopReason);
        log.info("🚀 【0x21】 {} 充电机器中止  故障原因    chargerStopFailure           : {}", PURPLE + deviceId + RESET, chargerStopFailure);
        log.info("🚀 【0x21】 {} 充电机器中止  错误原因    chargerStopErrorReason       : {}", PURPLE + deviceId + RESET, chargerStopErrorReason);
        System.out.println();
    }
}
