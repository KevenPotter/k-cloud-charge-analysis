package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardRequestCharging;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_REQUEST_CHARGING;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电桩主动申请启动充电 [0X31]
 *
 * @author KevenPotter
 * @date 2026-06-11 15:40:18
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SANRequestChargingRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*启动方式*/
    private Integer startupMode;
    /*是否需要密码*/
    private Integer needPassword;
    /*账号或物理卡号*/
    private String accountOrCardNo;
    /*输入密码*/
    private String password;
    /*车辆识别码*/
    private String vin;

    /**
     * 构建下发指令
     *
     * @param standardRequestCharging 充电桩主动申请启动充电
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-11 15:40:49
     */
    public static byte[] buildCommand(StandardRequestCharging standardRequestCharging) {
        SANRequestChargingRes res = new SANRequestChargingRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_REQUEST_CHARGING);
        res.setDeviceId(standardRequestCharging.getDeviceId());
        res.setGunNo(standardRequestCharging.getGunNo());
        res.setStartupMode(standardRequestCharging.getStartupMode());
        res.setNeedPassword(standardRequestCharging.getNeedPassword());
        res.setAccountOrCardNo(standardRequestCharging.getAccountOrCardNo());
        res.setPassword(standardRequestCharging.getPassword());
        res.setVin(standardRequestCharging.getVin());

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
     * @date 2026-06-11 15:41:25
     */
    private byte[] buildBody() {
        byte[] body = new byte[51];

        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 枪号 [1字节] [BCD]
        body[7] = StringUtil.string2bcd(StrUtil.padPre(gunNo.toString(), 2, '0'))[0];
        // 启动方式 [1字节] [BIN]
        body[8] = (byte) (startupMode & 0xFF);
        // 是否需要密码 [1字节] [BIN]
        body[9] = (byte) (needPassword & 0xFF);
        // 账号或物理卡号 [8字节] [BIN]
        byte[] cardBuf = new byte[8];
        String cardHex = StrUtil.padPre(accountOrCardNo, 16, '0');
        byte[] cardRaw = HexUtil.decodeHex(cardHex);
        System.arraycopy(cardRaw, 0, cardBuf, 0, 8);
        System.arraycopy(cardBuf, 0, body, 10, 8);
        // 输入密码 [16字节] [BIN]
        String md5Hex = DigestUtil.md5Hex(password).toLowerCase();
        byte[] pwdRaw = HexUtil.decodeHex(md5Hex);
        System.arraycopy(pwdRaw, 0, body, 18, 16);
        // 车辆识别码 [17字节] [ASCII]
        String fullVin = StrUtil.padAfter(vin, 17, '0');
        String reverseVin = StrUtil.reverse(fullVin);
        byte[] vinBuf = reverseVin.getBytes();
        System.arraycopy(vinBuf, 0, body, 34, 17);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-11 15:42:07
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x31】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 主动申请充电  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 主动申请充电  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 主动申请充电  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 主动申请充电  启动方式    startupMode                  : %s\n", devLabel, startupMode));
        sb.append(String.format("👩‍🚀%s 主动申请充电  需要密码    needPassword                 : %s\n", devLabel, needPassword == 0 ? "不需要密码" : "需要密码"));
        sb.append(String.format("👩‍🚀%s 主动申请充电  卡号信息    accountOrCardNo              : %s\n", devLabel, accountOrCardNo));
        sb.append(String.format("👩‍🚀%s 主动申请充电  输入密码    password                     : %s\n", devLabel, password));
        sb.append(String.format("👩‍🚀%s 主动申请充电  车识别码    VIN                          : %s\n", devLabel, vin));
        log.info(sb.toString());
    }
}
