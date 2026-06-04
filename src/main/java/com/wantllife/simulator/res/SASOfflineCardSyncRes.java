package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SASOfflineCardSyncReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_OFFLINE_CARD_SYNC;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 离线卡数据同步应答 [0X43]
 *
 * @author KevenPotter
 * @date 2026-06-04 10:17:11
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SASOfflineCardSyncRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*保存结果*/
    private Integer saveResult;
    /*失败原因*/
    private Integer failReason;

    /**
     * 构建下发指令
     *
     * @param offlineCardSyncReq 离线卡数据同步
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-04 10:17:50
     */
    public static byte[] buildCommand(SASOfflineCardSyncReq offlineCardSyncReq) {
        SASOfflineCardSyncRes res = new SASOfflineCardSyncRes();
        res.setSeqNo(offlineCardSyncReq.getSeqNo());
        res.setFrameType(SIM_DOWN_OFFLINE_CARD_SYNC);
        res.setDeviceId(offlineCardSyncReq.getDeviceId());
        res.setSaveResult(1);
        res.setFailReason(0);

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
     * @date 2026-06-04 10:18:36
     */
    private byte[] buildBody() {
        byte[] body = new byte[9];
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 保存结果 [1字节] [BIN]
        body[7] = (byte) (this.saveResult & 0xFF);
        // 失败原因 [1字节] [BIN]
        body[8] = (byte) (this.failReason & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-04 10:18:55
     */
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x43】 {} 电卡同步应答  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x43】 {} 电卡同步应答  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x43】 {} 电卡同步应答  保存结果    saveResult                   : {}", PURPLE + deviceId + RESET, saveResult == 0 ? "保存失败" : "保存成功");
        log.info("🚀 【0x43】 {} 电卡同步应答  失败原因    failReason                   : {}", PURPLE + deviceId + RESET, failReason);
        System.out.println();
    }
}
