package com.wantllife.analysis.req;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static com.wantllife.util.TimeUtil.parseCP56Time;

/**
 * 交易记录 [0X3B]
 *
 * @author KevenPotter
 * @date 2026-04-27 14:10:25
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AQTradeRecordReq extends FrameHeader {

    /*价格保留小数位*/
    private static final int SCALE_PRICE = 5;
    /*电量保留小数位*/
    private static final int SCALE_ELECTRIC = 4;

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*开始时间*/
    private String startTime;
    /*结束时间*/
    private String endTime;

    /*尖单价*/
    private BigDecimal sharpUnitPrice;
    /*尖电量*/
    private BigDecimal sharpElectricity;
    /*计损尖电量*/
    private BigDecimal sharpLossElectricity;
    /*尖金额*/
    private BigDecimal sharpAmount;

    /*峰单价*/
    private BigDecimal peakUnitPrice;
    /*峰电量*/
    private BigDecimal peakElectricity;
    /*计损峰电量*/
    private BigDecimal peakLossElectricity;
    /*峰金额*/
    private BigDecimal peakAmount;

    /*平单价*/
    private BigDecimal flatUnitPrice;
    /*平电量*/
    private BigDecimal flatElectricity;
    /*计损平电量*/
    private BigDecimal flatLossElectricity;
    /*平金额*/
    private BigDecimal flatAmount;

    /*谷单价*/
    private BigDecimal valleyUnitPrice;
    /*谷电量*/
    private BigDecimal valleyElectricity;
    /*计损谷电量*/
    private BigDecimal valleyLossElectricity;
    /*谷金额*/
    private BigDecimal valleyAmount;

    /*电表总起值*/
    private BigDecimal electricityStart;
    /*电表总止值*/
    private BigDecimal electricityEnd;
    /*总电量*/
    private BigDecimal totalElectricity;
    /*计损总电量*/
    private BigDecimal totalLossElectricity;

    /*消费金额*/
    private BigDecimal totalAmount;
    /*电动汽车唯一标识*/
    private String vinCode;
    /*交易标识*/
    private Integer tradeIdentifier;
    /*交易表示描述*/
    private String tradeIdentifierDesc;
    /*交易日期、时间*/
    private String tradeTime;
    /*停止原因*/
    private Integer stopReason;
    /*停止原因描述*/
    private String stopReasonDesc;
    /*物理卡号*/
    private String physicalCardNo;


    /* 有参构造 */
    public AQTradeRecordReq(byte[] data, String rawHexMsg) {
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
     * @date 2026-04-27 14:10:33
     */
    private void parseBody(byte[] data) {
        int index = 6;
        // 交易流水号 [16字节] [BCD]
        this.tradeNo = StringUtil.bcd2String(data, index, 16);
        index += 16;
        // 设备编号 [7字节] [BCD]
        this.deviceId = StringUtil.bcd2String(data, index, 7);
        index += 7;
        // 枪号 [1字节] [BIN]
        this.gunNo = data[index++] & 0xFF;
        // 开始时间 [7字节] [BIN]
        byte[] startTimeBytes = new byte[7];
        System.arraycopy(data, index, startTimeBytes, 0, 7);
        this.startTime = parseCP56Time(startTimeBytes);
        index += 7;
        // 结束时间 [7字节] [BIN]
        byte[] endTimeBytes = new byte[7];
        System.arraycopy(data, index, endTimeBytes, 0, 7);
        this.endTime = parseCP56Time(endTimeBytes);
        index += 7;
        // 尖单价 [4字节] [BIN]
        this.sharpUnitPrice = readLeLong(data, index).movePointLeft(SCALE_PRICE);
        index += 4;
        // 尖电量 [4字节] [BIN]
        this.sharpElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 计损尖电量 [4字节] [BIN]
        this.sharpLossElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 尖金额 [4字节] [BIN]
        this.sharpAmount = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 峰单价 [4字节] [BIN]
        this.peakUnitPrice = readLeLong(data, index).movePointLeft(SCALE_PRICE);
        index += 4;
        // 峰电量 [4字节] [BIN]
        this.peakElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 计损峰电量 [4字节] [BIN]
        this.peakLossElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 峰金额 [4字节] [BIN]
        this.peakAmount = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 平单价 [4字节] [BIN]
        this.flatUnitPrice = readLeLong(data, index).movePointLeft(SCALE_PRICE);
        index += 4;
        // 平电量 [4字节] [BIN]
        this.flatElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 计损平电量 [4字节] [BIN]
        this.flatLossElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 平金额 [4字节] [BIN]
        this.flatAmount = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 谷单价 [4字节] [BIN]
        this.valleyUnitPrice = readLeLong(data, index).movePointLeft(SCALE_PRICE);
        index += 4;
        // 谷电量 [4字节] [BIN]
        this.valleyElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 计损谷电量 [4字节] [BIN]
        this.valleyLossElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 谷金额 [4字节] [BIN]
        this.valleyAmount = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 电表总起值 [5字节] [BIN]
        this.electricityStart = readLe5Byte(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 5;
        // 电表总止值 [5字节] [BIN]
        this.electricityEnd = readLe5Byte(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 5;
        // 总电量 [4字节] [BIN]
        this.totalElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 计损总电量 [4字节] [BIN]
        this.totalLossElectricity = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // 消费金额 [4字节] [BIN]
        this.totalAmount = readLeLong(data, index).movePointLeft(SCALE_ELECTRIC);
        index += 4;
        // VIN码 [17字节] [ASCII]
        byte[] vinBytes = new byte[17];
        System.arraycopy(data, index, vinBytes, 0, 17);
        this.vinCode = new String(vinBytes).trim();
        index += 17;
        // 交易标识 [1字节] [BIN]
        this.tradeIdentifier = data[index++] & 0xFF;
        this.tradeIdentifierDesc = parseTradeIdentifierDesc(this.tradeIdentifier);
        // 交易时间 [7字节] [BIN]
        byte[] tradeTimeBytes = new byte[7];
        System.arraycopy(data, index, tradeTimeBytes, 0, 7);
        this.tradeTime = parseCP56Time(tradeTimeBytes);
        index += 7;
        // 停止原因 [1字节] [BIN]
        this.stopReason = data[index++] & 0xFF;
        this.stopReasonDesc = parseStopReasonDesc(this.stopReason);
        // 物理卡号 [8字节] [BIN]
        byte[] cardBytes = new byte[8];
        System.arraycopy(data, index, cardBytes, 0, 8);
        this.physicalCardNo = HexUtil.encodeHexStr(cardBytes).toUpperCase();
    }

    /**
     * 小端读取4字节无符号整数
     *
     * @author KevenPotter
     * @date 2026-04-27 15:21:07
     */
    private BigDecimal readLeLong(byte[] data, int index) {
        long value = ByteBuffer.wrap(data, index, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt() & 0xFFFFFFFFL;
        return new BigDecimal(value);
    }

    /**
     * 小端读取5字节无符号整数
     *
     * @author KevenPotter
     * @date 2026-04-27 15:20:51
     */
    private BigDecimal readLe5Byte(byte[] data, int index) {
        byte[] buf = new byte[8];
        System.arraycopy(data, index, buf, 0, 5);
        long value = ByteBuffer.wrap(buf)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getLong() & 0xFFFFFFFFFFL;
        return new BigDecimal(value);
    }

    /**
     * 解析交易表示描述
     *
     * @return 返回交易表示描述
     * @author KevenPotter
     * @date 2026-05-11 14:17:28
     */
    private static String parseTradeIdentifierDesc(Integer tradeIdentifier) {
        switch (tradeIdentifier) {
            case 0x01:
                return "APP启动";
            case 0x02:
                return "卡启动";
            case 0x04:
                return "离线卡启动";
            case 0x05:
                return "VIN码启动充电";
            default:
                return "未知启动";
        }
    }

    /**
     * 解析停止原因描述
     *
     * @return 返回停止原因描述
     * @author KevenPotter
     * @date 2026-04-27 14:37:03
     */
    private static String parseStopReasonDesc(Integer stopReason) {
        switch (stopReason) {
            // 充电完成
            case 0x40:
                return "结束充电，APP远程停止";
            case 0x41:
                return "结束充电，SOC达到100%";
            case 0x42:
                return "结束充电，充电电量满足设定条件";
            case 0x43:
                return "结束充电，充电金额满足设定条件";
            case 0x44:
                return "结束充电，充电时间满足设定条件";
            case 0x45:
                return "结束充电，手动停止充电";
            case 0x46:
            case 0x47:
            case 0x48:
            case 0x49:
                return "其他方式（预留）";
            // 充电启动失败
            case 0x4A:
                return "充电启动失败，充电桩控制系统故障";
            case 0x4B:
                return "充电启动失败，控制导引断开";
            case 0x4C:
                return "充电启动失败，断路器跳位";
            case 0x4D:
                return "充电启动失败，电表通信中断";
            case 0x4E:
                return "充电启动失败，余额不足";
            case 0x4F:
                return "充电启动失败，充电模块故障";
            case 0x50:
                return "充电启动失败，急停开入";
            case 0x51:
                return "充电启动失败，防雷器异常";
            case 0x52:
                return "充电启动失败，BMS未就绪";
            case 0x53:
                return "充电启动失败，温度异常";
            case 0x54:
                return "充电启动失败，电池反接故障";
            case 0x55:
                return "充电启动失败，电子锁异常";
            case 0x56:
                return "充电启动失败，合闸失败";
            case 0x57:
                return "充电启动失败，绝缘异常";
            case 0x58:
                return "预留";
            case 0x59:
                return "充电启动失败，接收BMS握手报文BHM超时";
            case 0x5A:
                return "充电启动失败，接收BMS和车辆的辨识报文超时BRM";
            case 0x5B:
                return "充电启动失败，接收电池充电参数报文超时BCP";
            case 0x5C:
                return "充电启动失败，接收BMS完成充电准备报文超时BRO_AA";
            case 0x5D:
                return "充电启动失败，接收电池充电总状态报文超时BCS";
            case 0x5E:
                return "充电启动失败，接收电池充电要求报文超时BCL";
            case 0x5F:
                return "充电启动失败，接收电池状态信息报文超时BSM";
            case 0x60:
                return "充电启动失败，GB2015电池在BHM阶段有电压不允许充电";
            case 0x61:
                return "充电启动失败，GB2015辨识阶段在BRO_AA时候电池实际电压与BCP报文电池电压差距大于5%";
            case 0x62:
                return "充电启动失败，GB2015充电机在预充电阶段从BRO_AA变成BRO_00状态";
            case 0x63:
                return "充电启动失败，接收主机配置报文超时";
            case 0x64:
                return "充电启动失败，充电机未准备就绪";
            case 0x65:
            case 0x66:
            case 0x67:
            case 0x68:
            case 0x69:
                return "充电启动失败，其他原因（预留）";
            // 充电异常中止
            case 0x6A:
                return "充电异常中止，系统闭锁";
            case 0x6B:
                return "充电异常中止，导引断开";
            case 0x6C:
                return "充电异常中止，断路器跳位";
            case 0x6D:
                return "充电异常中止，电表通信中断";
            case 0x6E:
                return "充电异常中止，余额不足";
            case 0x6F:
                return "充电异常中止，交流保护动作";
            case 0x70:
                return "充电异常中止，直流保护动作";
            case 0x71:
                return "充电异常中止，充电模块故障";
            case 0x72:
                return "充电异常中止，急停开入";
            case 0x73:
                return "充电异常中止，防雷器异常";
            case 0x74:
                return "充电异常中止，温度异常";
            case 0x75:
                return "充电异常中止，输出异常";
            case 0x76:
                return "充电异常中止，充电无流";
            case 0x77:
                return "充电异常中止，电子锁异常";
            case 0x78:
                return "预留";
            case 0x79:
                return "充电异常中止，总充电电压异常";
            case 0x7A:
                return "充电异常中止，总充电电流异常";
            case 0x7B:
                return "充电异常中止，单体充电电压异常";
            case 0x7C:
                return "充电异常中止，电池组过温";
            case 0x7D:
                return "充电异常中止，最高单体充电电压异常";
            case 0x7E:
                return "充电异常中止，最高电池组过温";
            case 0x7F:
                return "充电异常中止，BMV单体充电电压异常";
            case 0x80:
                return "充电异常中止，BMT电池组过温";
            case 0x81:
                return "充电异常中止，电池状态异常停止充电";
            case 0x82:
                return "充电异常中止，车辆发报文禁止充电";
            case 0x83:
                return "充电异常中止，充电桩断电";
            case 0x84:
                return "充电异常中止，接收电池充电总状态报文超时";
            case 0x85:
                return "充电异常中止，接收电池充电要求报文超时";
            case 0x86:
                return "充电异常中止，接收电池状态信息报文超时";
            case 0x87:
                return "充电异常中止，接收BMS中止充电报文超时";
            case 0x88:
                return "充电异常中止，接收BMS充电统计报文超时";
            case 0x89:
                return "充电异常中止，接收对侧CCS报文超时";
            case 0x8A:
            case 0x8B:
            case 0x8C:
            case 0x8D:
            case 0x8E:
            case 0x8F:
                return "充电异常中止，其他原因（预留）";
            case 0x90:
                return "未知原因停止";
            default:
                return "未知原因";
        }
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-11 14:08:31
     */
    private void log(String rawHexMsg) {
        log.info("------------------------------------------------------------------------------");
        log.info("🟢 【0x3B】 {} 交易记录 原始报文    rawMsg                       : {}", deviceId, rawHexMsg);
        log.info("🟢 【0x3B】 {} 交易记录 设备编号    deviceId                     : {}", deviceId, deviceId);
        log.info("🟢 【0x3B】 {} 交易记录 枪口编号    gunNo                        : {}", deviceId, gunNo);
        log.info("🟢 【0x3B】 {} 交易记录 交易编号    tradeNo                      : {}", deviceId, tradeNo);
        log.info("🟢 【0x3B】 {} 交易记录 时间范围    timeRange                    : {}-{}", deviceId, startTime, endTime);
        log.info("🟢 【0x3B】 {} 交易记录 尖时单价    sharpUnitPrice               : {}", deviceId, sharpUnitPrice);
        log.info("🟢 【0x3B】 {} 交易记录 尖时电量    sharpElectricity             : {}", deviceId, sharpElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 尖时计损    sharpLossElectricity         : {}", deviceId, sharpLossElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 尖时金额    sharpAmount                  : {}", deviceId, sharpAmount);
        log.info("🟢 【0x3B】 {} 交易记录 峰时单价    peakUnitPrice                : {}", deviceId, peakUnitPrice);
        log.info("🟢 【0x3B】 {} 交易记录 峰时电量    peakElectricity              : {}", deviceId, peakElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 峰时计损    peakLossElectricity          : {}", deviceId, peakLossElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 峰时金额    peakAmount                   : {}", deviceId, peakAmount);
        log.info("🟢 【0x3B】 {} 交易记录 平时单价    flatUnitPrice                : {}", deviceId, flatUnitPrice);
        log.info("🟢 【0x3B】 {} 交易记录 平时电量    flatElectricity              : {}", deviceId, flatElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 平时计损    flatLossElectricity          : {}", deviceId, flatLossElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 平时金额    flatAmount                   : {}", deviceId, flatAmount);
        log.info("🟢 【0x3B】 {} 交易记录 谷时单价    valleyUnitPrice              : {}", deviceId, valleyUnitPrice);
        log.info("🟢 【0x3B】 {} 交易记录 谷时电量    valleyElectricity            : {}", deviceId, valleyElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 谷时计损    valleyLossElectricity        : {}", deviceId, valleyLossElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 谷时金额    valleyAmount                 : {}", deviceId, valleyAmount);
        log.info("🟢 【0x3B】 {} 交易记录 电表起值    electricityStart             : {}", deviceId, electricityStart);
        log.info("🟢 【0x3B】 {} 交易记录 电表止值    electricityEnd               : {}", deviceId, electricityEnd);
        log.info("🟢 【0x3B】 {} 交易记录 总用电量    totalElectricity             : {}", deviceId, totalElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 计损电量    totalLossElectricity         : {}", deviceId, totalLossElectricity);
        log.info("🟢 【0x3B】 {} 交易记录 消费金额    totalAmount                  : {}", deviceId, totalAmount);
        log.info("🟢 【0x3B】 {} 交易记录 车识别码    VIN                          : {}", deviceId, vinCode);
        log.info("🟢 【0x3B】 {} 交易记录 交易标识    tradeIdentifierDesc          : {}", deviceId, tradeIdentifierDesc);
        log.info("🟢 【0x3B】 {} 交易记录 交易日期    tradeTime                    : {}", deviceId, tradeTime);
        log.info("🟢 【0x3B】 {} 交易记录 停止原因    stopReasonDesc               : {}", deviceId, stopReasonDesc);
        log.info("🟢 【0x3B】 {} 交易记录 物理卡号    physicalCardNo               : {}", deviceId, physicalCardNo);
        System.out.println();
    }

}
