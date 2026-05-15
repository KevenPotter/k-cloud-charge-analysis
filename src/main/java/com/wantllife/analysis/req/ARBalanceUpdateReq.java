package com.wantllife.analysis.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 余额更新应答 [0X41]
 *
 * @author KevenPotter
 * @date 2026-04-27 16:13:18
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ARBalanceUpdateReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*物理卡号*/
    private String physicalCardNo;
    /*修改结果*/
    private Integer updateResult;
    /*修改结果描述*/
    private String updateResultDesc;


    /* 有参构造 */
    public ARBalanceUpdateReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-27 16:13:40
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 物理卡号 [8字节] [BIN]
        byte[] cardBytes = new byte[8];
        System.arraycopy(data, index, cardBytes, 0, 8);
        String fullCardHex = HexUtil.encodeHexStr(cardBytes).toUpperCase();
        this.physicalCardNo = fullCardHex.replaceFirst("^0+", "");
        index += 8;
        // 修改结果 [1字节] [BIN]
        this.updateResult = data[index] & 0xFF;
        this.updateResultDesc = parseUpdateResultDesc(this.updateResult);
    }


    /**
     * 解析修改结果描述
     *
     * @author KevenPotter
     * @date 2026-04-27 16:17:20
     */
    private static String parseUpdateResultDesc(Integer updateResult) {
        switch (updateResult) {
            case 0x00:
                return "修改成功";
            case 0x01:
                return "设备编号错误";
            case 0x02:
                return "卡号错误";
            default:
                return "未知原因";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 14:06:42
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x41】 {} 余额更新应答 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x41】 {} 余额更新应答 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x41】 {} 余额更新应答 物理卡号    physicalCardNo               : {}", deviceId, physicalCardNo);
        log.info("🟢 【0x41】 {} 余额更新应答 修改结果    updateResultDesc             : {}", deviceId, updateResultDesc);
        System.out.println();
    }

}
