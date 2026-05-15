package com.wantllife.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 充电卡
 *
 * @author KevenPotter
 * @date 2026-04-28 09:54:33
 */
@Data
@Accessors(chain = true)
public class StandardCard {

    /*充电卡主键编号*/
    private Long cardId;
    /*逻辑卡号*/
    private String logicalCardNo;
    /*物理卡号*/
    private String physicalCardNo;
}
