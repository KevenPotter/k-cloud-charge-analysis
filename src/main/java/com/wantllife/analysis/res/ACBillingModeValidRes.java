package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.analysis.req.ACBillingModelValidReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_BILLING_MODE_VALID;


/**
 * 计费模型验证请求应答 [0X06]
 *
 * @author KevenPotter
 * @date 2026-04-22 16:05:58
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ACBillingModeValidRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*计费模型编码*/
    private String billingModeId;
    /*验证结果*/
    private String billingModeValidResult;

    /**
     * 构建下发指令
     *
     * @param req 充电桩心跳包
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-22 16:06:26
     */
    public static byte[] buildCommand(ACBillingModelValidReq req, String billingModeId, boolean validResult) {
        ACBillingModeValidRes res = new ACBillingModeValidRes();
        res.setSeqNo(req.getSeqNo());
        res.setFrameType(DOWN_BILLING_MODE_VALID);
        res.setDeviceId(req.getDeviceId());
        res.setBillingModeId(billingModeId);
        res.setBillingModeValidResult(validResult ? "00" : "01");

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body);

        // 记录日志
        res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }


    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-22 16:06:54
     */
    private byte[] buildBody() {
        byte[] body = new byte[10];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 计费模型编码 [2字节] [BCD]
        byte[] billingModeBcd = StringUtil.string2bcd(this.billingModeId);
        System.arraycopy(billingModeBcd, 0, body, 7, 2);
        // 验证结果 [1字节] [BIN]
        body[9] = Byte.parseByte(this.billingModeValidResult);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 16:37:00
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x06】 {} 计费模型验证请求应答 原始报文    rawMsg                 : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x06】 {} 计费模型验证请求应答 设备编号    deviceId               : {}", deviceId, deviceId);
        log.info("🔶 【0x06】 {} 计费模型验证请求应答 计费编码    billingModeId          : {}", deviceId, billingModeId);
        log.info("🔶 【0x06】 {} 计费模型验证请求应答 验证结果    billingModeValidResult : {}", deviceId, "00".equals(billingModeValidResult) ? "一致" : "不一致");
        System.out.println();
    }
}
