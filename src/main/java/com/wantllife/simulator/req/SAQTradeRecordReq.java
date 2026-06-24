package com.wantllife.simulator.req;

import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 交易记录确认 [0X40]
 *
 * @author KevenPotter
 * @date 2026-06-12 16:13:17
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAQTradeRecordReq extends FrameHeader {

    /** 交易流水号 */
    private String tradeNo;
    /** 确认结果 */
    private String confirmResult;

    /** 设备编号(不参与命令计算,仅提供日志打印) */
    private String deviceId;

    /* 有参构造 */
    public SAQTradeRecordReq(byte[] data, String rawHexMsg, String deviceId) {
        this.deviceId = deviceId;
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        if (CloudChargeHolder.isSimulatorLogOutput()) log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-06-12 16:13:29
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        String tradeRaw = StringUtil.bcd2String(data, index, 16);
        this.setTradeNo(tradeRaw.replaceFirst("^0+(?!$)", ""));
        index += 16;
        // 确认结果 [1字节] [BIN]
        int resultByte = data[index] & 0xFF;
        this.setConfirmResult(String.format("%02X", resultByte));
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-12 16:14:00
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇑ 【0x40】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 交易记录确认  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 交易记录确认  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 交易记录确认  确认结果    confirmResult                : %s\n", devLabel, "00".equals(confirmResult) ? "上传成功" : "非法账单"));
        log.info(sb.toString());
    }
}
