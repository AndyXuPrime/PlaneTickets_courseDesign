# PlaneTickets_courseDesign
## 前后端课程设计作业（机票预定系统） Curriculum Design for Front end and Back end Development （Plane Tickets Booking）
### **后端(Front):**
https://github.com/AndyXuPrime/PlaneTickets_courseDesign/tree/main/main/java
### **前端(Back):**
https://github.com/AndyXuPrime/PlaneTickets_courseDesign/tree/main/src

**USE：SpringBoot +Vue**

### **数据库设计：**
#### **数据库：MySQL**


Table: airlines  
Columns:  
  airline_code char(2) PK  
  airline_name varchar(50)  
  country varchar(30)  
  contact_phone varchar(20)  
  website varchar(100)  

```
TABLE `airlines` (
  `airline_code` char(2) NOT NULL,
  `airline_name` varchar(50) NOT NULL,
  `country` varchar(30) DEFAULT '中国',
  `contact_phone` varchar(20) DEFAULT NULL,
  `website` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`airline_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

Table: customers  
Columns:  
customer_id int AI PK     
name varchar(50)   
gender enum('男','女')   
id_card varchar(18)   
phone varchar(20)   
email varchar(50)   
membership_level enum('普通','银卡','金卡','白金')   
password varchar(30)  

```
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


Table: flights  
Columns:  
flight_number varchar(6) PK  
airline_code char(2)  
departure_airport varchar(50)  
arrival_airport varchar(50)  
departure_time time  
arrival_time time  
aircraft_model varchar(20)  
business_seats smallint  
economy_seats smallint  
base_price decimal(10,2)  

```
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

Table: ticket_status_log  
Columns:  
log_id int AI PK  
ticket_id bigint  
old_status enum('已预订','已支付','已取消','已使用')  
new_status enum('已预订','已支付','已取消','已使用')  
change_time timestamp  
changed_by varchar(50)  

```
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

Table: tickets  
Columns:  
ticket_id bigint AI PK    
flight_number varchar(6)    
flight_date date    
customer_id int  
class enum('经济舱','商务舱')    
price decimal(10,2)    
status enum('已预订','已支付','已取消','已使用')  
booking_time timestamp  
payment_time timestamp  

```
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

