package com.wantllife.analysis.req;

import cn.hutool.core.convert.NumberChineseFormatter;
import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 离线卡数据清除应答 [0X45]
 *
 * @author KevenPotter
 * @date 2026-04-28 11:12:03
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ATOfflineCardClearReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*清除结果*/
    private List<ClearResult> clearResultList;


    /* 有参构造 */
    public ATOfflineCardClearReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-28 11:12:55
     */
    private void parseBody(byte[] data) {
        int index = 6;
        clearResultList = new ArrayList<>();
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 循环解析：物理卡号(8) + 清除标记(1) + 失败原因(1)
        while (index + 9 < data.length) {
            ClearResult result = new ClearResult();
            // 物理卡号 [8字节] [BIN]
            byte[] cardBytes = new byte[8];
            System.arraycopy(data, index, cardBytes, 0, 8);
            String cardHex = HexUtil.encodeHexStr(cardBytes).toUpperCase();
            result.setPhysicalCardNo(cardHex.replaceFirst("^0+", ""));
            index += 8;
            // 清除标记 [1字节] [BIN]
            result.setClearResult(data[index++] & 0xFF);
            // 失败原因 [1字节] [BIN]
            int fail = data[index++] & 0xFF;
            result.setFailReason(fail);
            result.setFailReasonDesc(parseFailReasonDesc(fail));
            clearResultList.add(result);
        }
    }


    /**
     * 解析失败原因描述
     *
     * @author KevenPotter
     * @date 2026-04-28 11:13:50
     */
    private static String parseFailReasonDesc(Integer failReason) {
        switch (failReason) {
            case 0x00:
                return "清除成功";
            case 0x01:
                return "卡号格式错误";
            default:
                return "未知原因";
        }
    }

    /**
     * 清除应答结果
     *
     * @author KevenPotter
     * @date 2026-04-28 11:14:36
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
        /*失败原因描述*/
        private String failReasonDesc;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 14:39:49
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x45】 {} 离线卡数据清除应答 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x45】 {} 离线卡数据清除应答 设备编号    deviceId                     : {}", deviceId, deviceId);

        for (int i = 0; i < clearResultList.size(); i++) {
            System.out.println();
            ClearResult clearResult = clearResultList.get(i);
            log.info("🟢 【0x45】 {} 离线卡数据清除应答 第{}物号    physicalCardNo               : {}", deviceId, NumberChineseFormatter.format(i, false, false), clearResult.getPhysicalCardNo());
            log.info("🟢 【0x45】 {} 离线卡数据清除应答 清除标记    clearResult                  : {}", deviceId, clearResult.getClearResult() == 0 ? "清除失败" : "清除成功");
            log.info("🟢 【0x45】 {} 离线卡数据清除应答 失败原因    failReasonDesc               : {}", deviceId, clearResult.getFailReasonDesc());
        }
        System.out.println();
    }

}
