package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SAVWorkingParamsReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_WORKING_PARAMS;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电桩工作参数设置应答 [0X51]
 *
 * @author KevenPotter
 * @date 2026-06-04 14:33:35
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAVWorkingParamsRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*设置结果*/
    private Integer setResult;

    /**
     * 构建下发指令
     *
     * @param workingParamsReq 充电桩工作参数设置
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-04 14:35:07
     */
    public static byte[] buildCommand(SAVWorkingParamsReq workingParamsReq) {
        SAVWorkingParamsRes res = new SAVWorkingParamsRes();
        res.setSeqNo(workingParamsReq.getSeqNo());
        res.setFrameType(SIM_DOWN_WORKING_PARAMS);
        res.setDeviceId(workingParamsReq.getDeviceId());
        res.setSetResult(1);

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
     * @date 2026-06-04 14:35:41
     */
    private byte[] buildBody() {
        byte[] body = new byte[8];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        // 设置结果 [1字节] [BIN]
        body[7] = (byte) (setResult & 0xFF);

        return body;
    }


    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-04 14:36:30
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x51】 {} 工作参数设置  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x51】 {} 工作参数设置  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x51】 {} 工作参数设置  设置结果    setResult                    : {}", PURPLE + deviceId + RESET, setResult == 0 ? "设置失败" : "设置成功");
        System.out.println();
    }
}
