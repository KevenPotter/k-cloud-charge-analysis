package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_REBOOT;
import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.ColorConstants.GREEN;
import static com.wantllife.constant.ColorConstants.RESET;


/**
 * 远程重启 [0X92]
 *
 * @author KevenPotter
 * @date 2026-04-28 16:45:28
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BARebootRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*执行方式(1.立即执行 2.空闲执行)*/
    private Integer execMethod;

    /**
     * 构建下发指令
     *
     * @param deviceId   设备编号
     * @param execMethod 执行方式(1.立即执行 2.空闲执行)
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-28 16:45:39
     */
    public static byte[] buildCommand(String deviceId, Integer execMethod) {
        BARebootRes res = new BARebootRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_REBOOT);
        res.setDeviceId(deviceId);
        res.setExecMethod(execMethod);

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, true);

        // 记录日志
        if (CloudChargeHolder.isAnalysisLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-28 16:46:47
     */
    private byte[] buildBody() {
        byte[] body = new byte[8];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 执行控制方式 [1字节] [BIN] 0x01=立即执行 0x02=空闲执行
        body[7] = (byte) (this.execMethod & 0xFF);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-19 14:30:55
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = GREEN + "⇓ 【0x92】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("🟠%s 远程重启操作  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("🟠%s 远程重启操作  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("🟠%s 远程重启操作  执行方式    execMethod                   : %s\n", devLabel, execMethod == 1 ? "立即执行" : "空闲执行"));
        log.info(sb.toString());
    }

}
