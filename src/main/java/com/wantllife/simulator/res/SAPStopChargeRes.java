package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SAPStopChargeReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_STOP_CHARGE;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 远程停机命令回复 [0X35]
 *
 * @author KevenPotter
 * @date 2026-06-03 10:28:15
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAPStopChargeRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*停止结果*/
    private Integer stopResult;
    /*失败原因*/
    private Integer failureReason;

    /**
     * 构建下发指令
     *
     * @param stopChargeReq 运营平台远程停机
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-03 10:29:28
     */
    public static byte[] buildCommand(SAPStopChargeReq stopChargeReq) {
        SAPStopChargeRes res = new SAPStopChargeRes();
        res.setSeqNo(stopChargeReq.getSeqNo());
        res.setFrameType(SIM_DOWN_STOP_CHARGE);
        res.setDeviceId(stopChargeReq.getDeviceId());
        res.setGunNo(stopChargeReq.getGunNo());
        res.setStopResult(1);
        res.setFailureReason(0);

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
     * @date 2026-06-03 10:30:55
     */
    private byte[] buildBody() {
        byte[] body = new byte[10];
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BIN]
        body[7] = (byte) (this.gunNo & 0xFF);
        // 停止结果 [1字节] [BCD]
        body[8] = (byte) (this.stopResult & 0xFF);
        // 失败原因 [1字节] [BIN]
        body[9] = (byte) (this.failureReason & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-03 10:32:29
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x35】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 远程关电回复  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 远程关电回复  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 远程关电回复  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 远程关电回复  停止结果    stopResult                   : %s\n", devLabel, stopResult == 0 ? "停止失败" : "停止成功"));
        sb.append(String.format("👩‍🚀%s 远程关电回复  失败原因    failureReason                : %s\n", devLabel, failureReason));
        log.info(sb.toString());
    }
}
