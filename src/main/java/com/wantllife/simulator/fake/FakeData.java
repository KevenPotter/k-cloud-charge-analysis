package com.wantllife.simulator.fake;

import cn.hutool.core.util.RandomUtil;
import com.wantllife.domain.vo.StandardRealTimeMonitor;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.wantllife.util.StringUtil.generateSerial;

/**
 * 虚拟数据
 *
 * @author KevenPotter
 * @date 2026-06-02 10:08:11
 */
public class FakeData {

    /**
     * 虚拟初始化实时监控数据
     *
     * @param deviceId 设备编号
     * @param gunNo    枪号
     * @return 返回虚拟初始化实时监控数据
     * @author KevenPotter
     * @date 2026-06-02 10:09:32
     */
    public static StandardRealTimeMonitor fakeInitRealTimeMonitor(String deviceId, Integer gunNo) {
        String tradeNo = generateSerial(deviceId, gunNo);
        return new StandardRealTimeMonitor(
                tradeNo, deviceId, gunNo,
                2, 1, 1,
                BigDecimal.ZERO, BigDecimal.ZERO, 0,
                "0000000000000000", 0, -50, 0, 0,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                "00"
        );
    }

    /**
     * 虚拟充电种实时监控数据
     *
     * @param tradeNo                 交易流水号
     * @param deviceId                设备编号
     * @param gunNo                   枪号
     * @param accumulatedChargingTime 累计充电时间
     * @param remainingChargingTime   剩余时间
     * @param chargingDegree          充电度数
     * @param chargedAmount           已充金额
     * @return 返回虚拟充电中实时监控数据
     * @author KevenPotter
     * @date 2026-06-02 10:13:54
     */
    public static StandardRealTimeMonitor fakeChargingRealTimeMonitor(
            String tradeNo, String deviceId, Integer gunNo,
            int accumulatedChargingTime, int remainingChargingTime,
            BigDecimal chargingDegree, BigDecimal chargedAmount
    ) {
        // 1.输出电压 0.0~500.0V ,1位小数
        BigDecimal voltage = BigDecimal.valueOf(RandomUtil.randomDouble(0, 500)).setScale(1, RoundingMode.HALF_UP);
        // 2.输出电流 0.0~250.0A ,1位小数
        BigDecimal current = BigDecimal.valueOf(RandomUtil.randomDouble(0, 250)).setScale(1, RoundingMode.HALF_UP);
        // 3.枪线温度 30~130 (对应真实-20℃~80℃)
        Integer temperature = RandomUtil.randomInt(30, 131);
        // 组装
        return new StandardRealTimeMonitor(
                tradeNo, deviceId, gunNo,
                3, 2, 1,
                voltage, current, temperature,
                "A00001", 0, -50, accumulatedChargingTime, remainingChargingTime,
                chargingDegree, BigDecimal.ZERO, chargedAmount,
                "00"
        );
    }
}
