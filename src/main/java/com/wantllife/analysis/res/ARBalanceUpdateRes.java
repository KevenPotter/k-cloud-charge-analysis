package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_BALANCE_UPDATE;


/**
 * 远程账户余额更新 [0X42]
 *
 * @author KevenPotter
 * @date 2026-04-27 16:28:15
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ARBalanceUpdateRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*物理卡号*/
    private String physicalCardNo;
    /*修改后账户金额*/
    private BigDecimal balance;


    /**
     * 构建下发指令
     *
     * @param deviceId       设备编号
     * @param gunNo          枪号
     * @param physicalCardNo 物理卡号
     * @param balance        修改后账户金额
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-27 16:28:28
     */
    public static byte[] buildCommand(String deviceId, Integer gunNo, String physicalCardNo, BigDecimal balance) {
        ARBalanceUpdateRes res = new ARBalanceUpdateRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(DOWN_BALANCE_UPDATE);
        res.setDeviceId(deviceId);
        res.setGunNo(gunNo);
        res.setPhysicalCardNo(physicalCardNo);
        res.setBalance(balance);

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
     * @date 2026-04-27 16:28:56
     */
    private byte[] buildBody() {
        byte[] body = new byte[20];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BCD]
        body[7] = StringUtil.string2bcd(String.format("%02d", this.gunNo))[0];
        // 物理卡号 [8字节] [BIN]
        String fullCardNo = String.format("%16s", this.physicalCardNo).replace(' ', '0');
        byte[] phyCardBytes = HexUtil.decodeHex(fullCardNo);
        System.arraycopy(phyCardBytes, 0, body, 8, 8);
        // 修改后账户金额 [4字节] [BIN]
        long balanceVal = this.balance.multiply(new BigDecimal(100)).longValue();
        body[16] = (byte) (balanceVal & 0xFF);
        body[17] = (byte) ((balanceVal >> 8) & 0xFF);
        body[18] = (byte) ((balanceVal >> 16) & 0xFF);
        body[19] = (byte) ((balanceVal >> 24) & 0xFF);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 14:24:44
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x42】 {} 远程账户余额更新 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x42】 {} 远程账户余额更新 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x42】 {} 远程账户余额更新 枪口编号    gunNo                : {}", deviceId, gunNo);
        log.info("🔶 【0x42】 {} 远程账户余额更新 物理卡号    physicalCardNo       : {}", deviceId, physicalCardNo);
        log.info("🔶 【0x42】 {} 远程账户余额更新 改后金额    balance              : {}", deviceId, balance);
        System.out.println();
    }

}
