package com.wantllife.analysis.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 上传实时监测数据 [0X13]
 *
 * @author KevenPotter
 * @date 2026-04-23 17:04:30
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AERealTimeMonitorReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*状态*/
    private Integer status;
    /*状态描述*/
    private String statusDesc;
    /*枪是否归位*/
    private Integer haveReturn;
    /*枪是否归位描述*/
    private String haveReturnDesc;
    /*是否插枪*/
    private Integer haveInsert;
    /*输出电压*/
    private BigDecimal voltage;
    /*输出电流*/
    private BigDecimal current;
    /*枪线温度*/
    private Integer temperature;
    /*枪线编码*/
    private String gunCode;
    /*SOC*/
    private Integer soc;
    /*电池组最高温度*/
    private Integer highestTemperature;
    /*累计充电时间*/
    private Integer accumulatedChargingTime;
    /*剩余时间*/
    private Integer remainingChargingTime;
    /*充电度数*/
    private BigDecimal chargingDegree;
    /*计损充电度数*/
    private BigDecimal calculatedChargingDegree;
    /*已充金额*/
    private BigDecimal chargedAmount;
    /*硬件故障*/
    private String hardwareFailure;
    /*硬件故障描述*/
    private String hardwareFailureDesc;

    /* 有参构造 */
    public AERealTimeMonitorReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-23 17:19:14
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        this.setTradeNo(StringUtil.bcd2String(data, index, 16));
        index += 16;
        // 设备编号 [7字节] [BCD]
        this.setDeviceId(StringUtil.bcd2String(data, index, 7));
        index += 7;
        // 枪号 [1字节] [BCD]
        this.setGunNo(data[index++] & 0xFF);
        // 状态 [1字节] [BIN]
        this.setStatus(data[index++] & 0xFF);
        this.setStatusDesc(parseStatusDesc(this.status));
        // 枪是否归位 [1字节] [BIN]
        this.setHaveReturn(data[index++] & 0xFF);
        this.setHaveReturnDesc(parseHaveReturnDesc(this.haveReturn));
        // 是否插枪 [1字节] [BIN]
        this.setHaveInsert(data[index++] & 0xFF);
        // 输出电压 [2字节] [BIN]
        int voltageValue = ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setVoltage(BigDecimal.valueOf(voltageValue).movePointLeft(1));
        index += 2;
        // 输出电流 [2字节] [BIN]
        int currentValue = ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setCurrent(BigDecimal.valueOf(currentValue).movePointLeft(1));
        index += 2;
        // 枪线温度 [1字节] [BIN]
        this.setTemperature((data[index++] & 0xFF) - 50);
        // 枪线编码 [8字节] [BIN]
        byte[] gunCodeBytes = new byte[8];
        System.arraycopy(data, index, gunCodeBytes, 0, 8);
        this.setGunCode(HexUtil.encodeHexStr(gunCodeBytes).toUpperCase());
        index += 8;
        // SOC [1字节] [BIN]
        this.setSoc(data[index++] & 0xFF);
        // 电池组最高温度 [1字节] [BIN]
        this.setHighestTemperature((data[index++] & 0xFF) - 50);
        // 累计充电时间 [2字节] [BIN]
        int accumulatedTime = ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setAccumulatedChargingTime(accumulatedTime);
        index += 2;
        // 剩余时间 [2字节] [BIN]
        int remainingTime = ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setRemainingChargingTime(remainingTime);
        index += 2;
        // 充电度数 [4字节] [BIN]
        long chargeDegree = ((long) (data[index + 3] & 0xFF) << 24) | ((long) (data[index + 2] & 0xFF) << 16) | ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setChargingDegree(BigDecimal.valueOf(chargeDegree).movePointLeft(4));
        index += 4;
        // 计损充电度数 [4字节] [BIN]
        long calcDegree = ((long) (data[index + 3] & 0xFF) << 24) | ((long) (data[index + 2] & 0xFF) << 16) | ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setCalculatedChargingDegree(BigDecimal.valueOf(calcDegree).movePointLeft(4));
        index += 4;
        // 已充金额 [4字节] [BIN]
        long chargedAmt = ((long) (data[index + 3] & 0xFF) << 24) | ((long) (data[index + 2] & 0xFF) << 16) | ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setChargedAmount(BigDecimal.valueOf(chargedAmt).movePointLeft(4));
        index += 4;
        // 硬件故障 [2字节] [BIN]
        int hardwareFault = ((data[index + 1] & 0xFF) << 8) | (data[index] & 0xFF);
        this.setHardwareFailure(HexUtil.toHex(hardwareFault).toUpperCase());
        this.setHardwareFailureDesc(parseHardwareFailureDesc(hardwareFault));
        index += 2;
    }

    /**
     * 获取设备状态描述
     *
     * @author KevenPotter
     * @date 2026-04-24 09:45:56
     */
    private String parseStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0x00:
                return "离线";
            case 0x01:
                return "故障";
            case 0x02:
                return "空闲";
            case 0x03:
                return "充电";
            default:
                return "未知";
        }
    }

    /**
     * 获取枪是否归位描述
     *
     * @author KevenPotter
     * @date 2026-05-09 21:14:25
     */
    private String parseHaveReturnDesc(Integer haveReturn) {
        if (haveReturn == null) return "未知";
        switch (haveReturn) {
            case 0x00:
                return "未归位";
            case 0x01:
                return "已归位";
            case 0x02:
                return "无法检测到";
            default:
                return "未知";
        }
    }

    /**
     * 获取硬件故障描述
     *
     * @author KevenPotter
     * @date 2026-04-24 09:45:12
     */
    private String parseHardwareFailureDesc(int faultCode) {
        StringBuilder sb = new StringBuilder();
        if ((faultCode & 0x0001) != 0) sb.append("急停按钮动作故障;");
        if ((faultCode & 0x0002) != 0) sb.append("无可用整流模块;");
        if ((faultCode & 0x0004) != 0) sb.append("出风口温度过高;");
        if ((faultCode & 0x0008) != 0) sb.append("交流防雷故障;");
        if ((faultCode & 0x0010) != 0) sb.append("交直流模块DC20通信中断;");
        if ((faultCode & 0x0020) != 0) sb.append("绝缘检测模块FC08通信中断;");
        if ((faultCode & 0x0040) != 0) sb.append("电度表通信中断;");
        if ((faultCode & 0x0080) != 0) sb.append("读卡器通信中断;");
        if ((faultCode & 0x0100) != 0) sb.append("RC10通信中断;");
        if ((faultCode & 0x0200) != 0) sb.append("风扇调速板故障;");
        if ((faultCode & 0x0400) != 0) sb.append("直流熔断器故障;");
        if ((faultCode & 0x0800) != 0) sb.append("高压接触器故障;");
        if ((faultCode & 0x1000) != 0) sb.append("门打开;");
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "无故障";
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 14:01:40
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x13】 {} 上传实时监测数据 原始报文    rawMsg                  : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x13】 {} 上传实时监测数据 设备编号    deviceId                : {}", deviceId, deviceId);
        log.info("🟢 【0x13】 {} 上传实时监测数据 枪口编号    gunNo                   : {}", deviceId, gunNo);
        log.info("🟢 【0x13】 {} 上传实时监测数据 交易编号    tradeNo                 : {}", deviceId, tradeNo);
        log.info("🟢 【0x13】 {} 上传实时监测数据 设备状态    statusDesc              : {}", deviceId, statusDesc);
        log.info("🟢 【0x13】 {} 上传实时监测数据 插枪归位    haveReturnDesc          : {}", deviceId, haveReturnDesc);
        log.info("🟢 【0x13】 {} 上传实时监测数据 是否插枪    haveInsert              : {}", deviceId, haveInsert == 0 ? "未插枪" : "已插枪");
        log.info("🟢 【0x13】 {} 上传实时监测数据 输出电压    voltage                 : {}", deviceId, voltage);
        log.info("🟢 【0x13】 {} 上传实时监测数据 输出电流    current                 : {}", deviceId, current);
        log.info("🟢 【0x13】 {} 上传实时监测数据 枪线温度    temperature             : {}", deviceId, temperature);
        log.info("🟢 【0x13】 {} 上传实时监测数据 枪线编码    gunCode                 : {}", deviceId, gunCode);
        log.info("🟢 【0x13】 {} 上传实时监测数据 充电率值    SOC                     : {}", deviceId, soc);
        log.info("🟢 【0x13】 {} 上传实时监测数据 最高温度    highestTemperature      : {}", deviceId, highestTemperature);
        log.info("🟢 【0x13】 {} 上传实时监测数据 累充时间    accumulatedChargingTime : {}", deviceId, accumulatedChargingTime);
        log.info("🟢 【0x13】 {} 上传实时监测数据 剩余时间    remainingChargingTime   : {}", deviceId, remainingChargingTime);
        log.info("🟢 【0x13】 {} 上传实时监测数据 充电度数    chargingDegree          : {}", deviceId, chargingDegree);
        log.info("🟢 【0x13】 {} 上传实时监测数据 计损度数    calculatedChargingDegree: {}", deviceId, calculatedChargingDegree);
        log.info("🟢 【0x13】 {} 上传实时监测数据 已充金额    chargedAmount           : {}", deviceId, chargedAmount);
        log.info("🟢 【0x13】 {} 上传实时监测数据 故障描述    hardwareFailureDesc     : {}", deviceId, hardwareFailureDesc);
        System.out.println();
    }

}
