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
 * 远程账户余额更新 [0X42]
 *
 * @author KevenPotter
 * @date 2026-06-03 15:17:33
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SARBalanceUpdateReq extends FrameHeader {


    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*物理卡号*/
    private String physicalCardNo;
    /*修改后账户金额*/
    private BigDecimal balance;

    /* 有参构造 */
    public SARBalanceUpdateReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-06-03 15:18:26
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BCD]
        this.setGunNo(Integer.valueOf(StringUtil.bcd2String(data, index, 1)));
        index += 1;
        // 物理卡号 [8字节] [BIN]
        byte[] cardBytes = new byte[8];
        System.arraycopy(data, index, cardBytes, 0, 8);
        this.setPhysicalCardNo(HexUtil.encodeHexStr(cardBytes).toUpperCase());
        index += 8;
        // 修改后账户金额 [4字节] [BIN]
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
     * @date 2026-06-03 15:19:25
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("👨‍🚀 【0x42】 {} 远程余额更新  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("👨‍🚀 【0x42】 {} 远程余额更新  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("👨‍🚀 【0x42】 {} 远程余额更新  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("👨‍🚀 【0x42】 {} 远程余额更新  物理卡号    physicalCardNo               : {}", PURPLE + deviceId + RESET, physicalCardNo);
        log.info("👨‍🚀 【0x42】 {} 远程余额更新  改后金额    balance                      : {}", PURPLE + deviceId + RESET, balance);
        System.out.println();
    }
}
