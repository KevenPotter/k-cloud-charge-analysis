package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电阶段BMS中止 [0X1D]
 *
 * @author KevenPotter
 * @date 2026-04-24 17:03:25
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AJChargingBMSStopReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*BMS中止充电原因*/
    private String bmsStopReason;
    /*BMS中止充电原因描述*/
    private String bmsStopReasonDesc;
    /*BMS中止充电故障原因*/
    private String bmsStopFailure;
    /*BMS中止充电故障原因描述*/
    private String bmsStopFailureDesc;
    /*BMS中止充电错误原因*/
    private String bmsStopErrorReason;
    /*BMS中止充电错误原因描述*/
    private String bmsStopErrorReasonDesc;


    /* 有参构造 */
    public AJChargingBMSStopReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-24 17:03:54
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        this.tradeNo = StringUtil.bcd2String(data, index, 16);
        index += 16;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BCD]
        this.gunNo = data[index++] & 0xFF;
        // BMS中止充电原因 [1字节] [BIN]
        int reason = data[index++] & 0xFF;
        this.bmsStopReason = String.format("%8s", Integer.toBinaryString(reason)).replace(' ', '0');
        this.bmsStopReasonDesc = parseBmsStopReasonDesc(reason);
        // BMS中止充电故障原因 [2字节] [BIN]
        int failure = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.bmsStopFailure = String.format("%16s", Integer.toBinaryString(failure)).replace(' ', '0');
        this.bmsStopFailureDesc = parseBmsStopFailureDesc(failure);
        // BMS中止充电错误原因 [1字节] [BIN]
        int error = data[index++] & 0xFF;
        this.bmsStopErrorReason = String.format("%8s", Integer.toBinaryString(error)).replace(' ', '0');
        this.bmsStopErrorReasonDesc = parseBmsStopErrorDesc(error);
    }

    /**
     * 解析BMS中止充电原因(按位)
     *
     * @author KevenPotter
     * @date 2026-04-24 23:21:12
     */
    private String parseBmsStopReasonDesc(int val) {
        StringBuilder sb = new StringBuilder();
        if ((val & 0b00000011) != 0) sb.append("SOC目标达成");
        if ((val & 0b00001100) != 0) sb.append((sb.length() > 0 ? ";" : "") + "总电压达到设定值");
        if ((val & 0b00110000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "单体电压达到设定值");
        if ((val & 0b11000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "充电机主动中止");
        return sb.length() > 0 ? sb.toString() : "无";
    }

    /**
     * 解析BMS中止故障原因(16位)
     *
     * @author KevenPotter
     * @date 2026-04-24 23:21:17
     */
    private String parseBmsStopFailureDesc(int val) {
        StringBuilder sb = new StringBuilder();
        if ((val & 0b0000000000000011) != 0) sb.append("绝缘故障");
        if ((val & 0b0000000000001100) != 0) sb.append((sb.length() > 0 ? ";" : "") + "输出连接器过温故障");
        if ((val & 0b0000000000110000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "BMS元件/连接器过温");
        if ((val & 0b0000000011000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "充电连接器故障");
        if ((val & 0b0000001100000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "电池组温度过高");
        if ((val & 0b0000110000000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "高压继电器故障");
        if ((val & 0b0011000000000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "检测点2电压故障");
        if ((val & 0b1100000000000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "其他故障");
        return sb.length() > 0 ? sb.toString() : "无";
    }

    /**
     * 解析BMS中止错误原因
     *
     * @author KevenPotter
     * @date 2026-04-24 23:21:35
     */
    private String parseBmsStopErrorDesc(int val) {
        StringBuilder sb = new StringBuilder();
        if ((val & 0b00000011) != 0) sb.append("电流过大");
        if ((val & 0b00001100) != 0) sb.append((sb.length() > 0 ? ";" : "") + "电压异常");
        return sb.length() > 0 ? sb.toString() : "无";
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 10:15:50
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x1D】 {} 充电阶段BMS中止 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x1D】 {} 充电阶段BMS中止 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x1D】 {} 充电阶段BMS中止 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x1D】 {} 充电阶段BMS中止 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x1D】 {} 充电阶段BMS中止 中止原因    bmsStopReasonDesc            : {}", deviceId, bmsStopReasonDesc);
        log.info("🟢 【0x1D】 {} 充电阶段BMS中止 故障原因    bmsStopFailureDesc           : {}", deviceId, bmsStopFailureDesc);
        log.info("🟢 【0x1D】 {} 充电阶段BMS中止 错误原因    bmsStopErrorReasonDesc       : {}", deviceId, bmsStopErrorReasonDesc);
        System.out.println();
    }

}
