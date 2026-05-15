package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_TIME_SYNC;
import static com.wantllife.util.TimeUtil.transformCP56Time;


/**
 * 对时设置 [0X56]
 *
 * @author KevenPotter
 * @date 2026-04-28 13:51:07
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AWTimeSyncRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*当前时间*/
    private String currentTime;


    /**
     * 构建下发指令
     *
     * @param deviceId    设备编号
     * @param currentTime 当前时间
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-28 13:51:17
     */
    public static byte[] buildCommand(String deviceId, String currentTime) {
        AWTimeSyncRes res = new AWTimeSyncRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_TIME_SYNC);
        res.setDeviceId(deviceId);
        res.setCurrentTime(currentTime);

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body);

        // 记录日志
        res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-28 13:51:51
     */
    private byte[] buildBody() {
        byte[] body = new byte[14];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 当前时间 [7字节] [BIN] CP56Time2a
        String cp56Hex = transformCP56Time(this.currentTime);
        byte[] timeBytes = HexUtil.decodeHex(cp56Hex);
        System.arraycopy(timeBytes, 0, body, 7, 7);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 15:11:25
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x56】 {} 对时设置 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x56】 {} 对时设置 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x56】 {} 对时设置 当前时间    currentTime          : {}", deviceId, currentTime);
        System.out.println();
    }

}
