package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_WORKING_PARAMS;


/**
 * 充电桩工作参数设置 [0X52]
 *
 * @author KevenPotter
 * @date 2026-04-28 13:26:18
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AVWorkingParamsRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*是否允许工作*/
    private boolean allowWork;
    /*最大允许输出功率*/
    private Integer maxOutputPower;


    /**
     * 构建下发指令
     *
     * @param deviceId       设备编号
     * @param allowWork      是否允许工作
     * @param maxOutputPower 最大允许输出功率
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-28 13:28:28
     */
    public static byte[] buildCommand(String deviceId, boolean allowWork, Integer maxOutputPower) {
        AVWorkingParamsRes res = new AVWorkingParamsRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_WORKING_PARAMS);
        res.setDeviceId(deviceId);
        res.setAllowWork(allowWork);
        res.setMaxOutputPower(maxOutputPower);

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
     * @date 2026-04-28 13:28:50
     */
    private byte[] buildBody() {
        byte[] body = new byte[9];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 是否允许工作 [1字节] [BIN]
        body[7] = (byte) (allowWork ? 0x00 : 0x01);
        // 最大允许输出功率 [1字节] [BIN]
        body[8] = (byte) (maxOutputPower & 0xFF);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 15:07:09
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x52】 {} 充电桩工作参数设置 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x52】 {} 充电桩工作参数设置 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x52】 {} 充电桩工作参数设置 允许工作    allowWork            : {}", deviceId, allowWork ? "允许工作" : "停止使用");
        log.info("🔶 【0x52】 {} 充电桩工作参数设置 最大输出    maxOutputPower       : {}", deviceId, maxOutputPower);
        System.out.println();
    }

}
