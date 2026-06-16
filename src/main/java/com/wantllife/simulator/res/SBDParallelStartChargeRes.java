package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SBDParallelStartChargeReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_PARALLEL_START_CHARGE;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 远程并充启机命令回复 [0XA3]
 *
 * @author KevenPotter
 * @date 2026-06-16 11:13:09
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SBDParallelStartChargeRes extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*启动结果*/
    private Integer startupResult;
    /*失败原因*/
    private Integer failureReason;
    /*失败原因描述*/
    private String failureReasonDesc;
    /*主辅枪标记*/
    private Integer priAndSecFlag;
    /*主辅枪标记描述*/
    private String priAndSecFlagDesc;
    /*并充序号*/
    private String parallelNo;

    /**
     * 构建下发指令
     *
     * @param parallelStartChargeReq 运营平台远程控制并充启机
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-16 11:13:20
     */
    public static byte[] buildCommand(SBDParallelStartChargeReq parallelStartChargeReq) {
        SBDParallelStartChargeRes res = new SBDParallelStartChargeRes();
        res.setSeqNo(parallelStartChargeReq.getSeqNo());
        res.setFrameType(SIM_DOWN_PARALLEL_START_CHARGE);
        res.setTradeNo(parallelStartChargeReq.getTradeNo());
        res.setDeviceId(parallelStartChargeReq.getDeviceId());
        res.setGunNo(parallelStartChargeReq.getGunNo());
        res.setStartupResult(1);
        res.setFailureReason(0);
        res.setFailureReasonDesc(parseFailureReasonDesc(res.getFailureReason()));
        res.setPriAndSecFlag(1);
        res.setPriAndSecFlagDesc(res.getPriAndSecFlag() == 0 ? "主枪" : "辅枪");
        res.setParallelNo(parallelStartChargeReq.getParallelNo());

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
     * @date 2026-06-16 11:13:50
     */
    private byte[] buildBody() {
        byte[] body = new byte[33];
        // 交易流水号 [16字节] [BCD]
        String tradeNoFull = StrUtil.padPre(this.tradeNo, 32, '0');
        byte[] tradeNoBcd = StringUtil.string2bcd(tradeNoFull);
        System.arraycopy(tradeNoBcd, 0, body, 0, 16);
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 16, 7);
        // 枪号 [1字节] [BIN]
        body[23] = (byte) (this.gunNo & 0xFF);
        // 启动结果 [1字节] [BCD]
        body[24] = (byte) (this.startupResult & 0xFF);
        // 失败原因 [1字节] [BIN]
        body[25] = (byte) (this.failureReason & 0xFF);
        // 主辅枪标记 [1字节] [BIN]
        body[26] = (byte) (this.priAndSecFlag & 0xFF);
        // 并充序号 [6字节] [BCD]
        String parallelFull = StrUtil.padPre(parallelNo, 12, '0');
        byte[] parallelBcd = StringUtil.string2bcd(parallelFull);
        System.arraycopy(parallelBcd, 0, body, 27, 6);

        return body;
    }

    /**
     * 解析失败原因描述
     *
     * @author KevenPotter
     * @date 2026-06-16 11:15:00
     */
    private static String parseFailureReasonDesc(int failureReason) {
        switch (failureReason) {
            case 0x00:
                return "无";
            case 0x01:
                return "设备编号不匹配";
            case 0x02:
                return "枪已在充电";
            case 0x03:
                return "设备故障";
            case 0x04:
                return "设备离线";
            case 0x05:
                return "未插枪";
            default:
                return "未知原因";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-16 11:14:39
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0xA3】 {} 并充开电回复  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0xA3】 {} 并充开电回复  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0xA3】 {} 并充开电回复  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0xA3】 {} 并充开电回复  交易编号    tradeNo                      : {}", PURPLE + deviceId + RESET, tradeNo);
        log.info("🚀 【0xA3】 {} 并充开电回复  启动结果    startupResult                : {}", PURPLE + deviceId + RESET, startupResult == 0 ? "启动失败" : "启动成功");
        log.info("🚀 【0xA3】 {} 并充开电回复  失败原因    failureReasonDesc            : {}", PURPLE + deviceId + RESET, failureReasonDesc);
        log.info("🚀 【0xA3】 {} 并充开电回复  主辅标记    priAndSecFlagDesc            : {}", PURPLE + deviceId + RESET, priAndSecFlagDesc);
        log.info("🚀 【0xA3】 {} 并充开电回复  并充序号    parallelNo                   : {}", PURPLE + deviceId + RESET, parallelNo);
        System.out.println();
    }
}
