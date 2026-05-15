package com.wantllife.analysis.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电桩返回数据 [0X63]
 *
 * @author KevenPotter
 * @date 2026-04-28 16:31:22
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AZLockUpDownReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*地锁控制返回标志*/
    private Integer upDownStatus;
    /*预留位*/
    private String reserved;


    /* 有参构造 */
    public AZLockUpDownReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-28 16:31:39
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BIN]
        this.gunNo = data[index++] & 0xFF;
        // 地锁控制返回标志 [1字节] [BIN] 1=成功 0=失败
        this.upDownStatus = data[index++] & 0xFF;
        // 预留位 [4字节] [BIN]
        byte[] reserveBytes = new byte[4];
        System.arraycopy(data, index, reserveBytes, 0, 4);
        this.reserved = HexUtil.encodeHexStr(reserveBytes).toUpperCase();
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 16:27:29
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x63】 {} 充电桩返回数据 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x63】 {} 充电桩返回数据 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x63】 {} 充电桩返回数据 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x63】 {} 充电桩返回数据 控制标志    upDownStatus                 : {}", deviceId, upDownStatus == 1 ? "鉴权成功" : "鉴权失败");
        log.info("🟢 【0x63】 {} 充电桩返回数据 预留位值    reserved                     : {}", deviceId, reserved);
        System.out.println();
    }

}
