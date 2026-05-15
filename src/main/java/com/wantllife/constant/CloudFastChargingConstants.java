package com.wantllife.constant;

/**
 * 云快充协议常量
 *
 * @author KevenPotter
 * @date 2026-04-29 15:09:41
 */
public class CloudFastChargingConstants {

    private CloudFastChargingConstants() {
    }

    //=============================== 上行指令 设备->平台 ===============================
    /*登录请求*/
    public static final byte UP_LOGIN = 0x01;
    /*心跳请求*/
    public static final byte UP_HEARTBEAT = 0x03;
    /*计费模型验证请求*/
    public static final byte UP_BILLING_MODE_VALID = 0x05;
    /*充电桩计费模型请求*/
    public static final byte UP_BILLING_MODE = 0x09;
    /*上传实时监测数据*/
    public static final byte UP_REAL_TIME_MONITOR = 0x13;
    /*充电握手*/
    public static final byte UP_CHARGING_HANDSHAKE = 0x15;
    /*参数配置*/
    public static final byte UP_PARAM_CONFIG = 0x17;
    /*充电结束*/
    public static final byte UP_CHARGE_FINISHED = 0x19;
    /*错误报文*/
    public static final byte UP_ERROR = 0x1B;
    /*充电阶段BMS中止*/
    public static final byte UP_CHARGING_BMS_STOP = 0x1D;
    /*充电阶段充电机中止*/
    public static final byte UP_CHARGING_CHARGER_STOP = 0x21;
    /*充电过程BMS需求与充电机输出*/
    public static final byte UP_CHARGING_BMS_DEMAND = 0x23;
    /*充电过程BMS信息*/
    public static final byte UP_CHARGING_BMS_INFO = 0x25;
    /*充电桩主动申请启动充电*/
    public static final byte UP_REQUEST_CHARGING = 0x31;
    /*远程启动充电命令回复*/
    public static final byte UP_START_CHARGE = 0x33;
    /*远程停机命令回复*/
    public static final byte UP_STOP_CHARGE = 0x35;
    /*交易记录*/
    public static final byte UP_TRADE_RECORD = 0x3B;
    /*余额更新应答*/
    public static final byte UP_BALANCE_UPDATE = 0x41;
    /*离线卡数据同步应答*/
    public static final byte UP_OFFLINE_CARD_SYNC = 0x43;
    /*离线卡数据清除应答*/
    public static final byte UP_OFFLINE_CARD_CLEAR = 0x45;
    /*离线卡数据查询应答*/
    public static final byte UP_OFFLINE_CARD_QUERY = 0x47;
    /*充电桩工作参数设置应答*/
    public static final byte UP_WORKING_PARAMS = 0x51;
    /*对时设置应答*/
    public static final byte UP_TIME_SYNC = 0x55;
    /*计费模型应答*/
    public static final byte UP_BILLING_MODE_SET = 0x57;
    /*地锁数据上送*/
    public static final byte UP_GROUND_LOCK_DATA = 0x61;
    /*充电桩返回数据*/
    public static final byte UP_LOCK_UP_DOWN = 0x63;
    /*远程重启应答*/
    public static final byte UP_REBOOT = (byte) 0x91;
    /*远程更新应答*/
    public static final byte UP_UPGRADE = (byte) 0x93;
    /*充电桩主动申请并充充电*/
    public static final byte UP_APPLY_PARALLEL_CHARGING = (byte) 0xA1;
    /*远程并充启机命令回复*/
    public static final byte UP_PARALLEL_START_CHARGE = (byte) 0xA3;

    //=============================== 下行指令 平台->设备 ===============================
    /*登录认证应答*/
    public static final String DOWN_LOGIN = "02";
    /*心跳包应答*/
    public static final String DOWN_HEARTBEAT = "04";
    /*计费模型验证请求应答*/
    public static final String DOWN_BILLING_MODE_VALID = "06";
    /*计费模型请求应答*/
    public static final String DOWN_BILLING_MODE = "0A";
    /*读取实时监测数据*/
    public static final String DOWN_REAL_TIME_MONITOR = "12";
    /*运营平台确认启动充电*/
    public static final String DOWN_REQUEST_CHARGING = "32";
    /*运营平台远程控制启机*/
    public static final String DOWN_START_CHARGE = "34";
    /*运营平台远程停机*/
    public static final String DOWN_STOP_CHARGE = "36";
    /*交易记录确认*/
    public static final String DOWN_TRADE_RECORD = "40";
    /*远程账户余额更新*/
    public static final String DOWN_BALANCE_UPDATE = "42";
    /*离线卡数据同步*/
    public static final String DOWN_OFFLINE_CARD_SYNC = "44";
    /*离线卡数据清除*/
    public static final String DOWN_OFFLINE_CARD_CLEAR = "46";
    /*离线卡数据查询*/
    public static final String DOWN_OFFLINE_CARD_QUERY = "48";
    /*充电桩工作参数设置*/
    public static final String DOWN_WORKING_PARAMS = "52";
    /*对时设置*/
    public static final String DOWN_TIME_SYNC = "56";
    /*计费模型设置*/
    public static final String DOWN_BILLING_MODE_SET = "58";
    /*遥控地锁升锁与降锁*/
    public static final String DOWN_LOCK_UP_DOWN = "62";
    /*远程重启*/
    public static final String DOWN_REBOOT = "92";
    /*远程更新*/
    public static final String DOWN_UPGRADE = "94";
    /*运营平台确认并充启动充电*/
    public static final String DOWN_APPLY_PARALLEL_CHARGING = "A2";
    /*运营平台远程控制并充启机*/
    public static final String DOWN_PARALLEL_START_CHARGE = "A4";

    /*云快充协议起始符 0x68*/
    public static final String FRAME_START_FLAG = "68";
    /*云快充协议是否加密符 0x00*/
    public static final String FRAME_ENCRYPT_FLAG = "00";
}
