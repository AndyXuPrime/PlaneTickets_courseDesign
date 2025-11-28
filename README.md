# ✈️ PlaneTickets_courseDesign | 机票预订系统

<div align="center">
  
  ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?style=for-the-badge&logo=springboot)
  ![Vue](https://img.shields.io/badge/Vue.js-2.6-4FC08D?style=for-the-badge&logo=vue.js)
  ![Element UI](https://img.shields.io/badge/Element%20UI-2.15-409EFF?style=for-the-badge&logo=element)
  ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)
  ![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk)

  <p>🎓 基于 Spring Boot + Vue 的前后端分离机票预订系统课程设计</p>

</div>

<p align="center">
  <a href="#-项目概述">📖 项目概述</a> •
  <a href="#-功能特性">✨ 功能特性</a> •
  <a href="#-技术栈">🛠️ 技术栈</a> •
  <a href="#-快速开始">🚀 快速开始</a> •
  <a href="#-数据库设计">💾 数据库设计</a> •
  <a href="#-常见问题与解决方案">❓ 常见问题</a>
</p>

---

## 📖 项目概述

本项目是一个基于 **Spring Boot 3** 和 **Vue 2** 开发的前后端分离机票预订系统。系统模拟了真实的航班查询、机票预订、订单管理及退票流程，并包含一个管理员后台用于维护航班信息和查看销售统计。

项目旨在解决课程设计中常见的技术难点，如 JWT 身份认证、复杂 SQL 查询、前后端跨域交互以及 VS Code 环境下的 Java 开发配置。

### 🔗 仓库地址
- **后端 (Backend):** [backend](https://github.com/AndyXuPrime/PlaneTickets_courseDesign/tree/main/backend)
- **前端 (Frontend):** [frontend](https://github.com/AndyXuPrime/PlaneTickets_courseDesign/tree/main/frontend)

---

## ✨ 功能特性

### 🧑‍✈️ 用户端
- **航班查询**: 支持按出发地、目的地、日期、航空公司进行模糊/精确查询。
- **机票预订**: 实时显示余票，支持经济舱/商务舱选择，防止超卖。
- **订单管理**: 查看历史订单，支持查看订单详情。
- **在线退票**: 用户可对未使用的机票进行退票操作。
- **个人中心**: 管理个人信息及会员等级（普通/银卡/金卡/白金）。

### 👨‍💻 管理员端
- **航班管理**: 航班的增删改查 (CRUD)。
- **数据统计**: 
  - 📊 热门航线分析
  - 📈 销售额统计
  - 👥 用户画像分析（年龄/性别分布）
- **动态定价**: 根据剩余座位数和时间动态调整票价策略。

---

## 🛠️ 技术栈

| 模块 | 技术/工具 | 说明 |
| :--- | :--- | :--- |
| **后端** | Spring Boot 3.3.0 | 核心框架 |
| | Spring Security + JWT | 认证与授权 (无状态) |
| | Spring Data JPA |持久层框架 (Hibernate) |
| | Lombok | 简化 Java 代码 (需注意 IDE 插件) |
| **前端** | Vue 2.6 | 前端框架 |
| | Element UI | UI 组件库 |
| | Axios | HTTP 请求库 |
| **数据库** | MySQL 8.0 | 关系型数据库 |
| **开发环境** | VS Code / IDEA | 开发工具 |
| | Maven 3.x | 后端构建工具 |
| | npm / Node.js | 前端包管理 |

---

## 🚀 快速开始

### 1. 环境准备
- **JDK**: 17+ (Spring Boot 3 必需)
- **Node.js**: 建议 v14 或 v16
- **MySQL**: 8.0+

### 2. 数据库配置
1. 创建数据库 `plane_ticket_db`。
2. 执行下文 [数据库设计](#-数据库设计) 中的 SQL 建表语句。
3. 修改后端配置文件 `src/main/resources/application.properties`：
   ```properties
   spring.datasource.username=你的用户名一般为root
   spring.datasource.password=你的真实密码
   ```

### 3. 后端启动 (Backend)
> **⚠️ 注意：** 如果使用 VS Code 开发，可能会遇到 Lombok 注解不生效的问题。

```bash
cd backend
# 推荐使用命令行启动，避免 IDE 缓存问题
mvn clean spring-boot:run
```
启动成功后，后端服务运行在 `http://localhost:8080`。

### 4. 前端启动 (Frontend)
> **⚠️ 注意：** 本项目解决了 Element UI 依赖的 `async-validator` 与 Babel 7 的冲突问题。

```bash
cd frontend
# 1. 安装依赖
npm install

# 2. 启动开发服务器
npm run serve
```
启动成功后，访问 `http://localhost:8081` (如果 8080 被后端占用)。

---

## 💾 数据库设计

以下是核心业务表的 Schema 设计。

### Table: airlines (航空公司)
```sql
TABLE `airlines` (
  `airline_code` char(2) NOT NULL,
  `airline_name` varchar(50) NOT NULL,
  `country` varchar(30) DEFAULT '中国',
  `contact_phone` varchar(20) DEFAULT NULL,
  `website` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`airline_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

### Table: customers (乘客/用户)
```sql
 TABLE `customers` (
  `customer_id` int NOT NULL AUTO_INCREMENT COMMENT '顾客ID',
  `name` varchar(50) NOT NULL COMMENT '姓名',
  `gender` enum('男','女') DEFAULT '男' COMMENT '性别',
  `id_card` varchar(18) DEFAULT NULL,
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `email` varchar(50) DEFAULT NULL COMMENT '电子邮箱',
  `membership_level` enum('普通','银卡','金卡','白金') DEFAULT '普通' COMMENT '会员等级',
  `password` varchar(30) NOT NULL DEFAULT '1234567890',
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `uniq_customers_phone` (`phone`) COMMENT '手机号唯一索引',
  UNIQUE KEY `uk_id_card` (`id_card`),
  KEY `idx_customers_name` (`name`) COMMENT '按姓名查询',
  CONSTRAINT `chk_valid_email` CHECK (((`email` is null) or regexp_like(`email`,_utf8mb4'^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'))),
  CONSTRAINT `chk_valid_phone` CHECK (regexp_like(`phone`,_utf8mb4'^1[3-9]\\d{9}$'))
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='顾客信息表'
```

### Table: flights (航班)
```sql
TABLE `flights` (
  `flight_number` varchar(6) NOT NULL COMMENT '航班号(如CA1886)',
  `airline_code` char(2) NOT NULL COMMENT '航空公司代码',
  `departure_airport` varchar(50) NOT NULL COMMENT '起飞机场',
  `arrival_airport` varchar(50) NOT NULL COMMENT '到达机场',
  `departure_time` time NOT NULL COMMENT '起飞时间',
  `arrival_time` time NOT NULL COMMENT '到达时间',
  `aircraft_model` varchar(20) NOT NULL COMMENT '飞机型号',
  `business_seats` smallint NOT NULL COMMENT '商务舱座位数',
  `economy_seats` smallint NOT NULL COMMENT '经济舱座位数',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础价格',
  PRIMARY KEY (`flight_number`),
  KEY `idx_flights_airline` (`airline_code`) COMMENT '按航空公司查询',
  KEY `idx_flights_departure` (`departure_airport`) COMMENT '按起飞机场查询',
  KEY `idx_flights_arrival` (`arrival_airport`) COMMENT '按到达机场查询',
  CONSTRAINT `fk_flights_airline` FOREIGN KEY (`airline_code`) REFERENCES `airlines` (`airline_code`) ON DELETE CASCADE,
  CONSTRAINT `chk_seats_positive` CHECK (((`business_seats` >= 0) and (`economy_seats` >= 0))),
  CONSTRAINT `chk_valid_price` CHECK ((`base_price` > 0)),
  CONSTRAINT `chk_valid_times` CHECK ((`arrival_time` > `departure_time`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='航班基本信息表'
```

### Table: tickets (机票/订单)
```sql
 TABLE `tickets` (
  `ticket_id` bigint NOT NULL AUTO_INCREMENT COMMENT '机票ID',
  `flight_number` varchar(6) NOT NULL COMMENT '航班号',
  `flight_date` date NOT NULL COMMENT '航班日期',
  `customer_id` int NOT NULL COMMENT '顾客ID',
  `class` enum('经济舱','商务舱') NOT NULL COMMENT '舱位等级',
  `price` decimal(10,2) NOT NULL COMMENT '实际价格',
  `status` enum('已预订','已支付','已取消','已使用') DEFAULT '已预订' COMMENT '机票状态',
  `booking_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '预订时间',
  `payment_time` timestamp NULL DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`ticket_id`),
  KEY `fk_ticket_customer` (`customer_id`),
  KEY `idx_tickets_flight_date` (`flight_date`) COMMENT '按航班日期查询',
  KEY `idx_tickets_status` (`status`) COMMENT '按机票状态查询',
  KEY `idx_tickets_flight_instance` (`flight_number`,`flight_date`) COMMENT '联合查询航班实例',
  CONSTRAINT `fk_ticket_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ticket_flight` FOREIGN KEY (`flight_number`) REFERENCES `flights` (`flight_number`) ON DELETE CASCADE,
  CONSTRAINT `chk_booking_before_flight` CHECK ((`booking_time` < (`flight_date` + interval 1 day))),
  CONSTRAINT `chk_valid_payment_time` CHECK ((((`status` in (_utf8mb4'已预订',_utf8mb4'已取消')) and (`payment_time` is null)) or ((`status` in (_utf8mb4'已支付',_utf8mb4'已使用')) and (`payment_time` is not null))))
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机票信息表'
```

### Table: ticket_status_log (状态变更日志)
```sql
TABLE `ticket_status_log` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `ticket_id` bigint NOT NULL,
  `old_status` enum('已预订','已支付','已取消','已使用') DEFAULT NULL,
  `new_status` enum('已预订','已支付','已取消','已使用') DEFAULT NULL,
  `change_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `changed_by` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `FKb570eyncqo03d5877ldyw0hi0` (`ticket_id`),
  CONSTRAINT `FKb570eyncqo03d5877ldyw0hi0` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

---

## ❓ 常见问题与解决方案

在本项目开发过程中，总结了以下关键问题的解决方案，供参考：

### 1. 后端报 `NullPointerException` (依赖注入失败)
*   **现象**: 调用 Controller 或 Service 方法时，Repository 或 Service 对象为 null。
*   **原因**: VS Code 的 Java 插件有时无法正确处理 Lombok 的 `@RequiredArgsConstructor` 注解，导致构造函数未生成，依赖未注入。
*   **解决**: 
    1. 放弃 Lombok 自动生成，**手动编写构造函数**进行注入。
    2. 或者使用 `@Autowired` 字段注入（如本项目最终采用方案）。
    3. 运行 `mvn clean` 强制清理缓存。

### 2. 前端 `npm run serve` 报错 `async-validator`
*   **现象**: `Module not found: Error: Can't resolve 'babel-runtime/helpers/extends'`。
*   **原因**: Element UI 依赖旧版 `babel-runtime`，而 Vue CLI 项目默认使用新版 `@babel/runtime`。
*   **解决**: 在 `vue.config.js` 中配置别名映射：
    ```javascript
    configureWebpack: {
      resolve: {
        alias: { 'babel-runtime': '@babel/runtime' }
      }
    }
    ```
    并删除 `node_modules/.cache` 文件夹后重启。

### 3. 后端端口占用 (55880 / 8080)
*   **现象**: `Port already in use` 或 `BindException`。
*   **解决**: 
    - 简单方法：重启 VS Code。
    - 命令行查杀：`netstat -ano | findstr :端口号` 然后 `taskkill /PID <进程ID> /F`。

### 4. 数据库连接报错 `Access denied`
*   **原因**: `application.properties` 中使用了占位符用户名或密码错误。
*   **解决**: 确保配置为本地 MySQL 的真实 root 账号和密码。

---

<div align="center">
  <sub>Designed by AndyXuPrime | 课程设计 |Course Design 2024</sub>
</div>
