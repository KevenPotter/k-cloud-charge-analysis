package com.wantllife.analysis.res;

import cn.hutool.core.util.HexUtil;
import com.wantllife.analysis.FrameHeader;
import com.wantllife.analysis.req.ADBillingModelReq;
import com.wantllife.domain.vo.StandardBillingModel;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.wantllife.constant.CloudFastChargingConstants.DOWN_BILLING_MODE;


/**
 * 计费模型请求应答 [0X0A]
 *
 * @author KevenPotter
 * @date 2026-04-22 17:01:26
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ADBillingModelRes extends FrameHeader {

    /*尖*/
    private static final int SHARP = 0;
    /*峰*/
    private static final int PEAK = 1;
    /*平*/
    private static final int FLAT = 2;
    /*谷*/
    private static final int VALLEY = 3;

    /*设备编号*/
    private String deviceId;
    /*计费模型编码*/
    private String billingModeId;
    /*尖电费费率*/
    private BigDecimal sharpEleFee;
    /*尖服务费费率*/
    private BigDecimal sharpServiceFee;
    /*峰费电费费率*/
    private BigDecimal peakEleFee;
    /*峰服务费费率*/
    private BigDecimal peakServiceFee;
    /*平电费费率*/
    private BigDecimal flatEleFee;
    /*平服务费费率*/
    private BigDecimal flatServiceFee;
    /*谷电费费率*/
    private BigDecimal valleyEleFee;
    /*谷服务费费率*/
    private BigDecimal valleyServiceFee;
    /*计损比例*/
    private String lossRatio;
    /*时段费率*/
    private byte[] timeSlotRates = new byte[48];


    /**
     * 构建下发指令
     *
     * @param req 充电桩心跳包
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-04-22 17:01:32
     */
    public static byte[] buildCommand(ADBillingModelReq req, List<StandardBillingModel> billingModelList) {
        StandardBillingModel billingModel = billingModelList.get(0);
        ADBillingModelRes res = new ADBillingModelRes();
        res.setSeqNo(req.getSeqNo());
        res.setFrameType(DOWN_BILLING_MODE);
        res.setDeviceId(req.getDeviceId());
        res.setBillingModeId(String.valueOf(billingModel.getStrategyId()));

        for (StandardBillingModel mode : billingModelList) {
            switch (mode.getTimeSlotType()) {
                case 1:
                    res.setSharpEleFee(mode.getElectricityFee());
                    res.setSharpServiceFee(mode.getServiceFee());
                    break;
                case 2:
                    res.setPeakEleFee(mode.getElectricityFee());
                    res.setPeakServiceFee(mode.getServiceFee());
                    break;
                case 3:
                    res.setFlatEleFee(mode.getElectricityFee());
                    res.setFlatServiceFee(mode.getServiceFee());
                    break;
                case 4:
                    res.setValleyEleFee(mode.getElectricityFee());
                    res.setValleyServiceFee(mode.getServiceFee());
                    break;
            }
        }

        res.setLossRatio("00");
        res.setTimeSlotRates(buildDynamicTimeSlots(billingModelList));

        byte[] body = res.buildBody();
        byte[] downMessage = res.buildDownMessage(body);

        // 记录日志
        res.log(HexUtil.encodeHexStr(downMessage), billingModelList);

        return downMessage;
    }

    /**
     * 构建消息体
     *
     * @return 返回消息体
     * @author KevenPotter
     * @date 2026-04-22 17:02:00
     */
    private byte[] buildBody() {
        byte[] body = new byte[90];
        // 设备编号 [7字节] [BCD]
        byte[] deviceBcd = StringUtil.string2bcd(this.deviceId);
        System.arraycopy(deviceBcd, 0, body, 0, 7);
        // 计费模型编码 [2字节] [BCD]
        byte[] billingModeBcd = StringUtil.string2bcd(this.billingModeId);
        System.arraycopy(billingModeBcd, 0, body, 7, 2);
        // 尖电费费率 [4字节] [BIN]
        byte[] sharpEle = decimal2Bin4(this.sharpEleFee);
        System.arraycopy(sharpEle, 0, body, 9, 4);
        // 尖服务费费率 [4字节] [BIN]
        byte[] sharpSer = decimal2Bin4(this.sharpServiceFee);
        System.arraycopy(sharpSer, 0, body, 13, 4);
        // 峰电费费率 [4字节] [BIN]
        byte[] peakEle = decimal2Bin4(this.peakEleFee);
        System.arraycopy(peakEle, 0, body, 17, 4);
        // 峰服务费费率 [4字节] [BIN]
        byte[] peakSer = decimal2Bin4(this.peakServiceFee);
        System.arraycopy(peakSer, 0, body, 21, 4);
        // 平电费费率 [4字节] [BIN]
        byte[] flatEle = decimal2Bin4(this.flatEleFee);
        System.arraycopy(flatEle, 0, body, 25, 4);
        // 平服务费费率 [4字节] [BIN]
        byte[] flatSer = decimal2Bin4(this.flatServiceFee);
        System.arraycopy(flatSer, 0, body, 29, 4);
        // 谷电费费率 [4字节] [BIN]
        byte[] valleyEle = decimal2Bin4(this.valleyEleFee);
        System.arraycopy(valleyEle, 0, body, 33, 4);
        // 谷服务费费率 [4字节] [BIN]
        byte[] valleySer = decimal2Bin4(this.valleyServiceFee);
        System.arraycopy(valleySer, 0, body, 37, 4);
        // 计损比例 [1字节] [BIN]
        body[41] = Byte.parseByte(this.lossRatio);
        // 时段费率 [48字节] [BIN]
        System.arraycopy(this.timeSlotRates, 0, body, 42, 48);
        return body;
    }

    /**
     * 动态构建48个时段费率
     * 根据每个时段的开始时间和结束时间,自动匹配尖峰平谷类型
     *
     * @param billingModelList 计费模型列表
     * @return 48个时段的费率编号数组
     * @author KevenPotter
     * @date 2026-04-23 16:41:20
     */
    private static byte[] buildDynamicTimeSlots(List<StandardBillingModel> billingModelList) {
        // 定义48个时段数组,每个元素代表一个30分钟时间段的费率类型
        byte[] slots = new byte[48];

        // 遍历48个半小时时段：i=0 → 00:00-00:30 ... i=47 → 23:30-00:00
        for (int i = 0; i < 48; i++) {
            // 当前时段开始分钟数(0,30,60...1410)
            int startMin = i * 30;
            // 当前时段结束分钟数
            int endMin = (i + 1) * 30;

            // 遍历所有计费模型(尖、峰、平、谷)
            for (StandardBillingModel model : billingModelList) {
                // 获取模型的起止时间与时段类型
                String startTime = model.getStartTime();
                String endTime = model.getEndTime();
                int type = model.getTimeSlotType();

                // 将时间字符串HH:mm转换为当天分钟数
                int sMin = timeToMin(startTime);
                int eMin = timeToMin(endTime);

                // 标记当前时段是否落在该计费模型的时间范围内
                boolean inRange;
                // 判断是否跨天(如22:00-07:00)
                if (sMin > eMin) {
                    // 跨天:落在起始时间到24点 或 0点到结束时间都算命中
                    inRange = (startMin >= sMin && startMin < 1440) || (startMin >= 0 && startMin < eMin);
                } else {
                    // 不跨天:直接判断区间
                    inRange = startMin >= sMin && startMin < eMin;
                }

                if (inRange) {
                    switch (type) {
                        case 1:
                            slots[i] = SHARP;
                            break;
                        case 2:
                            slots[i] = PEAK;
                            break;
                        case 3:
                            slots[i] = FLAT;
                            break;
                        case 4:
                            slots[i] = VALLEY;
                            break;
                        default:
                            slots[i] = 3;
                    }
                    break;
                }
            }
        }
        return slots;
    }

    /**
     * 将时间字符串(HH:mm)转换为当天分钟数
     * 例如:07:30 → 450
     *
     * @param time 时间字符串
     * @return 转换后的分钟数
     * @author KevenPotter
     * @date 2026-04-23 16:40:20
     */
    private static int timeToMin(String time) {
        String[] arr = time.split(":");
        int h = Integer.parseInt(arr[0]);
        int m = Integer.parseInt(arr[1]);
        return h * 60 + m;
    }

    /**
     * 将BigDecimal类型的费率转换为4字节BIN码(低位在前)
     * 精确到5位小数,符合云快充协议规范
     *
     * @param fee 费率数值
     * @return 4字节小端模式数组
     * @author KevenPotter
     * @date 2026-04-23 16:38:20
     */
    public byte[] decimal2Bin4(BigDecimal fee) {
        BigDecimal value = fee.setScale(5, RoundingMode.HALF_UP);
        long val = value.movePointRight(5).longValue();
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xFF);
        b[1] = (byte) ((val >> 8) & 0xFF);
        b[2] = (byte) ((val >> 16) & 0xFF);
        b[3] = (byte) ((val >> 24) & 0xFF);
        return b;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-05-09 16:46:10
     */
    private void log(String rawHexMsg, List<StandardBillingModel> billingModelList) {
        log.info("------------------------------------------------------------------------------");
        log.info("🔶 【0x0A】 {} 计费模型请求应答 原始报文    rawMsg               : {}", deviceId, rawHexMsg);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 设备编号    deviceId             : {}", deviceId, deviceId);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 计费编码    billingModeId        : {}", deviceId, billingModeId);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 尖电费率    sharpEleFee          : {}", deviceId, sharpEleFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 尖服费率    sharpServiceFee      : {}", deviceId, sharpServiceFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 峰电费率    peakEleFee           : {}", deviceId, peakEleFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 峰服费率    peakServiceFee       : {}", deviceId, peakServiceFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 平电费率    flatEleFee           : {}", deviceId, flatEleFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 平服费率    flatServiceFee       : {}", deviceId, flatServiceFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 谷电费率    valleyEleFee         : {}", deviceId, valleyEleFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 谷服费率    valleyServiceFee     : {}", deviceId, valleyServiceFee);
        log.info("🔶 【0x0A】 {} 计费模型请求应答 计损比例    lossRatio            : {}", deviceId, lossRatio);
        for (StandardBillingModel mode : billingModelList) {
            switch (mode.getTimeSlotType()) {
                case 1:
                    log.info("🔶 【0x0A】 {} 计费模型请求应答 尖时间段    sharpTime            : {}-{}", deviceId, mode.getStartTime(), mode.getEndTime());
                    break;
                case 2:
                    log.info("🔶 【0x0A】 {} 计费模型请求应答 峰时间段    peakTime             : {}-{}", deviceId, mode.getStartTime(), mode.getEndTime());
                    break;
                case 3:
                    log.info("🔶 【0x0A】 {} 计费模型请求应答 平时间段    flatTime             : {}-{}", deviceId, mode.getStartTime(), mode.getEndTime());
                    break;
                case 4:
                    log.info("🔶 【0x0A】 {} 计费模型请求应答 谷时间段    valleyTime           : {}-{}", deviceId, mode.getStartTime(), mode.getEndTime());
                    break;
            }
        }
        System.out.println();
    }
}
