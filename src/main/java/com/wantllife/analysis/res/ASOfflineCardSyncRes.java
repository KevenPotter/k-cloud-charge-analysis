package com.wantllife.analysis.res;

import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.domain.vo.StandardCard;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_OFFLINE_CARD_SYNC;


/**
 * 离线卡数据同步 [0X44]
 *
 * @author KevenPotter
 * @date 2026-04-28 09:46:20
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ASOfflineCardSyncRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*下发卡个数*/
    private Integer cardCounts;
    /*卡号编组*/
    private List<StandardCard> cardList;


    /**
     * 构建下发指令
     *
     * @param deviceId 设备编号
     * @param cardList 卡号编组
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-28 09:48:30
     */
    public static byte[] buildCommand(String deviceId, List<StandardCard> cardList) {
        ASOfflineCardSyncRes res = new ASOfflineCardSyncRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_OFFLINE_CARD_SYNC);
        res.setDeviceId(deviceId);
        res.setCardCounts(cardList.size());
        res.setCardList(cardList);

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body);

        // 记录日志
        res.log(HexUtil.encodeHexStr(downMessage), cardList);

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-28 09:48:56
     */
    private byte[] buildBody() {
        int cardCount = cardList.size();
        int bodyLen = 7 + 1 + (8 + 8) * cardCount;
        byte[] body = new byte[bodyLen];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 下发卡个数 [1字节] [BIN]
        body[7] = (byte) cardCount;
        // 循环写入卡信息
        for (int i = 0; i < cardCount; i++) {
            StandardCard card = cardList.get(i);
            // 每张卡占用 16 字节
            int pos = 8 + i * 16;
            // 逻辑卡号 [8字节] [BCD]
            String fullLogical = String.format("%16s", card.getLogicalCardNo()).replace(' ', '0');
            byte[] logicalBcd = StringUtil.string2bcd(fullLogical);
            System.arraycopy(logicalBcd, 0, body, pos, 8);
            // 物理卡号 [8字节] [BIN]
            String fullPhysical = String.format("%16s", card.getPhysicalCardNo()).replace(' ', '0');
            byte[] physicalBin = HexUtil.decodeHex(fullPhysical);
            System.arraycopy(physicalBin, 0, body, pos + 8, 8);
        }
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 14:24:44
     */
    private void log(String rawHexMsg, List<StandardCard> cardList) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x44】 {} 离线卡数据同步 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x44】 {} 离线卡数据同步 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x44】 {} 离线卡数据同步 下发个数    cardCounts           : {}", deviceId, cardCounts);

        for (int i = 0; i < cardCounts; i++) {
            StandardCard card = cardList.get(i);
            log.info("🔶 【0x44】 {} 离线卡数据同步 第{}逻号    logicalCardNo        : {}", deviceId, NumberChineseFormatter.format(i, false, false), card.getLogicalCardNo());
            log.info("🔶 【0x44】 {} 离线卡数据同步 第{}物号    physicalCardNo       : {}", deviceId, NumberChineseFormatter.format(i, false, false), card.getPhysicalCardNo());
        }
        System.out.println();
    }

}
