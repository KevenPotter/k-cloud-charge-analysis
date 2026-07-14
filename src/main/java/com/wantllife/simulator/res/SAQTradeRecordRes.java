package com.wantllife.simulator.res;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardTradeRecord;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_TRADE_RECORD;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;
import static com.wantllife.util.TimeUtil.dateToCp56Bytes;

/**
 * 交易记录 [0X3B]
 *
 * @author KevenPotter
 * @date 2026-06-12 11:46:47
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAQTradeRecordRes extends FrameHeader {

    /** 价格保留小数位 */
    private static final int SCALE_PRICE = 5;
    /** 电量保留小数位 */
    private static final int SCALE_ELECTRIC = 4;

    /** 交易流水号 */
    private String tradeNo;
    /** 设备编号 */
    private String deviceId;
    /** 枪号 */
    private Integer gunNo;
    /** 开始时间 */
    private Date startTime;
    /** 结束时间 */
    private Date endTime;

    /** 尖单价 */
    private BigDecimal sharpUnitPrice;
    /** 尖电量 */
    private BigDecimal sharpElectricity;
    /** 计损尖电量 */
    private BigDecimal sharpLossElectricity;
    /** 尖金额 */
    private BigDecimal sharpAmount;

    /** 峰单价 */
    private BigDecimal peakUnitPrice;
    /** 峰电量 */
    private BigDecimal peakElectricity;
    /** 计损峰电量 */
    private BigDecimal peakLossElectricity;
    /** 峰金额 */
    private BigDecimal peakAmount;

    /** 平单价 */
    private BigDecimal flatUnitPrice;
    /** 平电量 */
    private BigDecimal flatElectricity;
    /** 计损平电量 */
    private BigDecimal flatLossElectricity;
    /** 平金额 */
    private BigDecimal flatAmount;

    /** 谷单价 */
    private BigDecimal valleyUnitPrice;
    /** 谷电量 */
    private BigDecimal valleyElectricity;
    /** 计损谷电量 */
    private BigDecimal valleyLossElectricity;
    /** 谷金额 */
    private BigDecimal valleyAmount;

    /** 电表总起值 */
    private BigDecimal electricityStart;
    /** 电表总止值 */
    private BigDecimal electricityEnd;
    /** 总电量 */
    private BigDecimal totalElectricity;
    /** 计损总电量 */
    private BigDecimal totalLossElectricity;

    /** 消费金额 */
    private BigDecimal totalAmount;
    /** 电动汽车唯一标识 */
    private String vinCode;
    /** 交易标识 */
    private Integer tradeIdentifier;
    /** 交易表示描述 */
    private String tradeIdentifierDesc;
    /** 交易日期、时间 */
    private Date tradeTime;
    /** 停止原因 */
    private Integer stopReason;
    /** 停止原因描述 */
    private String stopReasonDesc;
    /** 物理卡号 */
    private String physicalCardNo;

    /**
     * 构建下发指令
     *
     * @param tradeRecord 交易记录
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-12 11:47:20
     */
    public static byte[] buildCommand(StandardTradeRecord tradeRecord) {
        SAQTradeRecordRes res = new SAQTradeRecordRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_TRADE_RECORD);
        res.setTradeNo(tradeRecord.getTradeNo());
        res.setDeviceId(tradeRecord.getDeviceId());
        res.setGunNo(tradeRecord.getGunNo());
        res.setStartTime(tradeRecord.getStartTime());
        res.setEndTime(tradeRecord.getEndTime());

        res.setSharpUnitPrice(tradeRecord.getSharpUnitPrice());
        res.setSharpElectricity(tradeRecord.getSharpElectricity());
        res.setSharpLossElectricity(tradeRecord.getSharpLossElectricity());
        res.setSharpAmount(tradeRecord.getSharpAmount());

        res.setPeakUnitPrice(tradeRecord.getPeakUnitPrice());
        res.setPeakElectricity(tradeRecord.getPeakElectricity());
        res.setPeakLossElectricity(tradeRecord.getPeakLossElectricity());
        res.setPeakAmount(tradeRecord.getPeakAmount());

        res.setFlatUnitPrice(tradeRecord.getFlatUnitPrice());
        res.setFlatElectricity(tradeRecord.getFlatElectricity());
        res.setFlatLossElectricity(tradeRecord.getFlatLossElectricity());
        res.setFlatAmount(tradeRecord.getFlatAmount());

        res.setValleyUnitPrice(tradeRecord.getValleyUnitPrice());
        res.setValleyElectricity(tradeRecord.getValleyElectricity());
        res.setValleyLossElectricity(tradeRecord.getValleyLossElectricity());
        res.setValleyAmount(tradeRecord.getValleyAmount());

        res.setElectricityStart(tradeRecord.getElectricityStart());
        res.setElectricityEnd(tradeRecord.getElectricityEnd());
        res.setTotalElectricity(tradeRecord.getTotalElectricity());
        res.setTotalLossElectricity(tradeRecord.getTotalLossElectricity());

        res.setTotalAmount(tradeRecord.getTotalAmount());
        res.setVinCode(tradeRecord.getVinCode());
        res.setTradeIdentifier(tradeRecord.getTradeIdentifier());
        res.setTradeIdentifierDesc(parseTradeIdentifierDesc(tradeRecord.getTradeIdentifier()));
        res.setTradeTime(tradeRecord.getTradeTime());
        res.setStopReason(tradeRecord.getStopReason());
        res.setStopReasonDesc(parseStopReasonDesc(tradeRecord.getStopReason()));
        res.setPhysicalCardNo(tradeRecord.getPhysicalCardNo());

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
     * @date 2026-06-12 11:47:37
     */
    private byte[] buildBody() {
        byte[] body = new byte[158];
        // 交易流水号 [16字节] [BCD]
        String tradeNoFull = StrUtil.padPre(this.tradeNo, 32, '0');
        byte[] tradeNoBuf = StringUtil.string2bcd(tradeNoFull);
        System.arraycopy(tradeNoBuf, 0, body, 0, 16);
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceIdBuf = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceIdBuf, 0, body, 16, 7);
        // 枪号 [1字节] [BIN]
        body[23] = (byte) (this.gunNo & 0xFF);
        // 开始时间 [7字节] [BIN] CP56Time2a
        byte[] startTimeBuf = dateToCp56Bytes(this.startTime);
        System.arraycopy(startTimeBuf, 0, body, 24, 7);
        // 结束时间 [7字节] [BIN] CP56Time2a
        byte[] endTimeBuf = dateToCp56Bytes(this.endTime);
        System.arraycopy(endTimeBuf, 0, body, 31, 7);

        // 尖单价 [4字节] [BIN] 小端
        writeLe4Byte(body, 38, this.sharpUnitPrice.movePointRight(SCALE_PRICE).longValue());
        // 尖电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 42, this.sharpElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 计损尖电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 46, this.sharpLossElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 尖金额 [4字节] [BIN] 小端
        writeLe4Byte(body, 50, this.sharpAmount.movePointRight(SCALE_ELECTRIC).longValue());

        // 峰单价 [4字节] [BIN] 小端
        writeLe4Byte(body, 54, this.peakUnitPrice.movePointRight(SCALE_PRICE).longValue());
        // 峰电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 58, this.peakElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 计损峰电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 62, this.peakLossElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 峰金额 [4字节] [BIN] 小端
        writeLe4Byte(body, 66, this.peakAmount.movePointRight(SCALE_ELECTRIC).longValue());

        // 平单价 [4字节] [BIN] 小端
        writeLe4Byte(body, 70, this.flatUnitPrice.movePointRight(SCALE_PRICE).longValue());
        // 平电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 74, this.flatElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 计损平电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 78, this.flatLossElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 平金额 [4字节] [BIN] 小端
        writeLe4Byte(body, 82, this.flatAmount.movePointRight(SCALE_ELECTRIC).longValue());

        // 谷单价 [4字节] [BIN] 小端
        writeLe4Byte(body, 86, this.valleyUnitPrice.movePointRight(SCALE_PRICE).longValue());
        // 谷电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 90, this.valleyElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 计损谷电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 94, this.valleyLossElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 谷金额 [4字节] [BIN] 小端
        writeLe4Byte(body, 98, this.valleyAmount.movePointRight(SCALE_ELECTRIC).longValue());

        // 电表总起值 [5字节] [BIN] 小端
        writeLe5Byte(body, 102, this.electricityStart.movePointRight(SCALE_ELECTRIC).longValue());
        // 电表总止值 [5字节] [BIN] 小端
        writeLe5Byte(body, 107, this.electricityEnd.movePointRight(SCALE_ELECTRIC).longValue());
        // 总电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 112, this.totalElectricity.movePointRight(SCALE_ELECTRIC).longValue());
        // 计损总电量 [4字节] [BIN] 小端
        writeLe4Byte(body, 116, this.totalLossElectricity.movePointRight(SCALE_ELECTRIC).longValue());

        // 消费金额 [4字节] [BIN] 小端
        writeLe4Byte(body, 120, this.totalAmount.movePointRight(SCALE_ELECTRIC).longValue());
        // VIN码 [17字节] [ASCII]
        byte[] vinBuf = new byte[17];
        byte[] vinRaw = StrUtil.padAfter(this.vinCode, 17, '0').getBytes();
        System.arraycopy(vinRaw, 0, vinBuf, 0, 17);
        System.arraycopy(vinBuf, 0, body, 124, 17);
        // 交易标识 [1字节] [BIN]
        body[141] = this.tradeIdentifier.byteValue();
        // 交易时间 [7字节] [BIN] CP56Time2a
        byte[] tradeTimeBuf = dateToCp56Bytes(this.tradeTime);
        System.arraycopy(tradeTimeBuf, 0, body, 142, 7);
        // 停止原因 [1字节] [BIN]
        body[149] = this.stopReason.byteValue();
        // 物理卡号 [8字节] [BIN]
        byte[] cardBuf = new byte[8];
        String cardHex = StrUtil.padPre(StrUtil.blankToDefault(this.physicalCardNo, ""), 16, '0');
        byte[] cardRaw = HexUtil.decodeHex(cardHex);
        System.arraycopy(cardRaw, 0, cardBuf, 0, 8);
        System.arraycopy(cardBuf, 0, body, 150, 8);

        return body;
    }

    /**
     * 小端写入4字节无符号整数
     *
     * @param dest 目标数组
     * @param pos  固定起始偏移
     * @param val  放大后数值
     * @author KevenPotter
     * @date 2026-06-12 15:41:05
     */
    private void writeLe4Byte(byte[] dest, int pos, long val) {
        byte[] temp = ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt((int) (val & 0xFFFFFFFFL))
                .array();
        System.arraycopy(temp, 0, dest, pos, 4);
    }

    /**
     * 小端写入5字节无符号整数
     *
     * @param dest 目标数组
     * @param pos  固定起始偏移
     * @param val  放大后数值
     * @author KevenPotter
     * @date 2026-06-12 15:41:19
     */
    private void writeLe5Byte(byte[] dest, int pos, long val) {
        byte[] temp = ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(val & 0xFFFFFFFFFFL)
                .array();
        System.arraycopy(temp, 0, dest, pos, 5);
    }

    /**
     * 解析交易表示描述
     *
     * @return 返回交易表示描述
     * @author KevenPotter
     * @date 2026-06-12 11:49:17
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
     * @date 2026-06-12 11:48:35
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
     * 格式化价格(SCALE_PRICE位小数)
     *
     * @param num 数值
     * @author KevenPotter
     * @date 2026-06-23 11:30:16
     */
    private String fmtPr(BigDecimal num) {
        return num.setScale(SCALE_PRICE, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 格式化电量/金额/电表值(SCALE_ELECTRIC位小数)
     *
     * @param num 数值
     * @author KevenPotter
     * @date 2026-06-23 11:30:16
     */
    private String fmtEl(BigDecimal num) {
        return num.setScale(SCALE_ELECTRIC, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-12 11:49:06
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x3B】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 交易记录上报  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 交易记录上报  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 交易记录上报  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 交易记录上报  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 交易记录上报  时间范围    timeRange                    : %s-%s\n", devLabel, startTime, endTime));
        sb.append(String.format("👩‍🚀%s 交易记录上报  充电时常    chargeDuration               : %s\n", devLabel, DateUtil.between(startTime, endTime, DateUnit.MINUTE)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  尖时单价    sharpUnitPrice               : %s\n", devLabel, fmtPr(sharpUnitPrice)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  尖时电量    sharpElectricity             : %s\n", devLabel, fmtEl(sharpElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  尖时计损    sharpLossElectricity         : %s\n", devLabel, fmtEl(sharpLossElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  尖时金额    sharpAmount                  : %s\n", devLabel, fmtEl(sharpAmount)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  峰时单价    peakUnitPrice                : %s\n", devLabel, fmtPr(peakUnitPrice)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  峰时电量    peakElectricity              : %s\n", devLabel, fmtEl(peakElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  峰时计损    peakLossElectricity          : %s\n", devLabel, fmtEl(peakLossElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  峰时金额    peakAmount                   : %s\n", devLabel, fmtEl(peakAmount)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  平时单价    flatUnitPrice                : %s\n", devLabel, fmtPr(flatUnitPrice)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  平时电量    flatElectricity              : %s\n", devLabel, fmtEl(flatElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  平时计损    flatLossElectricity          : %s\n", devLabel, fmtEl(flatLossElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  平时金额    flatAmount                   : %s\n", devLabel, fmtEl(flatAmount)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  谷时单价    valleyUnitPrice              : %s\n", devLabel, fmtPr(valleyUnitPrice)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  谷时电量    valleyElectricity            : %s\n", devLabel, fmtEl(valleyElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  谷时计损    valleyLossElectricity        : %s\n", devLabel, fmtEl(valleyLossElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  谷时金额    valleyAmount                 : %s\n", devLabel, fmtEl(valleyAmount)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  电表起值    electricityStart             : %s\n", devLabel, fmtEl(electricityStart)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  电表止值    electricityEnd               : %s\n", devLabel, fmtEl(electricityEnd)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  总用电量    totalElectricity             : %s\n", devLabel, fmtEl(totalElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  计损电量    totalLossElectricity         : %s\n", devLabel, fmtEl(totalLossElectricity)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  消费金额    totalAmount                  : %s\n", devLabel, fmtEl(totalAmount)));
        sb.append(String.format("👩‍🚀%s 交易记录上报  车识别码    VIN                          : %s\n", devLabel, vinCode));
        sb.append(String.format("👩‍🚀%s 交易记录上报  交易标识    tradeIdentifierDesc          : %s\n", devLabel, tradeIdentifierDesc));
        sb.append(String.format("👩‍🚀%s 交易记录上报  交易日期    tradeTime                    : %s\n", devLabel, tradeTime));
        sb.append(String.format("👩‍🚀%s 交易记录上报  停止原因    stopReasonDesc               : %s\n", devLabel, stopReasonDesc));
        sb.append(String.format("👩‍🚀%s 交易记录上报  物理卡号    physicalCardNo               : %s\n", devLabel, physicalCardNo));
        log.info(sb.toString());
    }
}
