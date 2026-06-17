package com.wantllife.simulator.res;

import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardCard;
import com.wantllife.simulator.req.SATOfflineCardClearReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_OFFLINE_CARD_CLEAR;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 离线卡数据清除应答 [0X45]
 *
 * @author KevenPotter
 * @date 2026-06-04 10:45:24
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SATOfflineCardClearRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*卡号编组*/
    private List<StandardCard> cardList;
    /*清除结果*/
    private List<ClearResult> clearResultList;

    /**
     * 构建下发指令
     *
     * @param offlineCardClearReq 离线卡数据清除
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-04 10:46:29
     */
    public static byte[] buildCommand(SATOfflineCardClearReq offlineCardClearReq) {
        SATOfflineCardClearRes res = new SATOfflineCardClearRes();
        res.setSeqNo(offlineCardClearReq.getSeqNo());
        res.setFrameType(SIM_DOWN_OFFLINE_CARD_CLEAR);
        res.setDeviceId(offlineCardClearReq.getDeviceId());
        res.setCardList(offlineCardClearReq.getCardList());

        res.clearResultList = new ArrayList<>();
        for (StandardCard card : offlineCardClearReq.getCardList()) {
            ClearResult result = new ClearResult();
            result.setPhysicalCardNo(card.getPhysicalCardNo());
            result.setClearResult(1);
            result.setFailReason(0);
            res.clearResultList.add(result);
        }

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, false);

        // 记录日志
        if (CloudChargeHolder.isSimulatorLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-06-04 10:48:01
     */
    private byte[] buildBody() {
        int bodyLen = 7 + clearResultList.size() * 10;
        byte[] body = new byte[bodyLen];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        int pos = 7;
        for (ClearResult item : clearResultList) {
            // 物理卡号 [8字节] [BIN]
            String cardFull = StrUtil.padPre(item.getPhysicalCardNo(), 16, '0');
            byte[] cardBuf = HexUtil.decodeHex(cardFull);
            System.arraycopy(cardBuf, 0, body, pos, 8);

            body[pos + 8] = (byte) (item.getClearResult() & 0xFF);
            body[pos + 9] = (byte) (item.getFailReason() & 0xFF);

            pos += 10;
        }

        return body;
    }

    /**
     * 清除应答结果
     *
     * @author KevenPotter
     * @date 2026-06-04 10:46:17
     */
    @Data
    @Accessors(chain = true)
    public static
    class ClearResult {
        /*物理卡号*/
        private String physicalCardNo;
        /*清除标记*/
        private Integer clearResult;
        /*失败原因*/
        private Integer failReason;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-04 10:48:54
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(4096);
        String devLabel = PURPLE + "⇓ 【0x45】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 电卡清除应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 电卡清除应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));

        for (int i = 0; i < clearResultList.size(); i++) {
            ClearResult clearResult = clearResultList.get(i);
            String idxStr = NumberChineseFormatter.format(i, false, false);
            sb.append("\n");
            sb.append(String.format("👩‍🚀%s 电卡清除应答  第%s物号    physicalCardNo               : %s\n", devLabel, idxStr, clearResult.getPhysicalCardNo()));
            sb.append(String.format("👩‍🚀%s 电卡清除应答  清除标记    clearResult                  : %s\n", devLabel, clearResult.getClearResult() == 0 ? "清除失败" : "清除成功"));
            sb.append(String.format("👩‍🚀%s 电卡清除应答  失败原因    failReason                   : %s\n", devLabel, clearResult.getFailReason()));
        }
        log.info(sb.toString());
    }
}
