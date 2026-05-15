package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电桩计费模型请求 [0X09]
 *
 * @author KevenPotter
 * @date 2026-04-22 16:54:32
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ADBillingModelReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;

    /* 有参构造 */
    public ADBillingModelReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-22 16:54:47
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 16:45:24
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x09】 {} 充电桩计费模型请求 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x09】 {} 充电桩计费模型请求 设备编号    deviceId             : {}", deviceId, deviceId);
        System.out.println();
    }

}
