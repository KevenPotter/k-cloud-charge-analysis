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
 * 计费模型验证请求应答 [0X06]
 *
 * @author KevenPotter
 * @date 2026-05-28 16:54:16
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SACBillingModeValidReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*计费模型编码*/
    private Integer billingModeId;
    /*验证结果*/
    private Integer billingModeValidResult;

    /* 有参构造 */
    public SACBillingModeValidReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-05-28 16:55:32
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 计费模型编码 [2字节] [BCD]
        this.setBillingModeId(Integer.parseInt(StringUtil.bcd2String(data, index, 2)));
        index += 2;
        // 3. 验证结果 [1字节][BIN]
        this.setBillingModeValidResult(data[index++] & 0xFF);
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-28 16:56:07
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇑ 【0x06】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 计费验证应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 计费验证应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 计费验证应答  计费编码    billingModeId                : %s\n", devLabel, billingModeId));
        sb.append(String.format("👩‍🚀%s 计费验证应答  验证结果    billingModeValidResult       : %s\n", devLabel, billingModeValidResult == 0 ? "一致" : "不一致"));
        log.info(sb.toString());
    }
}
