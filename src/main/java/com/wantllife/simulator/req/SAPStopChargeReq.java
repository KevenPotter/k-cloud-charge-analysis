package com.wantllife.simulator.req;

import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 运营平台远程停机 [0X36]
 *
 * @author KevenPotter
 * @date 2026-06-03 09:38:14
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAPStopChargeReq extends FrameHeader {


    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;

    /* 有参构造 */
    public SAPStopChargeReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-03 09:38:38
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BCD]
        this.setGunNo(data[index++] & 0xFF);
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-03 09:40:33
     */
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("👨‍🚀 【0x36】 {} 远程控制停机  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("👨‍🚀 【0x36】 {} 远程控制停机  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("👨‍🚀 【0x36】 {} 远程控制停机  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        System.out.println();
    }
}
