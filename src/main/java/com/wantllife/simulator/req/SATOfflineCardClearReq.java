package com.wantllife.simulator.req;

import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.util.HexUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardCard;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 离线卡数据清除 [0X46]
 *
 * @author KevenPotter
 * @date 2026-06-04 10:38:06
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SATOfflineCardClearReq extends FrameHeader {


    /*设备编号*/
    private String deviceId;
    /*清除卡个数*/
    private Integer clearCounts;
    /*卡号编组*/
    private List<StandardCard> cardList = new ArrayList<>();

    /* 有参构造 */
    public SATOfflineCardClearReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-04 10:38:19
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 清除卡个数 [1字节] [BIN]
        this.setClearCounts(data[index] & 0xFF);
        index += 1;
        // 循环解析N张卡
        for (int i = 0; i < clearCounts; i++) {
            StandardCard card = new StandardCard();
            // 物理卡号 [8字节] [BIN]
            byte[] physicalBytes = new byte[8];
            System.arraycopy(data, index, physicalBytes, 0, 8);
            String physical = HexUtil.encodeHexStr(physicalBytes, false).toUpperCase();
            // 去除前导零
            physical = physical.replaceFirst("^0+(?!$)", "");
            card.setPhysicalCardNo(physical);
            index += 8;

            cardList.add(card);
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-04 10:39:43
     */
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("👨‍🚀 【0x46】 {} 电卡数据清除  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("👨‍🚀 【0x46】 {} 电卡数据清除  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("👨‍🚀 【0x46】 {} 电卡数据清除  清除个数    clearCounts                  : {}", PURPLE + deviceId + RESET, clearCounts);

        for (int i = 0; i < clearCounts; i++) {
            StandardCard card = cardList.get(i);
            log.info("👨‍🚀 【0x46】 {} 电卡数据清除  第{}物号    physicalCardNo               : {}", PURPLE + deviceId + RESET, NumberChineseFormatter.format(i, false, false), card.getPhysicalCardNo());
        }
        System.out.println();
    }
}
