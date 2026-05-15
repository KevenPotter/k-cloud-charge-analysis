package com.wantllife.analysis.req;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电桩主动申请启动充电 [0X31]
 *
 * @author KevenPotter
 * @date 2026-04-27 09:08:52
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ANRequestChargingReq extends FrameHeader {

    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*启动方式*/
    private Integer startupMode;
    /*启动方式描述*/
    private String startupModeDesc;
    /*是否需要密码*/
    private Integer needPassword;
    /*账号或物理卡号*/
    private String accountOrCardNo;
    /*输入密码*/
    private String password;
    /*车辆识别码*/
    private String vin;


    /* 有参构造 */
    public ANRequestChargingReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
        // 3.记录日志
        log(rawHexMsg);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-04-27 09:28:06
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BCD]
        this.gunNo = data[index++] & 0xFF;
        // 启动方式 [1字节] [BIN]
        this.startupMode = data[index++] & 0xFF;
        this.startupModeDesc = parseStartupMode(startupMode);
        // 是否需要密码 [1字节] [BIN]
        this.needPassword = data[index++] & 0xFF;
        // 账号或者物理卡号 [8字节] [BIN]
        byte[] cardBytes = new byte[8];
        System.arraycopy(data, index, cardBytes, 0, 8);
        this.accountOrCardNo = HexUtil.encodeHexStr(cardBytes).toUpperCase();
        index += 8;
        // 输入密码 [16字节] [BIN]
        byte[] pwdBytes = new byte[16];
        System.arraycopy(data, index, pwdBytes, 0, 16);
        this.password = HexUtil.encodeHexStr(pwdBytes).toUpperCase();
        index += 16;
        // VIN码 [17字节] [ASCII]
        byte[] vinBytes = new byte[17];
        System.arraycopy(data, index, vinBytes, 0, 17);
        String vinStr = new String(vinBytes).trim();
        this.vin = StrUtil.reverse(vinStr);
    }

    /**
     * 解析启动方式描述
     *
     * @author KevenPotter
     * @date 2026-04-27 09:28:28
     */
    private String parseStartupMode(int mode) {
        switch (mode) {
            case 0x01:
                return "刷卡启动充电";
            case 0x02:
                return "帐号启动充电(暂不支持)";
            case 0x03:
                return "VIN码启动充电";
            default:
                return "未知方式";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 13:48:50
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 启动方式    startupModeDesc              : {}", deviceId, startupModeDesc);
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 需要密码    needPassword                 : {}", deviceId, needPassword == 0 ? "不需要密码" : "需要密码");
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 卡号信息    accountOrCardNo              : {}", deviceId, accountOrCardNo);
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 输入密码    password                     : {}", deviceId, password);
        log.info("🟢 【0x31】 {} 充电桩主动申请启动充电 车识别码    VIN                          : {}", deviceId, vin);
        System.out.println();
    }

}
