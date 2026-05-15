package com.wantllife.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 充电订单
 *
 * @author KevenPotter
 * @date 2026-04-27 09:54:27
 */
@Data
@Accessors(chain = true)
public class StandardChargeOrder {

    /*充电订单主键编号*/
    private Long chargeId;
    /*设备编号*/
    private String deviceId;
    /*枪号*/
    private Integer gunNo;
    /*交易流水号*/
    private String tradeNo;
    /*逻辑卡号*/
    private String logicalCardNo;
    /*账户余额*/
    private BigDecimal balance;
    /*并充序号*/
    private String parallelNo;
}
