package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SAWTimeSyncReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_TIME_SYNC;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;
import static com.wantllife.util.TimeUtil.transformCP56Time;

/**
 * 对时设置应答 [0X55]
 *
 * @author KevenPotter
 * @date 2026-06-05 09:30:39
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAWTimeSyncRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*当前时间*/
    private String currentTime;

    /**
     * 构建下发指令
     *
     * @param timeSyncReq 对时设置
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-05 09:32:07
     */
    public static byte[] buildCommand(SAWTimeSyncReq timeSyncReq) {
        SAWTimeSyncRes res = new SAWTimeSyncRes();
        res.setSeqNo(timeSyncReq.getSeqNo());
        res.setFrameType(SIM_DOWN_TIME_SYNC);
        res.setDeviceId(timeSyncReq.getDeviceId());
        res.setCurrentTime(timeSyncReq.getCurrentTime());

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, false);

        // 记录日志
        if (CloudChargeHolder.isSimulatorLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-06-05 09:32:27
     */
    private byte[] buildBody() {
        byte[] body = new byte[14];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        // 当前时间 [7字节] [BIN]
        byte[] timeBytes = HexUtil.decodeHex(transformCP56Time(currentTime));
        System.arraycopy(timeBytes, 0, body, 7, 7);

        return body;
    }


    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-05 09:32:54
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x55】 {} 对时设置应答  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x55】 {} 对时设置应答  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x55】 {} 对时设置应答  当前时间    currentTime                  : {}", PURPLE + deviceId + RESET, currentTime);
        System.out.println();
    }
}
