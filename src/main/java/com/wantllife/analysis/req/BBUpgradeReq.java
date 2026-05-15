package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 远程更新应答 [0X93]
 *
 * @author KevenPotter
 * @date 2026-04-29 13:03:25
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BBUpgradeReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*升级状态*/
    private Integer upgradeStatus;
    /*升级状态描述*/
    private String upgradeStatusDesc;


    /* 有参构造 */
    public BBUpgradeReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-29 13:03:54
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 升级状态 [1字节] [BIN]
        this.upgradeStatus = data[index++] & 0xFF;
        this.upgradeStatusDesc = parseUpgradeStatusDesc(this.upgradeStatus);
    }

    /**
     * 解析升级状态描述
     *
     * @author KevenPotter
     * @date 2026-04-29 13:06:22
     */
    private static String parseUpgradeStatusDesc(Integer upgradeStatus) {
        switch (upgradeStatus) {
            case 0x00:
                return "升级成功";
            case 0x01:
                return "编号错误";
            case 0x02:
                return "程序与桩型号不符";
            case 0x03:
                return "下载更新文件超时";
            default:
                return "未知状态";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 16:35:28
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x93】 {} 远程更新应答 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x93】 {} 远程更新应答 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x93】 {} 远程更新应答 升级状态    upgradeStatusDesc            : {}", deviceId, upgradeStatusDesc);
        System.out.println();
    }

}
