package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.util.TimeUtil.parseCP56Time;

/**
 * 对时设置应答 [0X55]
 *
 * @author KevenPotter
 * @date 2026-04-28 14:03:52
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AWTimeSyncReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*当前时间*/
    private String currentTime;


    /* 有参构造 */
    public AWTimeSyncReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-28 14:04:12
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 当前时间 [7字节] [BIN] CP56Time2a
        byte[] timeBytes = new byte[7];
        System.arraycopy(data, index, timeBytes, 0, 7);
        this.currentTime = parseCP56Time(timeBytes);
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 15:13:41
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x55】 {} 对时设置应答 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x55】 {} 对时设置应答 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x55】 {} 对时设置应答 当前时间    currentTime                  : {}", deviceId, currentTime);
        System.out.println();
    }

}
