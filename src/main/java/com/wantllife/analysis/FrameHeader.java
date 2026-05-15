package com.wantllife.analysis;

import cn.hutool.core.util.HexUtil;
import com.wantllife.util.CRCUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.wantllife.constant.CloudFastChargingConstants.FRAME_ENCRYPT_FLAG;
import static com.wantllife.constant.CloudFastChargingConstants.FRAME_START_FLAG;


/**
 * 云快充指令帧头
 *
 * @author KevenPotter
 * @date 2026-04-20 14:24:28
 */
@Data
@Accessors(chain = true)
public class FrameHeader {

    /*原始数据*/
    private String rawMsg;
    /*起始标志*/
    private String startFlag;
    /*数据长度*/
    private String dataLength;
    /*序列号域*/
    private String seqNo;
    /*加密标志*/
    private String encryptFlag;
    /*帧类型*/
    private String frameType;
    /*帧校验域*/
    private String CRC;
    /*计算帧校验域*/
    private String ReCRC;

    /**
     * 解析帧头
     *
     * @author KevenPotter
     * @date 2026-04-21 10:27:21
     */
    public void parseFrameHeader(byte[] data, String rawHexMsg) {
        int index = 0;
        // 原始报文
        this.setRawMsg(rawHexMsg);
        // 起始符1字节
        this.setStartFlag(String.format("%02X", data[index++] & 0xFF));
        // 数据长度1字节
        this.setDataLength(String.format("%02X", data[index++] & 0xFF));
        // 序列号2字节
        byte seqHigh = data[index++];
        byte seqLow = data[index++];
        int seq = ((seqHigh & 0xFF) << 8) | (seqLow & 0xFF);
        this.setSeqNo(String.format("%04X", seq));
        // 加密标志
        this.setEncryptFlag(String.format("%02X", data[index++] & 0xFF));
        // 帧类型
        this.setFrameType(String.format("%02X", data[index++] & 0xFF));

        // 开始进行CRC校验
        // 截取——[序列号域开始,数据域结束](去掉最后2字节CRC)
        byte[] crcData = new byte[data.length - 4];
        System.arraycopy(data, 2, crcData, 0, data.length - 4);

        // 计算CRC
        this.setReCRC(CRCUtil.getYKCCRC(crcData));

        // 设备上报的CRC(经校验，设备上传为大端序)
        int crcPos = data.length - 2;
        byte crcHigh = data[crcPos];
        byte crcLow = data[crcPos + 1];
        this.setCRC(String.format("%02X%02X", crcHigh & 0xFF, crcLow & 0xFF));
    }

    /**
     * 构建指令
     *
     * @param bodyData 消息体
     * @return 完整报文
     * @author KevenPotter
     * @date 2026-04-21 17:09:32
     */
    public byte[] buildDownMessage(byte[] bodyData) {
        // 1.自动填充默认值
        if (startFlag == null) this.setStartFlag(FRAME_START_FLAG);
        if (encryptFlag == null) this.setEncryptFlag(FRAME_ENCRYPT_FLAG);
        // 2.自动计算长度
        int len = 6 + bodyData.length;
        this.setDataLength(String.format("%02X", len));
        // 3.转换为字节
        byte[] seqNoBytes = HexUtil.decodeHex(seqNo);
        byte[] encryptFlagBytes = HexUtil.decodeHex(encryptFlag);
        byte[] frameTypeBytes = HexUtil.decodeHex(frameType);
        // 4.拼接CRC数据源
        byte[] crcSource = concat(seqNoBytes, encryptFlagBytes, frameTypeBytes, bodyData);
        // 5.计算CRC
        String crcHex = CRCUtil.getYKCCRC(crcSource, true);
        setCRC(crcHex);
        // 6.最终报文
        byte[] startFlagBytes = HexUtil.decodeHex(startFlag);
        byte[] dataLengthBytes = HexUtil.decodeHex(dataLength);
        byte[] crcBytes = HexUtil.decodeHex(crcHex);
        return concat(startFlagBytes, dataLengthBytes, crcSource, crcBytes);
    }

    /**
     * 多字节数组合并
     *
     * @param arrays 待合并的字节数组
     * @return 合并后的字节数组
     * @author KevenPotter
     * @date 2026-04-21 17:23:22
     */
    private byte[] concat(byte[]... arrays) {
        // 计算总长度
        int totalLen = 0;
        for (byte[] arr : arrays) {
            if (arr != null) totalLen += arr.length;
        }

        // 拼接
        byte[] result = new byte[totalLen];
        int index = 0;
        for (byte[] arr : arrays) {
            if (arr != null) {
                System.arraycopy(arr, 0, result, index, arr.length);
                index += arr.length;
            }
        }
        return result;
    }

    /**
     * 判断CRC校验是否一致
     *
     * @return 返回校验结果
     * @author KevenPotter
     * @date 2026-04-21 10:27:30
     */
    public boolean isCrcOk() {
        return this.CRC.equalsIgnoreCase(this.ReCRC);
    }
}
