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
 * 登录认证应答 [0X02]
 *
 * @author KevenPotter
 * @date 2026-05-28 10:10:29
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAALoginReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*登录结果(0.成功 1.失败)*/
    private Integer loginResult;

    /* 有参构造 */
    public SAALoginReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-05-28 10:11:36
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 登录结果 [1字节] [BIN]
        this.setLoginResult(data[index++] & 0xFF);
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-19 14:21:39
     */
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("👨‍🚀 【0x02】 {} 登录认证应答  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("👨‍🚀 【0x02】 {} 登录认证应答  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("👨‍🚀 【0x02】 {} 登录认证应答  登录结果    loginResult                  : {}", PURPLE + deviceId + RESET, loginResult == 0 ? "成功" : "失败");
        System.out.println();
    }
}
