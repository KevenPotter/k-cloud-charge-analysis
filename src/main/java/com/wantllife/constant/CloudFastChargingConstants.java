package com.wantllife.constant;

/**
 * 云快充协议常量
 *
 * @author KevenPotter
 * @date 2026-04-29 15:09:41
 */
public final class CloudFastChargingConstants {

    private CloudFastChargingConstants() {
    }

    /* 提示图标 */
    public static final String TIP_ICON = "\uD83D\uDD0B";
    /* 项目名称 */
    public static final String PROJECT_NAME = "[k-cloud-charge-analysis]";

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

    //=============================== 上行指令 平台->模拟器 ===============================
    /*模拟器登录认证应答*/
    public static final byte SIM_UP_LOGIN = 0x02;
    /*模拟器心跳包应答*/
    public static final byte SIM_UP_HEARTBEAT = 0x04;
    /*模拟器计费模型验证请求应答*/
    public static final byte SIM_UP_BILLING_MODE_VALID = 0x06;
    /*模拟器计费模型请求应答*/
    public static final byte SIM_UP_BILLING_MODE = 0x0A;
    /*模拟器读取实时监测数据*/
    public static final byte SIM_UP_REAL_TIME_MONITOR = 0x12;
    /*模拟器运营平台确认启动充电*/
    public static final byte SIM_UP_REQUEST_CHARGING = 0x32;
    /*模拟器运营平台远程控制启机*/
    public static final byte SIM_UP_START_CHARGE = 0x34;
    /*模拟器运营平台远程停机*/
    public static final byte SIM_UP_STOP_CHARGE = 0x36;
    /*模拟器交易记录确认*/
    public static final byte SIM_UP_TRADE_RECORD = 0x40;
    /*模拟器远程账户余额更新*/
    public static final byte SIM_UP_BALANCE_UPDATE = 0x42;
    /*模拟器离线卡数据同步*/
    public static final byte SIM_UP_OFFLINE_CARD_SYNC = 0x44;
    /*模拟器离线卡数据清除*/
    public static final byte SIM_UP_OFFLINE_CARD_CLEAR = 0x46;
    /*模拟器离线卡数据查询*/
    public static final byte SIM_UP_OFFLINE_CARD_QUERY = 0x48;
    /*模拟器充电桩工作参数设置*/
    public static final byte SIM_UP_WORKING_PARAMS = 0x52;
    /*模拟器对时设置*/
    public static final byte SIM_UP_TIME_SYNC = 0x56;
    /*模拟器计费模型设置*/
    public static final byte SIM_UP_BILLING_MODE_SET = 0x58;
    /*模拟器遥控地锁升锁与降锁*/
    public static final byte SIM_UP_LOCK_UP_DOWN = 0x62;
    /*模拟器远程重启*/
    public static final byte SIM_UP_REBOOT = (byte) 0x92;
    /*模拟器远程更新*/
    public static final byte SIM_UP_UPGRADE = (byte) 0x94;
    /*模拟器运营平台确认并充启动充电*/
    public static final byte SIM_UP_APPLY_PARALLEL_CHARGING = (byte) 0xA2;
    /*模拟器运营平台远程控制并充启机*/
    public static final byte SIM_UP_PARALLEL_START_CHARGE = (byte) 0xA4;

    //=============================== 下行指令 模拟器->平台 ===============================
    /*模拟器登录请求*/
    public static final String SIM_DOWN_LOGIN = "01";
    /*模拟器心跳请求*/
    public static final String SIM_DOWN_HEARTBEAT = "03";
    /*模拟器计费模型验证请求*/
    public static final String SIM_DOWN_BILLING_MODE_VALID = "05";
    /*模拟器充电桩计费模型请求*/
    public static final String SIM_DOWN_BILLING_MODE = "09";
    /*模拟器上传实时监测数据*/
    public static final String SIM_DOWN_REAL_TIME_MONITOR = "13";
    /*模拟器充电握手*/
    public static final String SIM_DOWN_CHARGING_HANDSHAKE = "15";
    /*模拟器参数配置*/
    public static final String SIM_DOWN_PARAM_CONFIG = "17";
    /*模拟器充电结束*/
    public static final String SIM_DOWN_CHARGE_FINISHED = "19";
    /*模拟器错误报文*/
    public static final String SIM_DOWN_ERROR = "1B";
    /*模拟器充电阶段BMS中止*/
    public static final String SIM_DOWN_CHARGING_BMS_STOP = "1D";
    /*模拟器充电阶段充电机中止*/
    public static final String SIM_DOWN_CHARGING_CHARGER_STOP = "21";
    /*模拟器充电过程BMS需求与充电机输出*/
    public static final String SIM_DOWN_CHARGING_BMS_DEMAND = "23";
    /*模拟器充电过程BMS信息*/
    public static final String SIM_DOWN_CHARGING_BMS_INFO = "25";
    /*模拟器充电桩主动申请启动充电*/
    public static final String SIM_DOWN_REQUEST_CHARGING = "31";
    /*模拟器远程启动充电命令回复*/
    public static final String SIM_DOWN_START_CHARGE = "33";
    /*模拟器远程停机命令回复*/
    public static final String SIM_DOWN_STOP_CHARGE = "35";
    /*模拟器交易记录*/
    public static final String SIM_DOWN_TRADE_RECORD = "3B";
    /*模拟器余额更新应答*/
    public static final String SIM_DOWN_BALANCE_UPDATE = "41";
    /*模拟器离线卡数据同步应答*/
    public static final String SIM_DOWN_OFFLINE_CARD_SYNC = "43";
    /*模拟器离线卡数据清除应答*/
    public static final String SIM_DOWN_OFFLINE_CARD_CLEAR = "45";
    /*模拟器离线卡数据查询应答*/
    public static final String SIM_DOWN_OFFLINE_CARD_QUERY = "47";
    /*模拟器充电桩工作参数设置应答*/
    public static final String SIM_DOWN_WORKING_PARAMS = "51";
    /*模拟器对时设置应答*/
    public static final String SIM_DOWN_TIME_SYNC = "55";
    /*模拟器计费模型应答*/
    public static final String SIM_DOWN_BILLING_MODE_SET = "57";
    /*模拟器地锁数据上送*/
    public static final String SIM_DOWN_GROUND_LOCK_DATA = "61";
    /*模拟器充电桩返回数据*/
    public static final String SIM_DOWN_LOCK_UP_DOWN = "63";
    /*模拟器远程重启应答*/
    public static final String SIM_DOWN_REBOOT = "91";
    /*模拟器远程更新应答*/
    public static final String SIM_DOWN_UPGRADE = "93";
    /*模拟器充电桩主动申请并充充电*/
    public static final String SIM_DOWN_APPLY_PARALLEL_CHARGING = "A1";
    /*模拟器远程并充启机命令回复*/
    public static final String SIM_DOWN_PARALLEL_START_CHARGE = "A3";

    /*云快充协议起始符 0x68*/
    public static final String FRAME_START_FLAG = "68";
    /*云快充协议是否加密符 0x00*/
    public static final String FRAME_ENCRYPT_FLAG = "00";
}
