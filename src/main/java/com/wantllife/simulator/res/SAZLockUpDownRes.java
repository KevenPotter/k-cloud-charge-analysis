package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SAZLockUpDownReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_LOCK_UP_DOWN;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电桩返回数据 [0X63]
 *
 * @author KevenPotter
 * @date 2026-06-05 10:26:30
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAZLockUpDownRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*地锁控制返回标志*/
    private Integer upDownStatus;
    /*预留位*/
    private String reserved;

    /**
     * 构建下发指令
     *
     * @param lockUpDownReq 遥控地锁升锁与降锁
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-05 10:26:44
     */
    public static byte[] buildCommand(SAZLockUpDownReq lockUpDownReq) {
        SAZLockUpDownRes res = new SAZLockUpDownRes();
        res.setSeqNo(lockUpDownReq.getSeqNo());
        res.setFrameType(SIM_DOWN_LOCK_UP_DOWN);
        res.setDeviceId(lockUpDownReq.getDeviceId());
        res.setGunNo(lockUpDownReq.getGunNo());
        res.setUpDownStatus(1);
        res.setReserved(lockUpDownReq.getReserved());

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
     * @date 2026-06-05 10:26:50
     */
    private byte[] buildBody() {
        byte[] body = new byte[13];
        // 设备编号 [7字节] [BCD]
        String devFull = StrUtil.padPre(deviceId, 14, '0');
        byte[] devBcd = StringUtil.string2bcd(devFull);
        System.arraycopy(devBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BIN]
        body[7] = (byte) (gunNo & 0xFF);
        // 地锁控制返回标志 [1字节] [BIN]
        body[8] = (byte) (upDownStatus & 0xFF);
        // 预留位 [4字节] [BIN]
        byte[] reservedBytes = HexUtil.decodeHex(reserved);
        System.arraycopy(reservedBytes, 0, body, 9, 4);

        return body;
    }


    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-05 10:27:09
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x63】 {} 设备返回数据  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x63】 {} 设备返回数据  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x63】 {} 设备返回数据  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0x63】 {} 设备返回数据  控制标志    upDownStatus                 : {}", PURPLE + deviceId + RESET, upDownStatus == 1 ? "鉴权成功" : "鉴权失败");
        log.info("🚀 【0x63】 {} 设备返回数据  预留位值    reserved                     : {}", PURPLE + deviceId + RESET, reserved);
        System.out.println();
    }
}
