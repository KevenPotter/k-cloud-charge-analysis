package com.wantllife.simulator.req;

import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 运营平台确认并充启动充电 [0XA2]
 *
 * @author KevenPotter
 * @date 2026-06-16 10:20:06
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SBCApplyParallelChargingReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*交易流水号*/
    private String tradeNo;
    /*逻辑卡号*/
    private String logicalCardNo;
    /*账户余额*/
    private BigDecimal balance;
    /*鉴权成功标志*/
    private String authResult;
    /*失败原因*/
    private String failureReason;
    /*失败原因描述*/
    private String failureReasonDesc;
    /*并充序号*/
    private String parallelNo;

    /* 有参构造 */
    public SBCApplyParallelChargingReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-16 10:20:19
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        String tradeRaw = StringUtil.bcd2String(data, index, 16);
        this.setTradeNo(tradeRaw.replaceFirst("^0+(?!$)", ""));
        index += 16;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BCD]
        String gunStr = StringUtil.bcd2String(data, index, 1);
        this.setGunNo(Integer.parseInt(gunStr));
        index += 1;
        // 逻辑卡号 [8字节] [BCD]
        String logicRaw = StringUtil.bcd2String(data, index, 8);
        this.setLogicalCardNo(logicRaw.replaceFirst("^0+(?!$)", ""));
        index += 8;
        // 账户余额 [4字节] [BIN]
        long balanceRaw = (long) (data[index] & 0xFF)
                | ((long) (data[index + 1] & 0xFF) << 8)
                | ((long) (data[index + 2] & 0xFF) << 16)
                | ((long) (data[index + 3] & 0xFF) << 24);
        this.setBalance(BigDecimal.valueOf(balanceRaw, 2).setScale(2, RoundingMode.HALF_UP));
        index += 4;
        // 鉴权成功标志 [1字节] [BIN]
        int authByte = data[index] & 0xFF;
        this.setAuthResult(String.format("%02X", authByte));
        index += 1;
        // 失败原因 [1字节] [BCD]
        String failCode = StringUtil.bcd2String(data, index, 1);
        this.setFailureReason(failCode);
        this.setFailureReasonDesc(parseFailureReasonDesc(Integer.parseInt(failCode, 16)));
        index += 1;
        // 并充序号 [6字节] [BCD]
        String parallelRaw = StringUtil.bcd2String(data, index, 6);
        this.setParallelNo(parallelRaw.replaceFirst("^0+(?!$)", ""));
        index += 6;
    }

    /**
     * 解析失败原因描述
     *
     * @author KevenPotter
     * @date 2026-06-16 10:21:47
     */
    private static String parseFailureReasonDesc(Integer failureReason) {
        switch (failureReason) {
            case 0x00:
                return "开电成功";
            case 0x01:
                return "账户不存在";
            case 0x02:
                return "账户冻结";
            case 0x03:
                return "账户余额不足";
            case 0x04:
                return "该卡存在未结账记录";
            case 0x05:
                return "桩停用";
            case 0x06:
                return "该账户不能在此桩上充电";
            case 0x07:
                return "密码错误";
            case 0x08:
                return "电站电容不足";
            case 0x09:
                return "系统中vin码不存在";
            case 0x0A:
                return "该桩存在未结账记录";
            case 0x0B:
                return "该桩不支持刷卡";
            default:
                return "未知原因";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-16 10:20:35
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  交易编号    tradeNo                      : {}", PURPLE + deviceId + RESET, tradeNo);
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  逻辑卡号    logicalCardNo                : {}", PURPLE + deviceId + RESET, logicalCardNo);
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  账户余额    balance                      : {}", PURPLE + deviceId + RESET, balance);
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  鉴权标志    authResult                   : {}", PURPLE + deviceId + RESET, "00".equals(authResult) ? "失败" : "成功");
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  失败原因    failureReasonDesc            : {}", PURPLE + deviceId + RESET, failureReasonDesc);
        log.info("👨‍🚀 【0xA2】 {} 确认并充开电  并充序号    parallelNo                   : {}", PURPLE + deviceId + RESET, parallelNo);
        System.out.println();
    }
}
