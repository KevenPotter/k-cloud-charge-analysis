package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SBBUpgradeReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_UPGRADE;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 远程更新应答 [0X93]
 *
 * @author KevenPotter
 * @date 2026-06-05 14:21:08
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SBBUpgradeRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*升级状态*/
    private Integer upgradeStatus;

    /**
     * 构建下发指令
     *
     * @param upgradeReq 远程更新
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-05 14:21:15
     */
    public static byte[] buildCommand(SBBUpgradeReq upgradeReq) {
        SBBUpgradeRes res = new SBBUpgradeRes();
        res.setSeqNo(upgradeReq.getSeqNo());
        res.setFrameType(SIM_DOWN_UPGRADE);
        res.setDeviceId(upgradeReq.getDeviceId());
        res.setUpgradeStatus(0);

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
     * @date 2026-06-05 14:21:36
     */
    private byte[] buildBody() {
        byte[] body = new byte[8];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        // 升级状态 [1字节] [BIN]
        body[7] = (byte) (upgradeStatus & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-05 14:21:54
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(4096);
        String devLabel = PURPLE + "⇓ 【0x93】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 远程更新应答  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 远程更新应答  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 远程更新应答  升级状态    upgradeStatus                : %s\n", devLabel, upgradeStatus));
        log.info(sb.toString());
    }
}
