package com.wantllife.simulator.res;

import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardCard;
import com.wantllife.simulator.req.SAUOfflineCardQueryReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_OFFLINE_CARD_QUERY;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 离线卡数据查询应答 [0X47]
 *
 * @author KevenPotter
 * @date 2026-06-04 13:33:24
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAUOfflineCardQueryRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*卡号编组*/
    private List<StandardCard> cardList;
    /*清除结果*/
    private List<QueryResult> queryResultList;

    /**
     * 构建下发指令
     *
     * @param offlineCardQueryReq 离线卡数据查询
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-04 13:33:44
     */
    public static byte[] buildCommand(SAUOfflineCardQueryReq offlineCardQueryReq) {
        SAUOfflineCardQueryRes res = new SAUOfflineCardQueryRes();
        res.setSeqNo(offlineCardQueryReq.getSeqNo());
        res.setFrameType(SIM_DOWN_OFFLINE_CARD_QUERY);
        res.setDeviceId(offlineCardQueryReq.getDeviceId());
        res.setCardList(offlineCardQueryReq.getCardList());

        res.queryResultList = new ArrayList<>();
        for (StandardCard card : offlineCardQueryReq.getCardList()) {
            QueryResult result = new QueryResult();
            result.setPhysicalCardNo(card.getPhysicalCardNo());
            result.setQueryResult(1);
            res.queryResultList.add(result);
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
     * @date 2026-06-04 13:35:09
     */
    private byte[] buildBody() {
        int bodyLen = 7 + queryResultList.size() * 9;
        byte[] body = new byte[bodyLen];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        int pos = 7;
        for (QueryResult item : queryResultList) {
            // 物理卡号 [8字节] [BIN]
            String cardFull = StrUtil.padPre(item.getPhysicalCardNo(), 16, '0');
            byte[] cardBuf = HexUtil.decodeHex(cardFull);
            System.arraycopy(cardBuf, 0, body, pos, 8);

            // 查询结果 [1字节] [BIN]
            body[pos + 8] = (byte) (item.getQueryResult() & 0xFF);

            pos += 9;
        }

        return body;
    }

    /**
     * 查询应答结果
     *
     * @author KevenPotter
     * @date 2026-06-04 13:35:29
     */
    @Data
    @Accessors(chain = true)
    public static
    class QueryResult {
        /*物理卡号*/
        private String physicalCardNo;
        /*查询结果*/
        private Integer queryResult;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-04 13:36:40
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(4096);
        String devLabel = PURPLE + "⇓ 【0x47】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 电卡查询应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 电卡查询应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));

        for (int i = 0; i < queryResultList.size(); i++) {
            QueryResult queryResult = queryResultList.get(i);
            String idxStr = NumberChineseFormatter.format(i, false, false);
            sb.append("\n");
            sb.append(String.format("👩‍🚀%s 电卡查询应答  第%s物号    physicalCardNo               : %s\n", devLabel, idxStr, queryResult.getPhysicalCardNo()));
            sb.append(String.format("👩‍🚀%s 电卡查询应答  查询结果    queryResult                  : %s\n", devLabel, queryResult.getQueryResult() == 0 ? "卡不存在" : "卡存在"));
        }
        log.info(sb.toString());
    }
}
