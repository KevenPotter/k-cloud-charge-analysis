package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardChargingBMSInfo;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import static com.wantllife.constant.CloudFastChargingConstants.LOG_CAPACITY;
import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_CHARGING_BMS_INFO;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电过程BMS信息 [0X25]
 *
 * @author KevenPotter
 * @date 2026-06-11 11:33:27
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAMChargingBMSInfoRes extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*最高单体电压所在编号*/
    private Integer maxVoltageNo;
    /*最高动力蓄电池温度*/
    private Integer maxBatteryTemperature;
    /*最高温度检测点编号*/
    private Integer maxTemperatureCheckNo;
    /*最低蓄电池温度*/
    private Integer minBatteryTemperature;
    /*最低温度检测点编号*/
    private Integer minTemperatureCheckNo;
    /*状态位原始值*/
    private String statusBits;
    /*单体电压过高/过低描述*/
    private String voltageStatusDesc;
    /*SOC过高/过低描述*/
    private String socStatusDesc;
    /*充电过流描述*/
    private String chargeOverCurrentDesc;
    /*电池温度过高描述*/
    private String batteryTemperatureOverDesc;
    /*绝缘状态描述*/
    private String insulationStatusDesc;
    /*连接器连接状态描述*/
    private String connectorStatusDesc;
    /*充电禁止描述*/
    private String chargeForbidDesc;
    /*预留位描述*/
    private String reservedDesc;

    /**
     * 构建下发指令
     *
     * @param standardChargingBMSInfo 充电过程BMS信息
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-11 11:33:48
     */
    public static byte[] buildCommand(StandardChargingBMSInfo standardChargingBMSInfo) {
        SAMChargingBMSInfoRes res = new SAMChargingBMSInfoRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_CHARGING_BMS_INFO);
        res.setTradeNo(standardChargingBMSInfo.getTradeNo());
        res.setDeviceId(standardChargingBMSInfo.getDeviceId());
        res.setGunNo(standardChargingBMSInfo.getGunNo());
        res.setMaxVoltageNo(standardChargingBMSInfo.getMaxVoltageNo());
        res.setMaxBatteryTemperature(standardChargingBMSInfo.getMaxBatteryTemperature());
        res.setMaxTemperatureCheckNo(standardChargingBMSInfo.getMaxTemperatureCheckNo());
        res.setMinBatteryTemperature(standardChargingBMSInfo.getMinBatteryTemperature());
        res.setMinTemperatureCheckNo(standardChargingBMSInfo.getMinTemperatureCheckNo());
        res.setVoltageStatusDesc(standardChargingBMSInfo.getVoltageStatusDesc());
        res.setSocStatusDesc(standardChargingBMSInfo.getSocStatusDesc());
        res.setChargeOverCurrentDesc(standardChargingBMSInfo.getChargeOverCurrentDesc());
        res.setBatteryTemperatureOverDesc(standardChargingBMSInfo.getBatteryTemperatureOverDesc());
        res.setInsulationStatusDesc(standardChargingBMSInfo.getInsulationStatusDesc());
        res.setConnectorStatusDesc(standardChargingBMSInfo.getConnectorStatusDesc());
        res.setChargeForbidDesc(standardChargingBMSInfo.getChargeForbidDesc());
        res.setReservedDesc(standardChargingBMSInfo.getReservedDesc());
        res.setStatusBits(standardChargingBMSInfo.getVoltageStatusDesc() +
                standardChargingBMSInfo.getSocStatusDesc() +
                standardChargingBMSInfo.getChargeOverCurrentDesc() +
                standardChargingBMSInfo.getBatteryTemperatureOverDesc() +
                standardChargingBMSInfo.getInsulationStatusDesc() +
                standardChargingBMSInfo.getConnectorStatusDesc() +
                standardChargingBMSInfo.getChargeForbidDesc() +
                standardChargingBMSInfo.getReservedDesc()
        );

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
     * @date 2026-06-11 11:34:26
     */
    private byte[] buildBody() {
        byte[] body = new byte[31];

        // 交易流水号 [16字节] [BCD]
        String tradeNoFull = StrUtil.padPre(this.tradeNo, 32, '0');
        byte[] tradeNoBcd = StringUtil.string2bcd(tradeNoFull);
        System.arraycopy(tradeNoBcd, 0, body, 0, 16);
        // 设备编号 [7字节] [BCD]
        String deviceIdFull = StrUtil.padPre(this.deviceId, 14, '0');
        byte[] deviceBcd = StringUtil.string2bcd(deviceIdFull);
        System.arraycopy(deviceBcd, 0, body, 16, 7);
        // 枪号 [1字节] [BCD]
        body[23] = StringUtil.string2bcd(StrUtil.padPre(gunNo.toString(), 2, '0'))[0];
        // 最高单体电压所在编号 [1字节] [BIN]
        body[24] = (byte) ((maxVoltageNo - 1) & 0xFF);
        // 最高动力蓄电池温度 [1字节] [BIN]
        body[25] = (byte) (maxBatteryTemperature + 50);
        // 最高温度检测点编号 [1字节] [BIN]
        body[26] = (byte) ((maxTemperatureCheckNo - 1) & 0xFF);
        // 最低蓄电池温度 [1字节] [BIN]
        body[27] = (byte) (minBatteryTemperature + 50);
        // 最低温度检测点编号 [1字节] [BIN]
        body[28] = (byte) ((minTemperatureCheckNo - 1) & 0xFF);
        // 状态位原始值 [2字节] [BIN]
        String statusBin = voltageStatusDesc
                + socStatusDesc
                + chargeOverCurrentDesc
                + batteryTemperatureOverDesc
                + insulationStatusDesc
                + connectorStatusDesc
                + chargeForbidDesc
                + reservedDesc;
        int statusRaw = Integer.parseInt(statusBin, 2);
        body[29] = (byte) ((statusRaw >> 8) & 0xFF);
        body[30] = (byte) (statusRaw & 0xFF);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-11 11:35:01
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    private void log(String rawHexMsg) {
        StringBuilder sb = new StringBuilder(LOG_CAPACITY);
        String devLabel = PURPLE + "⇓ 【0x25】 " + deviceId + RESET;
        sb.append("\n\n");
        sb.append(String.format("👩‍🚀%s 电池管理信息  原始报文    rawMsg                       : %s\n", devLabel, rawHexMsg));
        sb.append(String.format("👩‍🚀%s 电池管理信息  设备编号    deviceId                     : %s\n", devLabel, deviceId));
        sb.append(String.format("👩‍🚀%s 电池管理信息  枪口编号    gunNo                        : %s\n", devLabel, gunNo));
        sb.append(String.format("👩‍🚀%s 电池管理信息  交易编号    tradeNo                      : %s\n", devLabel, tradeNo));
        sb.append(String.format("👩‍🚀%s 电池管理信息  单压编号    maxVoltageNo                 : %s\n", devLabel, maxVoltageNo));
        sb.append(String.format("👩‍🚀%s 电池管理信息  最高温度    maxBatteryTemperature        : %s\n", devLabel, maxBatteryTemperature));
        sb.append(String.format("👩‍🚀%s 电池管理信息  检测高温    maxTemperatureCheckNo        : %s\n", devLabel, maxTemperatureCheckNo));
        sb.append(String.format("👩‍🚀%s 电池管理信息  最低温度    minBatteryTemperature        : %s\n", devLabel, minBatteryTemperature));
        sb.append(String.format("👩‍🚀%s 电池管理信息  检测低温    minTemperatureCheckNo        : %s\n", devLabel, minTemperatureCheckNo));
        sb.append(String.format("👩‍🚀%s 电池管理信息  状态原值    statusBits                   : %s\n", devLabel, statusBits));
        sb.append(String.format("👩‍🚀%s 电池管理信息  电压高低    voltageStatusDesc            : %s\n", devLabel, voltageStatusDesc));
        sb.append(String.format("👩‍🚀%s 电池管理信息  率值高低    socStatusDesc                : %s\n", devLabel, socStatusDesc));
        sb.append(String.format("👩‍🚀%s 电池管理信息  充电过流    chargeOverCurrentDesc        : %s\n", devLabel, chargeOverCurrentDesc));
        sb.append(String.format("👩‍🚀%s 电池管理信息  温度过高    batteryTemperatureOverDesc   : %s\n", devLabel, batteryTemperatureOverDesc));
        sb.append(String.format("👩‍🚀%s 电池管理信息  绝缘状态    insulationStatusDesc         : %s\n", devLabel, insulationStatusDesc));
        sb.append(String.format("👩‍🚀%s 电池管理信息  连接状态    connectorStatusDesc          : %s\n", devLabel, connectorStatusDesc));
        sb.append(String.format("👩‍🚀%s 电池管理信息  充电禁止    chargeForbidDesc             : %s\n", devLabel, chargeForbidDesc));
        sb.append(String.format("👩‍🚀%s 电池管理信息  预留位值    reservedDesc                 : %s\n", devLabel, reservedDesc));
        log.info(sb.toString());
    }
}
