package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.analysis.req.ABHeartbeatReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_HEARTBEAT;


/**
 * 心跳包应答 [0X04]
 *
 * @author KevenPotter
 * @date 2026-04-22 15:38:21
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ABHeartbeatRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*心跳应答*/
    private String heartbeatResult;

    /**
     * 构建下发指令
     *
     * @param req 充电桩心跳包
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-22 15:39:30
     */
    public static byte[] buildCommand(ABHeartbeatReq req) {
        ABHeartbeatRes res = new ABHeartbeatRes();
        res.setSeqNo(req.getSeqNo());
        res.setFrameType(DOWN_HEARTBEAT);
        res.setDeviceId(req.getDeviceId());
        res.setGunNo(req.getGunNo());
        res.setHeartbeatResult("00");

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
     * @date 2026-04-22 15:41:47
     */
    private byte[] buildBody() {
        byte[] body = new byte[9];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BCD]
        body[7] = this.gunNo.byteValue();
        // 心跳应答 [1字节] [BIN]
        body[8] = Byte.parseByte(this.heartbeatResult);
        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 15:50:06
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x04】 {} 心跳包应答 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x04】 {} 心跳包应答 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x04】 {} 心跳包应答 枪口编号    gunNo                : {}", deviceId, gunNo);
        log.info("🔶 【0x04】 {} 心跳包应答 心跳应答    heartbeatResult      : {}", deviceId, heartbeatResult);
        System.out.println();
    }
}
