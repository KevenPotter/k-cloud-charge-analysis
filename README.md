# k-cloud-charge-analysis

云快充协议（GB/T 27930）解析与构建SDK，高性能、轻量级、零框架依赖，专为充电桩与平台对接场景设计。

## 核心功能
- 完整实现云快充协议报文解析，支持充电桩上报数据处理
- 提供全平台控制指令构建能力，一键生成标准下发报文
- 内置CRC校验、时间格式转换、十六进制处理等工具类
- 接口极简，仅负责字节数组与对象转换，网络通信完全解耦，适配各种业务场景

## 安装使用
1. 在项目pom.xml中添加GitHub Packages仓库：
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/KevenPotter/k-cloud-charge-analysis</url>
    </repository>
</repositories>

2. 引入依赖：
<dependency>
    <groupId>com.wantllife</groupId>
    <artifactId>k-cloud-charge-analysis</artifactId>
    <version>1.0.0</version>
</dependency>

## 快速上手
1. 解析上报报文
// 接收设备原始字节数组
byte[] rawFrame = ...;
// 解析帧头
FrameHeader header = FrameHeader.parse(rawFrame);
// 根据消息类型解析具体业务报文
if ("B0".equals(header.getMsgId())) {
    ChargeStartReq req = B0ChargeStartReq.parse(rawFrame);
    // 处理充电开始请求逻辑
}

2. 构建下发指令
// 生成开电指令
byte[] startCmd = AOStartChargeRes.buildCommand("设备编号", 枪号, "交易流水号", "逻辑卡号", "物理卡号", 余额);
// 通过自有信道管理器下发
deviceChannelManager.sendMsg("设备编号", startCmd);

## 许可证
本项目采用MIT许可证，详见LICENSE文件。

## 作者
KevenPotter
