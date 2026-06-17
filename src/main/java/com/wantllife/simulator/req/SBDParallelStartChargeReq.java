package com.wantllife.simulator.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 运营平台远程控制并充启机 [0XA4]
 *
 * @author KevenPotter
 * @date 2026-06-16 10:37:41
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SBDParallelStartChargeReq extends FrameHeader {

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

    /* 有参构造 */
    public SBDParallelStartChargeReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-16 10:38:06
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
        // 物理卡号 [8字节] [BIN]
        byte[] phyBuf = new byte[8];
        System.arraycopy(data, index, phyBuf, 0, 8);
        String phyHex = HexUtil.encodeHexStr(phyBuf, false).toUpperCase();
        // 去除前导零
        phyHex = phyHex.replaceFirst("^0+(?!$)", "");
        this.setPhysicalCardNo(phyHex);
        index += 8;
        // 账户余额 [4字节] [BIN]
        long balanceValue = ((long) data[index] & 0xFF)
                | ((long) data[index + 1] & 0xFF) << 8
                | ((long) data[index + 2] & 0xFF) << 16
                | ((long) data[index + 3] & 0xFF) << 24;
        this.setBalance(BigDecimal.valueOf(balanceValue, 2).setScale(2, RoundingMode.HALF_UP));
        index += 4;
        // 并充序号 [6字节] [BCD]
        String parallelRaw = StringUtil.bcd2String(data, index, 6);
        this.setParallelNo(parallelRaw.replaceFirst("^0+(?!$)", ""));
        index += 6;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-16 10:39:38
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇑ 【0xA4】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 远程并充启机  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 远程并充启机  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 远程并充启机  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 远程并充启机  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 远程并充启机  逻辑卡号    logicalCardNo                : %s\n", devLabel, logicalCardNo));
        sb.append(String.format("👩‍🚀%s 远程并充启机  物理卡号    physicalCardNo               : %s\n", devLabel, physicalCardNo));
        sb.append(String.format("👩‍🚀%s 远程并充启机  账户余额    balance                      : %s\n", devLabel, balance));
        sb.append(String.format("👩‍🚀%s 远程并充启机  并充序号    parallelNo                   : %s\n", devLabel, parallelNo));
        log.info(sb.toString());
    }
}
