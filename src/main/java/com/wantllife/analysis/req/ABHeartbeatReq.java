package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电桩心跳包 [0X03]
 *
 * @author KevenPotter
 * @date 2026-04-22 15:50:04
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ABHeartbeatReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*枪口状态*/
    private Integer gunStatus;
    /*枪口状态描述*/
    private String gunStatusDesc;

    /* 有参构造 */
    public ABHeartbeatReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-22 15:24:30
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BCD]
        this.setGunNo(data[index++] & 0xFF);
        // 枪口状态 [1字节] [BIN]
        this.setGunStatus(data[index++] & 0xFF);
        this.setGunStatusDesc(parseGunStatusDesc(this.gunStatus));
    }

    /**
     * 获取枪口状态描述
     *
     * @author KevenPotter
     * @date 2026-04-22 15:22:41
     */
    private String parseGunStatusDesc(Integer carrier) {
        if (carrier == null) return "未知";
        switch (carrier) {
            case 0:
                return "正常";
            case 1:
                return "故障";
            default:
                return "未知";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 16:03:30
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x03】 {} 充电桩心跳包 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x03】 {} 充电桩心跳包 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🟢 【0x03】 {} 充电桩心跳包 枪口编号    gunNo                : {}", deviceId, gunNo);
        log.info("🟢 【0x03】 {} 充电桩心跳包 枪口状态    gunStatusDesc        : {}", deviceId, gunStatusDesc);
        System.out.println();
    }

}
