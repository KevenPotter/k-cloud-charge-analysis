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

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_OFFLINE_CARD_CLEAR;


/**
 * 离线卡数据清除 [0X46]
 *
 * @author KevenPotter
 * @date 2026-04-28 10:56:26
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ATOfflineCardClearRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*清除卡个数*/
    private Integer clearCounts;
    /*卡号编组*/
    private List<StandardCard> cardList;


    /**
     * 构建下发指令
     *
     * @param deviceId 设备编号
     * @param cardList 卡号编组
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-28 10:57:08
     */
    public static byte[] buildCommand(String deviceId, List<StandardCard> cardList) {
        ATOfflineCardClearRes res = new ATOfflineCardClearRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_OFFLINE_CARD_CLEAR);
        res.setDeviceId(deviceId);
        res.setClearCounts(cardList.size());
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
     * @date 2026-04-28 10:58:30
     */
    private byte[] buildBody() {
        int cardCount = cardList.size();
        int bodyLen = 7 + 1 + 8 * cardCount;
        byte[] body = new byte[bodyLen];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 清除卡个数 [1字节] [BIN]
        body[7] = (byte) cardCount;
        // 循环写入物理卡号
        for (int i = 0; i < cardCount; i++) {
            StandardCard card = cardList.get(i);
            // 每张卡占用 8 字节
            int pos = 8 + i * 8;
            // 物理卡号 [8字节] [BIN]
            String fullPhysical = String.format("%16s", card.getPhysicalCardNo()).replace(' ', '0');
            byte[] physicalBin = HexUtil.decodeHex(fullPhysical);
            System.arraycopy(physicalBin, 0, body, pos, 8);
        }
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 14:36:25
     */
    private void log(String rawHexMsg, List<StandardCard> cardList) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x46】 {} 离线卡数据清除 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x46】 {} 离线卡数据清除 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x46】 {} 离线卡数据清除 清除个数    clearCounts          : {}", deviceId, clearCounts);

        for (int i = 0; i < clearCounts; i++) {
            StandardCard card = cardList.get(i);
            log.info("🔶 【0x46】 {} 离线卡数据清除 第{}物号    physicalCardNo       : {}", deviceId, NumberChineseFormatter.format(i, false, false), card.getPhysicalCardNo());
        }
        System.out.println();
    }

}
