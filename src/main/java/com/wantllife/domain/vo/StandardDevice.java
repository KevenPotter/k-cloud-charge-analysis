package com.wantllife.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 设备
 *
 * @author KevenPotter
 * @date 2026-04-28 09:54:33
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class StandardDevice {

    /** 设备编号 */
    private String deviceId;
    /** 设备类型 */
    private Integer deviceType;
    /** 充电枪数量 */
    private Integer gunNum;
    /** 通信协议版本 */
    private String protocolVersion;
    /** 程序版本 */
    private String programVersion;
    /** 网络连接类型 */
    private Integer networkLinkType;
    /** sim卡 */
    private String simNo;
    /** 运营商 */
    private Integer carrier;
}
