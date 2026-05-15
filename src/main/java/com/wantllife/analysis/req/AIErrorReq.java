package com.wantllife.analysis.req;

import com.wantllife.analysis.FrameHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * 错误报文(目前和协议对不上)
 *
 * @author KevenPotter
 * @date 2026-04-24 17:02:05
 */
@Slf4j
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class AIErrorReq extends FrameHeader {


    /* 有参构造 */
    public AIErrorReq(byte[] data, String rawHexMsg) {
        // 1.自助解析帧头
        parseFrameHeader(data, rawHexMsg);
        // 2.自助解析消息体
        parseBody(data);
    }

    /**
     * 消息体解析
     *
     * @param data 消息体
     * @author KevenPotter
     * @date 2026-04-24 17:02:49
     */
    private void parseBody(byte[] data) {
    }


}
