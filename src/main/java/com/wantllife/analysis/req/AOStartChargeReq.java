package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 远程启动充电命令回复 [0X33]
 *
 * @author KevenPotter
 * @date 2026-04-27 13:19:05
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AOStartChargeReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*启动结果*/
    private Integer startupResult;
    /*失败原因*/
    private Integer failureReason;
    /*失败原因描述*/
    private String failureReasonDesc;


    /* 有参构造 */
    public AOStartChargeReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-27 13:19:30
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        this.tradeNo = StringUtil.bcd2String(data, index, 16);
        index += 16;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BCD]
        this.gunNo = Integer.parseInt(StringUtil.bcd2String(data, index, 1));
        index += 1;
        // 启动结果 [1字节] [BCD]
        this.startupResult = Integer.parseInt(StringUtil.bcd2String(data, index, 1));
        index += 1;
        // 失败原因 [1字节] [BIN]
        this.failureReason = data[index++] & 0xFF;
        this.failureReasonDesc = parseFailureReasonDesc(this.failureReason);
    }

    /**
     * 解析失败原因描述
     *
     * @author KevenPotter
     * @date 2026-04-27 13:19:59
     */
    private String parseFailureReasonDesc(int failureReason) {
        switch (failureReason) {
            case 0x00:
                return "无";
            case 0x01:
                return "设备编号不匹配";
            case 0x02:
                return "枪已在充电";
            case 0x03:
                return "设备故障";
            case 0x04:
                return "设备离线";
            case 0x05:
                return "未插枪";
            default:
                return "未知原因";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 14:02:05
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x33】 {} 远程启动充电命令回复 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x33】 {} 远程启动充电命令回复 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x33】 {} 远程启动充电命令回复 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x33】 {} 远程启动充电命令回复 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x33】 {} 远程启动充电命令回复 启动结果    startupResult                : {}", deviceId, startupResult == 0 ? "启动失败" : "启动成功");
        log.info("🟢 【0x33】 {} 远程启动充电命令回复 失败原因    failureReasonDesc            : {}", deviceId, failureReasonDesc);
        System.out.println();
    }

}
