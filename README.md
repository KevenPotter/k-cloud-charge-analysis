# k-cloud-charge-analysis

云快充协议（GBT-27930）解析与构建SDK，高性能、轻量级、零框架依赖，专为充电桩与平台对接场景设计。

[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![Java](https://img.shields.io/badge/Java-8+-green.svg)](https://www.oracle.com/java/)
[![MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![GitHub Stars](https://img.shields.io/github/stars/KevenPotter/k-cloud-charge-analysis?style=social)](https://github.com/KevenPotter/k-cloud-charge-analysis)

---

## 📌 核心功能

- 🔄 完整实现云快充协议报文解析，支持充电桩上报数据处理
- 🎯 提供全平台控制指令构建能力，一键生成标准下发报文
- 🛠️ 内置CRC校验、时间格式转换、十六进制处理等工具类
- 🔗 接口极简，仅负责字节数组与对象转换，网络通信完全解耦，适配各种业务场景

---

## 🔧 安装使用

> **必须完整配置以下所有步骤**

### 1. 配置 GitHub Maven 仓库

在项目 `pom.xml` 中添加：

```xml
<repositories>
   <repository>
      <id>github</id>
      <url>https://maven.pkg.github.com/KevenPotter/k-cloud-charge-analysis</url>
   </repository>
</repositories>
```

### 2. 引入依赖

```xml
<dependency>
    <groupId>com.wantllife</groupId>
    <artifactId>k-cloud-charge-analysis</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 3. 🔔 必须配置 GitHub Token

GitHub Packages 即使是公开仓库，**也必须配置 token 才能下载依赖**。

找到 Maven 的 `settings.xml` 文件：
- Windows：`C:\Users\你的用户名\.m2\settings.xml`
- Mac/Linux：`~/.m2/settings.xml`

添加以下配置：

```xml
<servers>
    <server>
        <id>github</id>
        <username>你的GitHub用户名</username>
        <password>你的GitHub Token</password>
    </server>
</servers>
```

> 💡 **如何生成 GitHub Token**：GitHub → Settings → Developer settings → Personal access tokens → Generate new token → 勾选 `repo` 和 `packages` 权限

---

## 🚀 快速上手

### 解析上报报文

实现 `MessageProcessor<ByteBuffer>` 接口，通过帧类型 `frameType` 分发处理：

```java
@Override
public void process(AioSession session, ByteBuffer buffer) {
   try {
      // 1. 读取原始字节数组
      byte[] data = new byte[buffer.remaining()];
      buffer.get(data);

      // 2. 原始报文
      String rawHexMsg = HexUtil.encodeHexStr(data).toUpperCase();

      // 3. 基础校验
      if (data.length < 6) {
         log.warn("报文长度过短，忽略");
         return;
      }
      // 4.起始符必须 0x68（云快充协议）
      if ((data[0] & 0xFF) != 0x68) {
         log.warn("非云快充协议，忽略");
         return;
      }

      byte frameType = data[5];

      switch (frameType) {
         // 充电桩登录认证
         case UP_LOGIN:
            AALoginReq loginReq = new AALoginReq(data, rawHexMsg);
            // 注册设备
            if (StrUtil.isNotBlank(loginReq.getDeviceId())) {
               deviceChannelManager.register(loginReq.getDeviceId(), session);
               deviceChannelManager.sendMsg(loginReq.getDeviceId(), AALoginRes.buildCommand(loginReq));
            }
            break;
         // 充电桩心跳包
         case UP_HEARTBEAT:
            ABHeartbeatReq heartbeatReq = new ABHeartbeatReq(data, rawHexMsg);
            deviceChannelManager.sendMsg(heartbeatReq.getDeviceId(), ABHeartbeatRes.buildCommand(heartbeatReq));
            break;
         // 计费模型验证请求
         case UP_BILLING_MODE_VALID:
            ACBillingModelValidReq billingModelValidReq = new ACBillingModelValidReq(data, rawHexMsg);
            Long billingModeId = billingModelValidReq.getBillingModeId();
            deviceChannelManager.sendMsg(billingModelValidReq.getDeviceId(), ACBillingModeValidRes.buildCommand(billingModelValidReq, "1096", billingModeId == 1096));
            break;
         // 充电桩计费模型请求
         case UP_BILLING_MODE:
            ADBillingModelReq billingModelReq = new ADBillingModelReq(data, rawHexMsg);
            List<StandardBillingModel> billingModelList = fakeBillingMode();
            deviceChannelManager.sendMsg(billingModelReq.getDeviceId(), ADBillingModelRes.buildCommand(billingModelReq, billingModelList));
            break;
         // 上传实时监测数据
         case UP_REAL_TIME_MONITOR:
            AERealTimeMonitorReq realTimeMonitorReq = new AERealTimeMonitorReq(data, rawHexMsg);
            break;
         // 更多帧类型...
      }
   } catch (Exception e) {
      log.error("处理设备消息异常", e);
   }
}
```

### 构建下发指令

调用对应的 `buildCommand` 方法即可生成标准报文：

```java
// 实时监测指令
byte[] realTimeData = AERealTimeMonitorRes.buildCommand(deviceId, gunNo);
deviceChannelManager.sendMsg(deviceId, realTimeData);

// 开电指令
byte[] startData = AOStartChargeRes.buildCommand(deviceId, gunNo, tradeNo, logicalCardNo, physicalCardNo, balance);
deviceChannelManager.sendMsg(deviceId, startData);

// 关电指令
byte[] stopData = APStopChargeRes.buildCommand(deviceId, gunNo);
deviceChannelManager.sendMsg(deviceId, stopData);

// 离线卡同步
byte[] cardSyncData = ASOfflineCardSyncRes.buildCommand(deviceId, cardList);
deviceChannelManager.sendMsg(deviceId, cardSyncData);
```

### 模拟数据

这个是模拟的数据，请按照实际方式进行构建：

```java
/**
 * 离线卡模拟假数据
 *
 * @return 返回离线卡
 * @author KevenPotter
 */
private List<StandardCard> fakeCardList() {
    List<StandardCard> cardList = new ArrayList<>();
    StandardCard card_1 = new StandardCard().setCardId(1L).setLogicalCardNo("10000001").setPhysicalCardNo("D14B0A54");
    StandardCard card_2 = new StandardCard().setCardId(1L).setLogicalCardNo("10000002").setPhysicalCardNo("D14B0A55");
    cardList.add(card_1);
    cardList.add(card_2);
    return cardList;
}

/**
 * 计费模型模拟假数据
 * 规则：尖 > 峰 > 平 > 谷
 * 价格：电费、服务费均按阶梯递减，谷段最低
 * 格式：全部保留5位小数
 *
 * @return 返回计费模式
 * @author KevenPotter
 */
private List<StandardBillingModel> fakeBillingMode() {
    List<StandardBillingModel> billingModeList = new ArrayList<>();

    // 尖 18:00-22:00
    StandardBillingModel sharpMode = new StandardBillingModel();
    sharpMode.setModeId(1L);
    sharpMode.setStrategyId(1096L);
    sharpMode.setTimeSlotType(1).setTimeSlotName("尖");
    sharpMode.setStartTime("18:00").setEndTime("22:00");
    sharpMode.setElectricityFee(new BigDecimal("1.72500"))
            .setServiceFee(new BigDecimal("0.58000"))
            .setCostFee(new BigDecimal("0.83000"));

    // 峰 10:00-18:00
    StandardBillingModel peakMode = new StandardBillingModel();
    peakMode.setModeId(2L);
    peakMode.setStrategyId(1096L);
    peakMode.setTimeSlotType(2).setTimeSlotName("峰");
    peakMode.setStartTime("10:00").setEndTime("18:00");
    peakMode.setElectricityFee(new BigDecimal("1.43600"))
            .setServiceFee(new BigDecimal("0.46000"))
            .setCostFee(new BigDecimal("0.76000"));

    // 平 07:00-10:00
    StandardBillingModel flatMode = new StandardBillingModel();
    flatMode.setModeId(3L);
    flatMode.setStrategyId(1096L);
    flatMode.setTimeSlotType(3).setTimeSlotName("平");
    flatMode.setStartTime("07:00").setEndTime("10:00");
    flatMode.setElectricityFee(new BigDecimal("1.16800"))
            .setServiceFee(new BigDecimal("0.37000"))
            .setCostFee(new BigDecimal("0.66000"));

    // 谷 22:00-07:00
    StandardBillingModel valleyMode = new StandardBillingModel();
    valleyMode.setModeId(4L);
    valleyMode.setStrategyId(1096L);
    valleyMode.setTimeSlotType(4).setTimeSlotName("谷");
    valleyMode.setStartTime("22:00").setEndTime("07:00");
    valleyMode.setElectricityFee(new BigDecimal("0.61200"))
            .setServiceFee(new BigDecimal("0.22000"))
            .setCostFee(new BigDecimal("0.46000"));

    billingModeList.add(sharpMode);
    billingModeList.add(peakMode);
    billingModeList.add(flatMode);
    billingModeList.add(valleyMode);

    return billingModeList;
}
```

---

## 📁 项目结构

```
com.wantllife
├── analysis                    报文解析与指令构建核心模块
│   ├── req                     设备上报请求类
│   │   ├── AALoginReq.java
│   │   ├── ABHeartbeatReq.java
│   │   ├── ACBillingModelValidReq.java
│   │   ├── ADBillingModelReq.java
│   │   ├── AERealTimeMonitorReq.java
│   │   ├── AFChargingHandshakeReq.java
│   │   ├── AGParamConfigReq.java
│   │   ├── AHChargeFinishedReq.java
│   │   ├── ......
│   └── res                     平台下发指令类
│       ├── AALoginRes.java
│       ├── ABHeartbeatRes.java
│       ├── ACBillingModeValidRes.java
│       ├── ADBillingModelRes.java
│       ├── AERealTimeMonitorRes.java
│       ├── ......
├── constant                     协议常量与消息定义
│   └── CloudFastChargingConstants.java
├── domain.vo                    标准业务对象
│   ├── StandardBillingModel.java
│   ├── StandardCard.java
│   └── StandardChargeOrder.java
└── util                         工具类
    ├── CRCUtil.java
    ├── StringUtil.java
    └── TimeUtil.java
```

---

## 📦 依赖环境

| 依赖 | 版本 | 说明 |
|------|------|------|
| Lombok | 1.18.46 | 简化代码 |
| Hutool | 5.8.44 | 工具库 |
| SLF4J | 2.0.18 | 日志门面 |

---

## 📄 许可证

本项目采用 [MIT](LICENSE) 许可证，详情请参阅 LICENSE 文件。

---

## 👨‍💻 作者

**KevenPotter**

- GitHub: [https://github.com/KevenPotter](https://github.com/KevenPotter)
- 项目地址: [https://github.com/KevenPotter/k-cloud-charge-analysis](https://github.com/KevenPotter/k-cloud-charge-analysis)

---

⭐ 如果这个项目对你有帮助，欢迎 star！