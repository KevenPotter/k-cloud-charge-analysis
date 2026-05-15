package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_REAL_TIME_MONITOR;


/**
 * 读取实时监测数据 [0X12]
 *
 * @author KevenPotter
 * @date 2026-04-24 10:05:33
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AERealTimeMonitorRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;

    /**
     * 构建下发指令
     *
     * @param deviceId 设备编号
     * @param gunNo    枪号
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-24 10:06:32
     */
    public static byte[] buildCommand(String deviceId, Integer gunNo) {
        AERealTimeMonitorRes res = new AERealTimeMonitorRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_REAL_TIME_MONITOR);
        res.setDeviceId(deviceId);
        res.setGunNo(gunNo);

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
     * @date 2026-04-22 17:02:00
     */
    private byte[] buildBody() {
        byte[] body = new byte[8];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BCD]
        body[7] = this.gunNo.byteValue();
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 21:08:09
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x12】 {} 读取实时监测数据 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x12】 {} 读取实时监测数据 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x12】 {} 读取实时监测数据 枪口编号    gunNo                : {}", deviceId, gunNo);
        System.out.println();
    }

}
