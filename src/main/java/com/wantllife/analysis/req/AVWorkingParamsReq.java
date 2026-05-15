package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电桩工作参数设置应答 [0X51]
 *
 * @author KevenPotter
 * @date 2026-04-28 13:37:05
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AVWorkingParamsReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*设置结果*/
    private Integer setResult;


    /* 有参构造 */
    public AVWorkingParamsReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-28 13:37:11
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 设置结果 [1字节] [BIN]
        this.setResult = data[index++] & 0xFF;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 15:09:54
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x51】 {} 充电桩工作参数设置应答 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x51】 {} 充电桩工作参数设置应答 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x51】 {} 充电桩工作参数设置应答 设置结果    setResult                    : {}", deviceId, setResult == 0 ? "设置失败" : "设置成功");
        System.out.println();
    }

}
