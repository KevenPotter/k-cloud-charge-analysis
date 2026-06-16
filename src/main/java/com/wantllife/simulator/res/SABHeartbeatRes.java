package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.simulator.req.SAALoginReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_HEARTBEAT;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电桩心跳包 [0X03]
 *
 * @author KevenPotter
 * @date 2026-05-28 09:32:20
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SABHeartbeatRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*枪口状态(0.正常 1.故障)*/
    private Integer gunStatus;

    /**
     * 构建下发指令
     *
     * @param loginReq  登录认证应答
     * @param gunNo     枪号
     * @param gunStatus 枪口状态
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-05-28 09:45:30
     */
    public static byte[] buildCommand(SAALoginReq loginReq, Integer gunNo, Integer gunStatus) {
        SABHeartbeatRes res = new SABHeartbeatRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_HEARTBEAT);
        res.setDeviceId(loginReq.getDeviceId());
        res.setGunNo(gunNo);
        res.setGunStatus(gunStatus);

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body, false);

        // 记录日志
        if (CloudChargeHolder.isSimulatorHeartbeatLogOutput()) res.log(HexUtil.encodeHexStr(downMessage));

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-05-28 09:46:15
     */
    private byte[] buildBody() {
        byte[] body = new byte[9];
        // 1.设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 2.枪号 [1字节] [BCD]
        body[7] = (byte) (this.gunNo & 0xFF);
        // 3.枪状态 [1字节] [BIN]
        body[8] = (byte) (this.gunStatus & 0xFF);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-28 09:46:57
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x03】 {} 设备心跳检测  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x03】 {} 设备心跳检测  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x03】 {} 设备心跳检测  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0x03】 {} 设备心跳检测  枪口状态    gunStatus                    : {}", PURPLE + deviceId + RESET, gunStatus == 0 ? "正常" : "故障");
        System.out.println();
    }
}
