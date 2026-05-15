package com.wantllife.analysis.res;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Date;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_PARALLEL_START_CHARGE;


/**
 * 运营平台远程控制并充启机 [0XA4]
 *
 * @author KevenPotter
 * @date 2026-04-29 14:27:14
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class BDParallelStartChargeRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*交易流水号*/
    private String tradeNo;
    /*逻辑卡号*/
    private String logicalCardNo;
    /*物理卡号*/
    private String physicalCardNo;
    /*账户余额*/
    private BigDecimal balance;
    /*并充序号*/
    private String parallelNo;


    /**
     * 构建下发指令
     *
     * @param deviceId       设备编号
     * @param gunNo          枪号
     * @param tradeNo        交易流水号
     * @param logicalCardNo  逻辑卡号
     * @param physicalCardNo 物理卡号
     * @param balance        账户余额
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-29 14:27:41
     */
    public static byte[] buildCommand(String deviceId, Integer gunNo, String tradeNo, String logicalCardNo, String physicalCardNo, BigDecimal balance) {
        BDParallelStartChargeRes res = new BDParallelStartChargeRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_PARALLEL_START_CHARGE);
        res.setDeviceId(deviceId);
        res.setGunNo(gunNo);
        res.setTradeNo(tradeNo);
        res.setLogicalCardNo(logicalCardNo);
        res.setPhysicalCardNo(physicalCardNo);
        res.setBalance(balance);
        res.setParallelNo(DateUtil.format(new Date(), "yyMMddHHmmss"));

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
     * @date 2026-04-29 14:28:11
     */
    private byte[] buildBody() {
        byte[] body = new byte[50];
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
        // 物理卡号 [8字节] [BIN]
        String fullPhysicalCard = String.format("%16s", this.physicalCardNo).replace(' ', '0');
        byte[] phyCardBytes = HexUtil.decodeHex(fullPhysicalCard);
        System.arraycopy(phyCardBytes, 0, body, 32, 8);
        // 账户余额 [4字节] [BIN]
        long balanceVal = this.balance.multiply(new BigDecimal(100)).longValue();
        body[40] = (byte) (balanceVal & 0xFF);
        body[41] = (byte) ((balanceVal >> 8) & 0xFF);
        body[42] = (byte) ((balanceVal >> 16) & 0xFF);
        body[43] = (byte) ((balanceVal >> 24) & 0xFF);
        // 并充序号 [6字节] [BCD]
        byte[] parallelNoBcd = StringUtil.string2bcd(this.parallelNo);
        System.arraycopy(parallelNoBcd, 0, body, 44, 6);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 16:43:25
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 枪口编号    gunNo                : {}", deviceId, gunNo);
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 交易编号    tradeNo              : {}", deviceId, tradeNo);
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 逻辑卡号    logicalCardNo        : {}", deviceId, logicalCardNo);
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 物理卡号    physicalCardNo       : {}", deviceId, physicalCardNo);
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 账户余额    balance              : {}", deviceId, balance);
        log.info("🔶 【0xA4】 {} 运营平台远程控制并充启机 并充序号    parallelNo           : {}", deviceId, parallelNo);
        System.out.println();
    }

}
