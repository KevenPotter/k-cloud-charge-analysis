package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardChargingBMSStop;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_CHARGING_BMS_STOP;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电阶段BMS中止 [0X1D]
 *
 * @author KevenPotter
 * @date 2026-06-10 11:46:04
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAJChargingBMSStopRes extends FrameHeader {

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** BMS中止充电原因 */
    private String bmsStopReason;
    /** BMS中止充电故障原因 */
    private String bmsStopFailure;
    /** BMS中止充电错误原因 */
    private String bmsStopErrorReason;

    /**
     * 构建下发指令
     *
     * @param standardChargingBMSStop 充电阶段BMS中止
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-10 11:46:25
     */
    public static byte[] buildCommand(StandardChargingBMSStop standardChargingBMSStop) {
        SAJChargingBMSStopRes res = new SAJChargingBMSStopRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_CHARGING_BMS_STOP);
        res.setTradeNo(standardChargingBMSStop.getTradeNo());
        res.setDeviceId(standardChargingBMSStop.getDeviceId());
        res.setGunNo(standardChargingBMSStop.getGunNo());
        res.setBmsStopReason(standardChargingBMSStop.getBmsStopReason());
        res.setBmsStopFailure(standardChargingBMSStop.getBmsStopFailure());
        res.setBmsStopErrorReason(standardChargingBMSStop.getBmsStopErrorReason());

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
     * @date 2026-06-10 11:46:50
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
        // BMS中止充电原因 [1字节] [BIN]
        body[24] = (byte) Integer.parseInt(bmsStopReason, 2);
        // BMS中止充电故障原因 [2字节] [BIN]
        int failure = Integer.parseInt(bmsStopFailure, 2);
        body[25] = (byte) ((failure >> 8) & 0xFF);
        body[26] = (byte) (failure & 0xFF);
        // BMS中止充电错误原因 [1字节] [BIN]
        body[27] = (byte) Integer.parseInt(bmsStopErrorReason, 2);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-10 11:48:05
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x1D】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 电池管理中止  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 电池管理中止  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 电池管理中止  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 电池管理中止  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 电池管理中止  中止原因    bmsStopReason                : %s\n", devLabel, bmsStopReason));
        sb.append(String.format("👩‍🚀%s 电池管理中止  故障原因    bmsStopFailure               : %s\n", devLabel, bmsStopFailure));
        sb.append(String.format("👩‍🚀%s 电池管理中止  错误原因    bmsStopErrorReason           : %s\n", devLabel, bmsStopErrorReason));
        log.info(sb.toString());
    }
}
