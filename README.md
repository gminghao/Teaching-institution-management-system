# 教学机构课程管理系统

> Course Management System for Teaching Institutions

## 项目简介

本项目是一个面向小型教学机构的轻量级课程管理系统，核心卖点是将"课程宣传—报名—收费—管理统计"流程集中到一个可维护的 Web 系统中。

系统支持两类角色：

- **访客端**：浏览机构介绍、课程信息，提交课程报名购买意向
- **管理员端**：管理课程、处理报名订单、登记缴费、查看运营数据仪表盘

## 技术栈

| 层级 | 技术选型 |
|------|----------|
| 前端 | Vue 3 + Element Plus + Axios + Vite |
| 后端 | Spring Boot 3 + MyBatis-Plus |
| 数据库 | MySQL 8.x |
| 接口风格 | RESTful API |

## 项目结构

```
Project/
├── backend/                # Spring Boot 后端工程
│   ├── pom.xml
│   └── src/main/java/com/institution/coursemanager/
│       ├── controller/     # Controller 层（HTTP 请求处理）
│       ├── service/        # Service 层（业务逻辑）
│       │   └── impl/       # Service 实现类
│       ├── mapper/         # Mapper 层（数据访问）
│       ├── entity/         # 数据库实体类
│       ├── dto/            # 请求入参 DTO
│       ├── vo/             # 响应出参 VO
│       ├── enums/          # 枚举类
│       ├── config/         # 配置类
│       ├── interceptor/    # 拦截器
│       ├── exception/      # 异常处理
│       └── util/           # 工具类
├── frontend/               # Vue 3 前端工程
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── views/public/   # 访客端页面
│       ├── views/admin/    # 管理员端页面
│       ├── api/            # API 封装
│       ├── router/         # 路由配置
│       ├── components/     # 公共组件
│       └── utils/          # 工具函数
├── database/               # 数据库脚本
│   └── schema.sql
├── docs/                   # 设计文档
│   ├── architecture.md     # 架构设计
│   ├── database-design.md  # 数据库设计
│   ├── api-design.md       # 接口设计
│   ├── test-cases.md       # 测试用例
│   └── deployment.md       # 部署说明
├── CLAUDE.md               # 开发规范与架构决策
├── CHANGELOG.md            # 变更日志
└── README.md               # 本文件
```

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Node.js 18+
- pnpm 8+

### 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE course_manager DEFAULT CHARACTER SET utf8mb4;"

# 导入建表脚本
mysql -u root -p course_manager < database/schema.sql
```

### 启动后端

```bash
cd backend
# 修改 src/main/resources/application.yml 中的数据库连接信息
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

前端默认运行在 `http://localhost:5173`，开发模式下 `/api` 请求会代理到后端 8080 端口。

## 默认管理员账号

| 用户名 | 密码 |
|--------|------|
| admin  | admin123 |

## 核心功能

**访客端**
- 机构介绍展示
- 课程列表浏览（分类筛选、关键词搜索）
- 课程详情查看
- 在线报名（无需注册）

**管理员端**
- 管理员登录（JWT 鉴权）
- 仪表盘（课程数、报名数、缴费统计、最近报名）
- 课程管理（新增、编辑、上架、下架、逻辑删除）
- 报名管理（查看列表、筛选、修改报名状态）
- 财务管理（登记缴费、查看缴费记录、财务汇总）

## 架构说明

系统采用三层架构：

```
Controller 层 → Service 层 → Mapper 层
     ↓               ↓             ↓
  请求处理        业务逻辑      数据访问
```

- Controller 只负责请求解析和响应封装，不包含业务逻辑
- Service 层包含所有业务规则（状态流转、金额计算等）
- Mapper 层负责数据库访问，使用 MyBatis-Plus
- DTO 定义请求参数，VO 定义响应格式
- 枚举类统一管理状态值

详细架构设计见 [docs/architecture.md](docs/architecture.md)。

## 文档

- [架构设计](docs/architecture.md)
- [数据库设计](docs/database-design.md)
- [接口设计](docs/api-design.md)
- [测试用例](docs/test-cases.md)
- [部署说明](docs/deployment.md)
- [变更日志](CHANGELOG.md)
