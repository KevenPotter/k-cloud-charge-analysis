package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_BILLING_MODE_VALID;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 计费模型验证请求 [0X05]
 *
 * @author KevenPotter
 * @date 2026-05-28 15:38:26
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SACBillingModeValidRes extends FrameHeader {

    /** 设备编号 */
    private String deviceId;
    /** 计费模型编码 */
    private Long billingModeId;

    /**
     * 构建下发指令
     *
     * @param deviceId      设备编号
     * @param billingModeId 计费模型编码
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-05-28 15:39:05
     */
    public static byte[] buildCommand(String deviceId, Long billingModeId) {
        SACBillingModeValidRes res = new SACBillingModeValidRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_BILLING_MODE_VALID);
        res.setDeviceId(deviceId);
        res.setBillingModeId(billingModeId);

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
     * @date 2026-05-28 15:40:15
     */
    private byte[] buildBody() {
        byte[] body = new byte[9];
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 计费模型编码 [2字节] [BCD]
        String billIdStr = String.valueOf(this.billingModeId);
        String billIdFull = StrUtil.padPre(billIdStr, 4, '0');
        byte[] billBcd = StringUtil.string2bcd(billIdFull);
        System.arraycopy(billBcd, 0, body, 7, 2);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-28 15:40:50
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x05】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 计费模型验证  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 计费模型验证  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 计费模型验证  计费编码    billingModeId                : %s\n", devLabel, billingModeId));
        log.info(sb.toString());
    }
}
