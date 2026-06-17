package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SARBalanceUpdateReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_BALANCE_UPDATE;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 余额更新应答 [0X41]
 *
 * @author KevenPotter
 * @date 2026-06-03 16:38:52
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SARBalanceUpdateRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*物理卡号*/
    private String physicalCardNo;
    /*修改结果*/
    private Integer updateResult;

    /**
     * 构建下发指令
     *
     * @param balanceUpdateReq 远程账户余额更新
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-03 16:39:07
     */
    public static byte[] buildCommand(SARBalanceUpdateReq balanceUpdateReq) {
        SARBalanceUpdateRes res = new SARBalanceUpdateRes();
        res.setSeqNo(balanceUpdateReq.getSeqNo());
        res.setFrameType(SIM_DOWN_BALANCE_UPDATE);
        res.setDeviceId(balanceUpdateReq.getDeviceId());
        res.setPhysicalCardNo(balanceUpdateReq.getPhysicalCardNo());
        res.setUpdateResult(0);

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
     * @date 2026-06-03 16:39:22
     */
    private byte[] buildBody() {
        byte[] body = new byte[16];
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 物理卡号 [8字节] [BIN]
        String cardNoFull = StrUtil.padPre(this.physicalCardNo, 16, '0');
        byte[] cardNoBytes = HexUtil.decodeHex(cardNoFull);
        System.arraycopy(cardNoBytes, 0, body, 7, 8);
        // 修改结果 [1字节] [BIN]
        body[15] = (byte) (this.updateResult & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-03 16:40:00
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x41】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 余额更新应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 余额更新应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 余额更新应答  物理卡号    physicalCardNo               : %s\n", devLabel, physicalCardNo));
        sb.append(String.format("👩‍🚀%s 余额更新应答  修改结果    updateResult                 : %s\n", devLabel, updateResult));
        log.info(sb.toString());
    }
}
