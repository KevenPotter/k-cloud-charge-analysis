package com.wantllife.analysis.req;

import com.wantllife.core.FrameHeader;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.ColorConstants.GREEN;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 远程重启应答 [0X91]
 *
 * @author KevenPotter
 * @date 2026-04-29 10:26:41
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BARebootReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*设置结果*/
    private Integer setResult;


    /* 有参构造 */
    public BARebootReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        if (CloudChargeHolder.isAnalysisLogOutput()) log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-04-29 10:27:50
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 设置结果 [1字节] [BIN] 0=失败 1=成功
        this.setResult = data[index++] & 0xFF;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-19 14:19:57
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🟢 【0x91】 {} 远程重启应答  原始报文    rawMsg                       : {}", GREEN + deviceId + RESET, rawHexMsg);
        log.info("🟢 【0x91】 {} 远程重启应答  设备编号    deviceId                     : {}", GREEN + deviceId + RESET, deviceId);
        log.info("🟢 【0x91】 {} 远程重启应答  设置结果    setResult                    : {}", GREEN + deviceId + RESET, setResult == 0 ? "重启失败" : "重启成功");
        System.out.println();
    }

}
