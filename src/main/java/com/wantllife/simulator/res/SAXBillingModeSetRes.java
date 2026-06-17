package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SAXBillingModeSetReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_BILLING_MODE_SET;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 计费模型应答 [0X57]
 *
 * @author KevenPotter
 * @date 2026-06-05 10:05:05
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAXBillingModeSetRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*设置结果*/
    private Integer setResult;

    /**
     * 构建下发指令
     *
     * @param billingModeSetReq 计费模型设置
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-05 10:05:19
     */
    public static byte[] buildCommand(SAXBillingModeSetReq billingModeSetReq) {
        SAXBillingModeSetRes res = new SAXBillingModeSetRes();
        res.setSeqNo(billingModeSetReq.getSeqNo());
        res.setFrameType(SIM_DOWN_BILLING_MODE_SET);
        res.setDeviceId(billingModeSetReq.getDeviceId());
        res.setSetResult(1);

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
     * @date 2026-06-05 10:05:30
     */
    private byte[] buildBody() {
        byte[] body = new byte[8];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        // 设置结果 [1字节] [BIN]
        body[7] = (byte) (setResult & 0xFF);

        return body;
    }


    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-05 10:06:22
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x57】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 计费模型应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 计费模型应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 计费模型应答  设置结果    setResult                    : %s\n", devLabel, setResult == 0 ? "设置失败" : "设置成功"));
        log.info(sb.toString());
    }
}
