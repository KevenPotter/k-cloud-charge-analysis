package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 充电阶段充电机中止 [0X21]
 *
 * @author KevenPotter
 * @date 2026-04-24 23:22:05
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AKChargingChargerStopReq extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*充电机中止充电原因*/
    private String chargerStopReason;
    /*充电机中止充电原因描述*/
    private String chargerStopReasonDesc;
    /*充电机中止充电故障原因*/
    private String chargerStopFailure;
    /*充电机中止充电故障原因描述*/
    private String chargerStopFailureDesc;
    /*充电机中止充电错误原因*/
    private String chargerStopErrorReason;
    /*充电机中止充电错误原因描述*/
    private String chargerStopErrorReasonDesc;


    /* 有参构造 */
    public AKChargingChargerStopReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-24 23:36:05
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
        // 充电机中止充电原因 [1字节] [BIN]
        int reason = data[index++] & 0xFF;
        this.chargerStopReason = String.format("%8s", Integer.toBinaryString(reason)).replace(' ', '0');
        this.chargerStopReasonDesc = parseChargerStopReasonDesc(reason);
        // 充电机中止充电故障原因 [2字节] [BIN]
        int failure = ((data[index] & 0xFF) << 8) | (data[index + 1] & 0xFF);
        index += 2;
        this.chargerStopFailure = String.format("%16s", Integer.toBinaryString(failure)).replace(' ', '0');
        this.chargerStopFailureDesc = parseChargerStopFailureDesc(failure);
        // 充电机中止充电错误原因 [1字节] [BIN]
        int error = data[index++] & 0xFF;
        this.chargerStopErrorReason = String.format("%8s", Integer.toBinaryString(error)).replace(' ', '0');
        this.chargerStopErrorReasonDesc = parseChargerStopErrorDesc(error);
    }

    /**
     * 解析BMS中止充电原因(按位)
     *
     * @author KevenPotter
     * @date 2026-04-24 23:37:51
     */
    private String parseChargerStopReasonDesc(int val) {
        StringBuilder sb = new StringBuilder();
        if ((val & 0b00000011) != 0) sb.append("达到充电机设定的条件中止");
        if ((val & 0b00001100) != 0) sb.append((sb.length() > 0 ? ";" : "") + "人工中止");
        if ((val & 0b00110000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "异常中止");
        if ((val & 0b11000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "BMS主动中止");
        return sb.length() > 0 ? sb.toString() : "无";
    }

    /**
     * 解析BMS中止故障原因(16位)
     *
     * @author KevenPotter
     * @date 2026-04-24 23:38:18
     */
    private String parseChargerStopFailureDesc(int val) {
        StringBuilder sb = new StringBuilder();
        if ((val & 0b0000000000000011) != 0) sb.append("充电机过温故障");
        if ((val & 0b0000000000001100) != 0) sb.append((sb.length() > 0 ? ";" : "") + "充电连接器故障");
        if ((val & 0b0000000000110000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "充电机内部过温故障");
        if ((val & 0b0000000011000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "所需电量不能传送");
        if ((val & 0b0000001100000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "充电机急停故障");
        if ((val & 0b0000110000000000) != 0) sb.append((sb.length() > 0 ? ";" : "") + "其他故障");
        return sb.length() > 0 ? sb.toString() : "无";
    }

    /**
     * 解析BMS中止错误原因
     *
     * @author KevenPotter
     * @date 2026-04-24 23:39:55
     */
    private String parseChargerStopErrorDesc(int val) {
        StringBuilder sb = new StringBuilder();
        if ((val & 0b00000001) != 0) sb.append("电流不匹配");
        if ((val & 0b00000010) != 0) sb.append((sb.length() > 0 ? ";" : "") + "电压异常");
        return sb.length() > 0 ? sb.toString() : "无";
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 10:17:24
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x21】 {} 充电阶段充电机中止 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x21】 {} 充电阶段充电机中止 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x21】 {} 充电阶段充电机中止 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x21】 {} 充电阶段充电机中止 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x21】 {} 充电阶段充电机中止 中止原因    chargerStopReason            : {}", deviceId, chargerStopReason);
        log.info("🟢 【0x21】 {} 充电阶段充电机中止 故障原因    chargerStopFailureDesc       : {}", deviceId, chargerStopFailureDesc);
        log.info("🟢 【0x21】 {} 充电阶段充电机中止 错误原因    chargerStopErrorReasonDesc   : {}", deviceId, chargerStopErrorReasonDesc);
        System.out.println();
    }

}
