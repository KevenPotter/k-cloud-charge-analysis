package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 计费模型验证请求 [0X05]
 *
 * @author KevenPotter
 * @date 2026-04-22 16:01:22
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ACBillingModelValidReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*计费模型编码*/
    private Long billingModeId;

    /* 有参构造 */
    public ACBillingModelValidReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-04-22 16:01:17
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 计费模型编码 [2字节] [BCD]
        this.setBillingModeId(Long.parseLong(StringUtil.bcd2String(data, index, 2)));
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 16:36:51
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x05】 {} 计费模型验证请求 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x05】 {} 计费模型验证请求 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🟢 【0x05】 {} 计费模型验证请求 计费编码    billingModeId        : {}", deviceId, billingModeId);
        System.out.println();
    }

}
