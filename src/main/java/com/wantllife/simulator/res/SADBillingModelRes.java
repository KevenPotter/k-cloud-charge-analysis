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

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_BILLING_MODE;

/**
 * 充电桩计费模型请求 [0X09]
 *
 * @author KevenPotter
 * @date 2026-05-29 09:56:31
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SADBillingModelRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;

    /**
     * 构建下发指令
     *
     * @param deviceId 设备编号
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-05-29 09:57:35
     */
    public static byte[] buildCommand(String deviceId) {
        SADBillingModelRes res = new SADBillingModelRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_BILLING_MODE);
        res.setDeviceId(deviceId);

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, false);

        // 记录日志
        if (CloudChargeHolder.isLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-05-29 09:57:59
     */
    private byte[] buildBody() {
        byte[] body = new byte[7];
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-29 10:00:20
     */
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x09】 {} 计费模型请求  原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🚀 【0x09】 {} 计费模型请求  设备编号    deviceId                     : {}", deviceId, deviceId);
        System.out.println();
    }
}
