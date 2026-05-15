package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.analysis.req.AALoginReq;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_LOGIN;

/**
 * 登录认证应答 [0X02]
 *
 * @author KevenPotter
 * @date 2026-04-21 11:21:04
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AALoginRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*登录结果(00.成功 01.失败)*/
    private String loginResult;

    /**
     * 构建下发指令
     *
     * @param req 充电桩登录认证
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-21 17:25:30
     */
    public static byte[] buildCommand(AALoginReq req) {
        AALoginRes res = new AALoginRes();
        res.setSeqNo(req.getSeqNo());
        res.setFrameType(DOWN_LOGIN);
        res.setDeviceId(req.getDeviceId());
        res.setLoginResult("00");

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
     * @date 2026-04-21 17:26:35
     */
    private byte[] buildBody() {
        byte[] body = new byte[8];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 登录结果 [1字节] [BIN]
        body[7] = Byte.parseByte(this.loginResult);
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
        log.info("🔶 【0x02】 {} 登录认证应答 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x02】 {} 登录认证应答 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x02】 {} 登录认证应答 登录结果    loginResult          : {}", deviceId, "00".equals(loginResult) ? "成功" : "失败");
        System.out.println();
    }
}
