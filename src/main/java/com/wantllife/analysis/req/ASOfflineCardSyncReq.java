package com.wantllife.analysis.req;

import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.ColorConstants.GREEN;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 离线卡数据同步应答 [0X43]
 *
 * @author KevenPotter
 * @date 2026-04-28 10:46:36
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ASOfflineCardSyncReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*保存结果*/
    private Integer saveResult;
    /*失败原因*/
    private Integer failReason;
    /*失败原因描述*/
    private String failReasonDesc;


    /* 有参构造 */
    public ASOfflineCardSyncReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-28 10:47:39
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 保存结果 [1字节] [BIN]
        this.saveResult = data[index++] & 0xFF;
        // 失败原因 [1字节] [BIN]
        this.failReason = data[index++] & 0xFF;
        this.failReasonDesc = parseFailReasonDesc(this.failReason);
    }


    /**
     * 解析失败原因描述
     *
     * @author KevenPotter
     * @date 2026-04-28 10:47:21
     */
    private static String parseFailReasonDesc(Integer failReason) {
        switch (failReason) {
            case 0x00:
                return "无";
            case 0x01:
                return "卡号格式错误";
            case 0x02:
                return "储存空间不足";
            default:
                return "未知原因";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-19 14:18:13
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(4096);
        String devLabel = GREEN + "⇑ 【0x43】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("🟢%s 电卡同步应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("🟢%s 电卡同步应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("🟢%s 电卡同步应答  保存结果    saveResult                   : %s\n", devLabel, saveResult == 0 ? "保存失败" : "保存成功"));
        sb.append(String.format("🟢%s 电卡同步应答  失败原因    failReasonDesc               : %s\n", devLabel, failReasonDesc));
        log.info(sb.toString());
    }

}
