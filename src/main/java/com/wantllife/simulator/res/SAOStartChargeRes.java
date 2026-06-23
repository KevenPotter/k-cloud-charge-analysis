package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SAOStartChargeReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_START_CHARGE;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 远程启动充电命令回复 [0X33]
 *
 * @author KevenPotter
 * @date 2026-06-02 11:39:32
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAOStartChargeRes extends FrameHeader {

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

    /**
     * 构建下发指令
     *
     * @param startChargeReq 运营平台远程控制启机
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-02 11:39:59
     */
    public static byte[] buildCommand(SAOStartChargeReq startChargeReq, Integer startupResult, Integer failureReason) {
        SAOStartChargeRes res = new SAOStartChargeRes();
        res.setSeqNo(startChargeReq.getSeqNo());
        res.setFrameType(SIM_DOWN_START_CHARGE);
        res.setTradeNo(startChargeReq.getTradeNo());
        res.setDeviceId(startChargeReq.getDeviceId());
        res.setGunNo(startChargeReq.getGunNo());
        res.setStartupResult(startupResult);
        res.setFailureReason(failureReason);

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
     * @date 2026-06-02 11:40:35
     */
    private byte[] buildBody() {
        byte[] body = new byte[26];
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

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-02 11:45:10
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x33】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 远程开电回复  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 远程开电回复  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 远程开电回复  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 远程开电回复  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 远程开电回复  启动结果    startupResult                : %s\n", devLabel, startupResult == 0 ? "启动失败" : "启动成功"));
        sb.append(String.format("👩‍🚀%s 远程开电回复  失败原因    failureReason                : %s\n", devLabel, failureReason));
        log.info(sb.toString());
    }
}
