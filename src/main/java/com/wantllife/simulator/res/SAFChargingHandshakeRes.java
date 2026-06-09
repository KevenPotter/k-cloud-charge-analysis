package com.wantllife.simulator.res;

import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wantllife.config.holder.CloudChargeHolder;
import com.wantllife.core.FrameHeader;
import com.wantllife.domain.vo.StandardChargingHandshake;
import com.wantllife.util.StringUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static com.wantllife.constant.CloudFastChargingConstants.SIM_DOWN_CHARGING_HANDSHAKE;
import static com.wantllife.constant.ColorConstants.PURPLE;
import static com.wantllife.constant.ColorConstants.RESET;

/**
 * 充电握手 [0x15]
 *
 * @author KevenPotter
 * @date 2026-06-09 09:46:02
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class SAFChargingHandshakeRes extends FrameHeader {

    /*交易流水号*/
    private String tradeNo;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*BMS通信协议版本号*/
    private String communicationProtocolVersion;
    /*电池类型*/
    private Integer batteryType;
    /*整车动力蓄电池系统额定容量*/
    private BigDecimal batteryRated;
    /*整车动力蓄电池系统额定总电压*/
    private BigDecimal batteryTotalVoltage;
    /*电池生产厂商名称*/
    private String batteryManufacturer;
    /*电池组序号*/
    private String batterySerialNo;
    /*电池组生产日期年*/
    private Integer batteryProductionYear;
    /*电池组生产日期月*/
    private Integer batteryProductionMonth;
    /*电池组生产日期日*/
    private Integer batteryProductionDay;
    /*电池组充电次数*/
    private Integer batteryChargeCounts;
    /*电池组产权标识*/
    private Integer batteryPropertyIdentification;
    /*预留位*/
    private String reserved;
    /*车辆识别码*/
    private String vin;
    /*软件版本号*/
    private String softwareVersion;

    /**
     * 构建下发指令
     *
     * @param chargingHandshake 充电握手
     * @return 返回下发指令
     * @author KevenPotter
     * @date 2026-06-09 09:46:50
     */
    public static byte[] buildCommand(StandardChargingHandshake chargingHandshake) {
        SAFChargingHandshakeRes res = new SAFChargingHandshakeRes();
        res.setSeqNo(RandomUtil.randomNumbers(4));
        res.setFrameType(SIM_DOWN_CHARGING_HANDSHAKE);
        res.setTradeNo(chargingHandshake.getTradeNo());
        res.setDeviceId(chargingHandshake.getDeviceId());
        res.setGunNo(chargingHandshake.getGunNo());
        res.setCommunicationProtocolVersion(chargingHandshake.getCommunicationProtocolVersion());
        res.setBatteryType(chargingHandshake.getBatteryType());
        res.setBatteryRated(chargingHandshake.getBatteryRated());
        res.setBatteryTotalVoltage(chargingHandshake.getBatteryTotalVoltage());
        res.setBatteryManufacturer(chargingHandshake.getBatteryManufacturer());
        res.setBatterySerialNo(chargingHandshake.getBatterySerialNo());
        res.setBatteryProductionYear(chargingHandshake.getBatteryProductionYear());
        res.setBatteryProductionMonth(chargingHandshake.getBatteryProductionMonth());
        res.setBatteryProductionDay(chargingHandshake.getBatteryProductionDay());
        res.setBatteryChargeCounts(chargingHandshake.getBatteryChargeCounts());
        res.setBatteryPropertyIdentification(chargingHandshake.getBatteryPropertyIdentification());
        res.setReserved(chargingHandshake.getReserved());
        res.setVin(chargingHandshake.getVin());
        res.setSoftwareVersion(chargingHandshake.getSoftwareVersion());

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
     * @date 2026-06-09 09:48:11
     */
    private byte[] buildBody() {
        byte[] body = new byte[73];

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
        // BMS通信协议版本号 [3字节] [BIN]
        body[24] = 0x00;
        body[25] = 0x01;
        body[26] = 0x01;
        // 电池类型 [1字节] [BIN]
        body[27] = (byte) (batteryType & 0xFF);
        // 整车动力蓄电池系统额定容量 [2字节] [BIN]
        int rated = batteryRated.multiply(new BigDecimal(10)).intValue();
        body[28] = (byte) ((rated >> 8) & 0xFF);
        body[29] = (byte) (rated & 0xFF);
        // 整车动力蓄电池系统额定总电压 [2字节] [BIN]
        int voltage = batteryTotalVoltage.multiply(new BigDecimal(10)).intValue();
        body[30] = (byte) ((voltage >> 8) & 0xFF);
        body[31] = (byte) (voltage & 0xFF);
        // 电池生产厂商名称 [4字节] [ASCII]
        byte[] manu = StrUtil.padAfter(batteryManufacturer, 4, ' ').getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(manu, 0, body, 32, 4);
        // 电池组序号 [4字节] [BIN]
        byte[] serial = HexUtil.decodeHex(StrUtil.padPre(batterySerialNo, 8, '0'));
        System.arraycopy(serial, 0, body, 36, 4);
        // 电池组生产日期年 [1字节] [BIN]
        body[40] = (byte) ((batteryProductionYear - 1985) & 0xFF);
        // 电池组生产日期月 [1字节] [BIN]
        body[41] = (byte) (batteryProductionMonth & 0xFF);
        // 电池组生产日期日 [1字节] [BIN]
        body[42] = (byte) (batteryProductionDay & 0xFF);
        // 电池组充电次数 [3字节] [BIN]
        body[43] = (byte) ((batteryChargeCounts >> 16) & 0xFF);
        body[44] = (byte) ((batteryChargeCounts >> 8) & 0xFF);
        body[45] = (byte) (batteryChargeCounts & 0xFF);
        // 电池组产权标识 [1字节] [BIN]
        body[46] = (byte) (batteryPropertyIdentification & 0xFF);
        // 预留位 [1字节] [BIN]
        body[47] = HexUtil.decodeHex(StrUtil.padPre(reserved, 2, '0'))[0];
        // 车辆识别码 [17字节] [ASCII]
        byte[] vinBytes = StrUtil.padAfter(vin, 17, ' ').getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(vinBytes, 0, body, 48, 17);
        // 软件版本号 [8字节] [BIN]
        byte[] verBuf = new byte[8];
        verBuf[0] = (byte) 0xFF;
        verBuf[1] = (byte) 0xFF;
        verBuf[2] = (byte) 0xFF;
        verBuf[3] = (byte) 0xDF;
        verBuf[4] = (byte) 0x07;
        verBuf[5] = (byte) 0x0B;
        verBuf[6] = (byte) 0x0A;
        verBuf[7] = (byte) 0x10;
        System.arraycopy(verBuf, 0, body, 65, 8);

        return body;
    }

    /**
     * 日志记录
     *
     * @param rawHexMsg 原始报文数据
     * @author KevenPotter
     * @date 2026-06-09 09:52:14
     */
    private void log(String rawHexMsg) {
        log.info("-------------------------------------------------------------------------------------------");
        log.info("🚀 【0x15】 {} 充电握手上传  原始报文    rawMsg                       : {}", PURPLE + deviceId + RESET, rawHexMsg);
        log.info("🚀 【0x15】 {} 充电握手上传  设备编号    deviceId                     : {}", PURPLE + deviceId + RESET, deviceId);
        log.info("🚀 【0x15】 {} 充电握手上传  枪口编号    gunNo                        : {}", PURPLE + deviceId + RESET, gunNo);
        log.info("🚀 【0x15】 {} 充电握手上传  交易编号    tradeNo                      : {}", PURPLE + deviceId + RESET, tradeNo);
        log.info("🚀 【0x15】 {} 充电握手上传  通信版本    communicationProtocolVersion : {}", PURPLE + deviceId + RESET, communicationProtocolVersion);
        log.info("🚀 【0x15】 {} 充电握手上传  电池类型    batteryType                  : {}", PURPLE + deviceId + RESET, batteryType);
        log.info("🚀 【0x15】 {} 充电握手上传  额定容量    batteryRated                 : {}", PURPLE + deviceId + RESET, batteryRated);
        log.info("🚀 【0x15】 {} 充电握手上传  额定电压    batteryTotalVoltage          : {}", PURPLE + deviceId + RESET, batteryTotalVoltage);
        log.info("🚀 【0x15】 {} 充电握手上传  厂商名称    batteryManufacturer          : {}", PURPLE + deviceId + RESET, batteryManufacturer);
        log.info("🚀 【0x15】 {} 充电握手上传  电池序号    batterySerialNo              : {}", PURPLE + deviceId + RESET, batterySerialNo);
        log.info("🚀 【0x15】 {} 充电握手上传  生产日期    ProductionDate               : {}-{}-{}", PURPLE + deviceId + RESET, batteryProductionYear, batteryProductionMonth, batteryProductionDay);
        log.info("🚀 【0x15】 {} 充电握手上传  充电次数    batteryChargeCounts          : {}", PURPLE + deviceId + RESET, batteryChargeCounts);
        log.info("🚀 【0x15】 {} 充电握手上传  产权标识    propertyIdentification       : {}", PURPLE + deviceId + RESET, batteryPropertyIdentification);
        log.info("🚀 【0x15】 {} 充电握手上传  预留位值    reserved                     : {}", PURPLE + deviceId + RESET, reserved);
        log.info("🚀 【0x15】 {} 充电握手上传  车识别码    VIN                          : {}", PURPLE + deviceId + RESET, vin);
        log.info("🚀 【0x15】 {} 充电握手上传  软件版本    softwareVersion              : {}", PURPLE + deviceId + RESET, softwareVersion);
        System.out.println();
    }
}
