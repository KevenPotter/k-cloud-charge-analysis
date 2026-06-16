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

import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 运营平台远程控制启机 [0X34]
 *
 * @author KevenPotter
 * @date 2026-06-02 10:40:18
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAOStartChargeReq extends FrameHeader {


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

    /* 有参构造 */
    public SAOStartChargeReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-02 10:41:35
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        this.setTradeNo(StringUtil.bcd2String(data, index, 16));
        index += 16;
        // 桩编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BCD]
        this.setGunNo(Integer.parseInt(StringUtil.bcd2String(data, index, 1)));
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
        // 账户余额 [4字节] [BIN] 小端模式，保留两位小数
        long balanceValue = ((long) data[index] & 0xFF)
                | ((long) data[index + 1] & 0xFF) << 8
                | ((long) data[index + 2] & 0xFF) << 16
                | ((long) data[index + 3] & 0xFF) << 24;
        this.setBalance(BigDecimal.valueOf(balanceValue, 2));
        index += 4;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-02 10:43:00
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("👨‍🚀 【0x34】 {} 远程控制启机  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("👨‍🚀 【0x34】 {} 远程控制启机  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("👨‍🚀 【0x34】 {} 远程控制启机  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("👨‍🚀 【0x34】 {} 远程控制启机  交易编号    tradeNo                      : {}", PURPLE + deviceId + RESET, tradeNo);
        log.info("👨‍🚀 【0x34】 {} 远程控制启机  逻辑卡号    logicalCardNo                : {}", PURPLE + deviceId + RESET, logicalCardNo);
        log.info("👨‍🚀 【0x34】 {} 远程控制启机  物理卡号    physicalCardNo               : {}", PURPLE + deviceId + RESET, physicalCardNo);
        log.info("👨‍🚀 【0x34】 {} 远程控制启机  账户余额    balance                      : {}", PURPLE + deviceId + RESET, balance);
        System.out.println();
    }
}
