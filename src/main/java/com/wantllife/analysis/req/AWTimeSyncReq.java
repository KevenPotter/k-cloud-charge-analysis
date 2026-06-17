package com.wantllife.analysis.req;

import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.ColorConstants.GREEN;
import static com.wantllife.constant.ColorConstants.RESET;
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
        if (CloudChargeHolder.isAnalysisLogOutput()) log(rawHexMsg);
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
     * @date 2026-05-19 14:19:05
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = GREEN + "⇑ 【0x55】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("🟢%s 对时设置应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("🟢%s 对时设置应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("🟢%s 对时设置应答  当前时间    currentTime                  : %s\n", devLabel, currentTime));
        log.info(sb.toString());
    }

}
