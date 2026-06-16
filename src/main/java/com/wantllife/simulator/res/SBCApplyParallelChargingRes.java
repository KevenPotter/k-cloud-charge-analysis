package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardApplyParallelCharging;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_APPLY_PARALLEL_CHARGING;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电桩主动申请并充充电 [0XA1]
 *
 * @author KevenPotter
 * @date 2026-06-16 09:57:24
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SBCApplyParallelChargingRes extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*启动方式*/
    private Integer startupMode;
    /*是否需要密码(0.不需要 1.需要)*/
    private Integer needPassword;
    /*账号或物理卡号*/
    private String accountOrCardNo;
    /*输入密码*/
    private String password;
    /*车辆识别码*/
    private String vin;
    /*主辅枪标记*/
    private Integer priAndSecFlag;
    /*并充序号*/
    private String parallelNo;

    /**
     * 构建下发指令
     *
     * @param applyParallelCharging 充电桩主动申请并充充电
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-16 09:57:54
     */
    public static byte[] buildCommand(StandardApplyParallelCharging applyParallelCharging) {
        SBCApplyParallelChargingRes res = new SBCApplyParallelChargingRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_APPLY_PARALLEL_CHARGING);
        res.setDeviceId(applyParallelCharging.getDeviceId());
        res.setGunNo(applyParallelCharging.getGunNo());
        res.setStartupMode(applyParallelCharging.getStartupMode());
        res.setNeedPassword(applyParallelCharging.getNeedPassword());
        res.setAccountOrCardNo(applyParallelCharging.getAccountOrCardNo());
        res.setPassword(applyParallelCharging.getPassword());
        res.setVin(applyParallelCharging.getVin());
        res.setPriAndSecFlag(applyParallelCharging.getPriAndSecFlag());
        res.setParallelNo(applyParallelCharging.getParallelNo());

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
     * @date 2026-06-16 09:58:12
     */
    private byte[] buildBody() {
        byte[] body = new byte[58];
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
        // 主辅枪标记 [1字节] [BIN]
        body[51] = (byte) (priAndSecFlag & 0xFF);
        // 并充序号 [6字节] [BCD]
        String parallelFull = StrUtil.padPre(parallelNo, 12, '0');
        byte[] parallelBcd = StringUtil.string2bcd(parallelFull);
        System.arraycopy(parallelBcd, 0, body, 52, 6);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-16 09:58:40
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0xA1】 {} 主动申请并充  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0xA1】 {} 主动申请并充  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0xA1】 {} 主动申请并充  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0xA1】 {} 主动申请并充  启动方式    startupMode                  : {}", PURPLE + deviceId + RESET, startupMode);
        log.info("🚀 【0xA1】 {} 主动申请并充  需要密码    needPassword                 : {}", PURPLE + deviceId + RESET, needPassword == 0 ? "不需要密码" : "需要密码");
        log.info("🚀 【0xA1】 {} 主动申请并充  卡号信息    accountOrCardNo              : {}", PURPLE + deviceId + RESET, accountOrCardNo);
        log.info("🚀 【0xA1】 {} 主动申请并充  输入密码    password                     : {}", PURPLE + deviceId + RESET, password);
        log.info("🚀 【0xA1】 {} 主动申请并充  车识别码    VIN                          : {}", PURPLE + deviceId + RESET, vin);
        log.info("🚀 【0xA1】 {} 主动申请并充  主辅标记    priAndSecFlag                : {}", PURPLE + deviceId + RESET, priAndSecFlag);
        log.info("🚀 【0xA1】 {} 主动申请并充  并充序号    parallelNo                   : {}", PURPLE + deviceId + RESET, parallelNo);
        System.out.println();
    }
}
