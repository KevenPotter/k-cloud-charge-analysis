package com.wantllife.simulator.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 遥控地锁升锁与降锁 [0X62]
 *
 * @author KevenPotter
 * @date 2026-06-05 10:19:32
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAZLockUpDownReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*升/降地锁(0.降锁 1.升锁)*/
    private Integer upOrDown;
    /*预留位*/
    private String reserved;

    /* 有参构造 */
    public SAZLockUpDownReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-05 10:20:07
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BIN]
        this.setGunNo(data[index] & 0xFF);
        index += 1;
        // 升/降地锁 [1字节] [BCD]
        byte cmd = data[index];
        this.setUpOrDown(cmd == 0x55 ? 1 : 0);
        index += 1;
        // 预留位 [4字节] [BIN]
        byte[] reservedBytes = new byte[4];
        System.arraycopy(data, index, reservedBytes, 0, 4);
        this.setReserved(HexUtil.encodeHexStr(reservedBytes, false).toUpperCase());
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-05 10:20:25
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇑ 【0x62】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 遥控地锁升降  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 遥控地锁升降  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 遥控地锁升降  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 遥控地锁升降  升降地锁    upOrDown                     : %s\n", devLabel, upOrDown == 0 ? "降锁" : "升锁"));
        sb.append(String.format("👩‍🚀%s 遥控地锁升降  预留位值    reserved                     : %s\n", devLabel, reserved));
        log.info(sb.toString());
    }
}
