package com.wantllife.simulator.req;

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
 * 充电桩工作参数设置 [0X52]
 *
 * @author KevenPotter
 * @date 2026-06-04 14:29:48
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAVWorkingParamsReq extends FrameHeader {


    /*设备编号*/
    private String deviceId;
    /*是否允许工作*/
    private boolean allowWork;
    /*最大允许输出功率*/
    private Integer maxOutputPower;

    /* 有参构造 */
    public SAVWorkingParamsReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-04 14:30:17
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 是否允许工作 [1字节] [BIN]
        this.setAllowWork((data[index] & 0xFF) == 0x00);
        index += 1;
        // 最大允许输出功率 [1字节] [BIN]
        this.setMaxOutputPower(data[index] & 0xFF);
        index += 1;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-04 14:31:05
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇑ 【0x52】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 工作参数设置  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 工作参数设置  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 工作参数设置  允许工作    allowWork                    : %s\n", devLabel, allowWork ? "允许工作" : "停止使用"));
        sb.append(String.format("👩‍🚀%s 工作参数设置  最大输出    maxOutputPower               : %s\n", devLabel, maxOutputPower));
        log.info(sb.toString());
    }
}
