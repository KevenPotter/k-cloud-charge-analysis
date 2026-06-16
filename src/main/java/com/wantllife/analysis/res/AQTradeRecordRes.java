package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import com.wantllife.core.FrameHeader;
import com.wantllife.analysis.req.AQTradeRecordReq;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_TRADE_RECORD;
import static com.wantllife.constant.ColorConstants.GREEN;
import static com.wantllife.constant.ColorConstants.RESET;


/**
 * 交易记录确认 [0X40]
 *
 * @author KevenPotter
 * @date 2026-04-27 15:29:50
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AQTradeRecordRes extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*确认结果*/
    private String confirmResult;

    /*设备编号(不参与命令计算,仅提供日志打印)*/
    private String deviceId;


    /**
     * 构建下发指令
     *
     * @param req 交易记录
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-27 15:31:00
     */
    public static byte[] buildCommand(AQTradeRecordReq req) {
        AQTradeRecordRes res = new AQTradeRecordRes();
        res.setSeqNo(req.getSeqNo());
        res.setFrameType(DOWN_TRADE_RECORD);
        res.setTradeNo(req.getTradeNo());
        res.setConfirmResult("00");
        res.setDeviceId(req.getDeviceId());

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, true);

        // 记录日志
        if (CloudChargeHolder.isAnalysisLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-27 15:31:09
     */
    private byte[] buildBody() {
        byte[] body = new byte[17];
        // 交易流水号 [16字节] [BCD]
        byte[] tradeNoBcd = StringUtil.string2bcd(this.tradeNo);
        System.arraycopy(tradeNoBcd, 0, body, 0, 16);
        // 确认结果 [1字节] [BIN]
        body[16] = (byte) Integer.parseInt(this.confirmResult, 16);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-19 14:27:15
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🔶 【0x40】 {} 交易记录确认  原始报文    rawMsg                       : {}", GREEN + deviceId + RESET, rawHexMsg);
        log.info("🔶 【0x40】 {} 交易记录确认  交易编号    tradeNo                      : {}", GREEN + deviceId + RESET, tradeNo);
        log.info("🔶 【0x40】 {} 交易记录确认  确认结果    confirmResult                : {}", GREEN + deviceId + RESET, "00".equals(confirmResult) ? "上传成功" : "非法账单");
        System.out.println();
    }

}
