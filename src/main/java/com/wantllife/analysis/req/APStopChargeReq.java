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
 * 远程停机命令回复 [0X35]
 *
 * @author KevenPotter
 * @date 2026-04-27 13:56:09
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class APStopChargeReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*停止结果*/
    private Integer stopResult;
    /*失败原因*/
    private Integer failureReason;
    /*失败原因描述*/
    private String failureReasonDesc;


    /* 有参构造 */
    public APStopChargeReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-27 13:56:55
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BIN]
        this.gunNo = data[index++] & 0xFF;
        // 停止结果 [1字节] [BIN]
        this.stopResult = data[index++] & 0xFF;
        // 失败原因 [1字节] [BIN]
        this.failureReason = data[index++] & 0xFF;
        this.failureReasonDesc = parseFailureReasonDesc(this.failureReason);
    }

    /**
     * 解析失败原因描述
     *
     * @author KevenPotter
     * @date 2026-04-27 13:57:27
     */
    private String parseFailureReasonDesc(int failureReason) {
        switch (failureReason) {
            case 0x00:
                return "无";
            case 0x01:
                return "设备编号不匹配";
            case 0x02:
                return "枪未处于充电状态";
            case 0x03:
                return "其他";
            default:
                return "未知原因";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-19 14:17:20
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(4096);
        String devLabel = GREEN + "⇑ 【0x35】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("🟢%s 远程关电回复  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("🟢%s 远程关电回复  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("🟢%s 远程关电回复  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("🟢%s 远程关电回复  停止结果    stopResult                   : %s\n", devLabel, stopResult == 0 ? "停止失败" : "停止成功"));
        sb.append(String.format("🟢%s 远程关电回复  失败原因    failureReasonDesc            : %s\n", devLabel, failureReasonDesc));
        log.info(sb.toString());
    }

}
