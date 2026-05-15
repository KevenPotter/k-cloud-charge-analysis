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
 * 离线卡数据查询应答 [0X47]
 *
 * @author KevenPotter
 * @date 2026-04-28 11:43:43
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AUOfflineCardQueryReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*查询结果*/
    private List<QueryResult> queryResultList;


    /* 有参构造 */
    public AUOfflineCardQueryReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-28 11:44:05
     */
    private void parseBody(byte[] data) {
        int index = 6;
        queryResultList = new ArrayList<>();
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 循环解析：物理卡号(8) + 查询结果(1)
        while (index + 8 < data.length) {
            QueryResult result = new QueryResult();
            // 物理卡号 [8字节] [BIN]
            byte[] cardBytes = new byte[8];
            System.arraycopy(data, index, cardBytes, 0, 8);
            String cardHex = HexUtil.encodeHexStr(cardBytes).toUpperCase();
            result.setPhysicalCardNo(cardHex.replaceFirst("^0+", ""));
            index += 8;
            // 查询结果 [1字节] [BIN]
            int status = data[index++] & 0xFF;
            result.setQueryResult(status);
            result.setQueryResultDesc(parseQueryResultDesc(status));
            queryResultList.add(result);
        }
    }


    /**
     * 解析查询结果描述
     *
     * @author KevenPotter
     * @date 2026-04-28 11:44:18
     */
    private static String parseQueryResultDesc(Integer failReason) {
        switch (failReason) {
            case 0x00:
                return "不存在";
            case 0x01:
                return "存在";
            default:
                return "未知原因";
        }
    }

    /**
     * 查询应答结果
     *
     * @author KevenPotter
     * @date 2026-04-28 11:45:52
     */
    @Data
    @Accessors(chain = true)
    public static
    class QueryResult {
        /*物理卡号*/
        private String physicalCardNo;
        /*查询结果*/
        private Integer queryResult;
        /*查询结果描述*/
        private String queryResultDesc;
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
        log.info("🟢 【0x47】 {} 离线卡数据查询应答 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x47】 {} 离线卡数据查询应答 设备编号    deviceId                     : {}", deviceId, deviceId);

        for (int i = 0; i < queryResultList.size(); i++) {
            System.out.println();
            QueryResult queryResult = queryResultList.get(i);
            log.info("🟢 【0x47】 {} 离线卡数据查询应答 第{}物号    physicalCardNo               : {}", deviceId, NumberChineseFormatter.format(i, false, false), queryResult.getPhysicalCardNo());
            log.info("🟢 【0x47】 {} 离线卡数据查询应答 查询结果    queryResult                  : {}", deviceId, queryResult.getQueryResult() == 0 ? "卡不存在" : "卡存在");
        }
        System.out.println();
    }

}
