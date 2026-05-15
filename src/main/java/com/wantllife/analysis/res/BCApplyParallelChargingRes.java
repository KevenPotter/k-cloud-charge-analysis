package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.analysis.req.BCApplyParallelChargingReq;
import com.wantllife.domain.vo.StandardChargeOrder;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_APPLY_PARALLEL_CHARGING;


/**
 * 运营平台确认并充启动充电 [0XA2]
 *
 * @author KevenPotter
 * @date 2026-04-29 13:14:56
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BCApplyParallelChargingRes extends FrameHeader {

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


    /**
     * 构建下发指令
     *
     * @param applyParallelChargingReq 充电桩主动申请并充充电
     * @param order                    充电订单
     * @param failureReason            失败原因(默认00)
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-29 13:18:22
     */
    public static byte[] buildCommand(BCApplyParallelChargingReq applyParallelChargingReq, StandardChargeOrder order, String failureReason) {
        BCApplyParallelChargingRes res = new BCApplyParallelChargingRes();
        res.setSeqNo(applyParallelChargingReq.getSeqNo());
        res.setFrameType(DOWN_APPLY_PARALLEL_CHARGING);
        res.setDeviceId(order.getDeviceId());
        res.setGunNo(order.getGunNo());
        res.setTradeNo(order.getTradeNo());
        res.setLogicalCardNo(order.getLogicalCardNo());
        res.setBalance(order.getBalance());
        res.setAuthResult("01");
        res.setFailureReason(failureReason);
        res.setFailureReasonDesc(parseFailureReasonDesc(Integer.parseInt(failureReason, 16)));
        res.setParallelNo(applyParallelChargingReq.getParallelNo());

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
     * @date 2026-04-29 13:18:40
     */
    private byte[] buildBody() {
        byte[] body = new byte[44];
        // 交易流水号 [16字节] [BCD]
        byte[] tradeNoBcd = StringUtil.string2bcd(this.tradeNo);
        System.arraycopy(tradeNoBcd, 0, body, 0, 16);
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 16, 7);
        // 枪号 [1字节] [BCD]
        body[23] = StringUtil.string2bcd(String.format("%02d", this.gunNo))[0];
        // 逻辑卡号 [8字节] [BCD]
        String fullLogicalCard = String.format("%16s", this.logicalCardNo).replace(' ', '0');
        byte[] logicalBcd = StringUtil.string2bcd(fullLogicalCard);
        System.arraycopy(logicalBcd, 0, body, 24, 8);
        // 账户余额 [4字节] [BIN]
        long balanceVal = this.balance.multiply(new BigDecimal(100)).longValue();
        body[32] = (byte) (balanceVal & 0xFF);
        body[33] = (byte) ((balanceVal >> 8) & 0xFF);
        body[34] = (byte) ((balanceVal >> 16) & 0xFF);
        body[35] = (byte) ((balanceVal >> 24) & 0xFF);
        // 鉴权成功标志 [1字节] [BIN]
        body[36] = (byte) Integer.parseInt(this.authResult, 16);
        // 失败原因 [1字节] [BCD]
        body[37] = StringUtil.string2bcd(this.failureReason)[0];
        // 并充序号 [6字节] [BCD]
        byte[] parallelNoBcd = StringUtil.string2bcd(this.parallelNo);
        System.arraycopy(parallelNoBcd, 0, body, 38, 6);
        return body;
    }

    /**
     * 解析失败原因描述
     *
     * @author KevenPotter
     * @date 2026-04-29 13:19:41
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
     * @date 2026-05-11 16:41:44
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 枪口编号    gunNo                : {}", deviceId, gunNo);
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 交易编号    tradeNo              : {}", deviceId, tradeNo);
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 逻辑卡号    logicalCardNo        : {}", deviceId, logicalCardNo);
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 账户余额    balance              : {}", deviceId, balance);
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 鉴权标志    authResult           : {}", deviceId, "00".equals(authResult) ? "失败" : "成功");
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 失败原因    failureReasonDesc    : {}", deviceId, failureReasonDesc);
        log.info("🔶 【0xA2】 {} 运营平台确认并充启动充电 并充序号    parallelNo           : {}", deviceId, parallelNo);
        System.out.println();
    }

}
