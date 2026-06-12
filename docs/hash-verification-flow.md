# BCrypt 哈希验证流程说明文档

> 本文档说明教学机构课程管理系统中管理员密码的 BCrypt 哈希加密与验证机制。

---

## 1. 概述

系统使用 **BCrypt** 算法对管理员密码进行单向加密存储，确保密码安全。

- **算法:** BCrypt（基于 Blowfish 密码算法）
- **实现:** Spring Security 的 `BCryptPasswordEncoder`
- **特点:** 每次加密生成随机盐值，同一密码产生不同哈希

---

## 2. 哈希结构

BCrypt 哈希格式：

```
$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW
│││  │  └──────────────┬──────────────┘ └──────────────┬──────────────┘
│││  │          │                        │
│││  │     盐值 (22字符)            哈希结果 (31字符)
│││  │
│││ 成本因子 (10 = 2^10 = 1024次迭代)
││
│版本标识 ($2a = BCrypt版本2)
│
前缀 ($)
```

| 部分 | 说明 | 示例 |
|------|------|------|
| `$2a$` | BCrypt 版本标识 | 固定格式 |
| `10$` | 成本因子（cost factor） | 10 表示 2^10 = 1024 次迭代 |
| 前 22 字符 | 随机盐值（Base64 编码） | `CBr8M3z3oPFt2kD2D1BD` |
| 后 31 字符 | 实际哈希结果 | `.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW` |

---

## 3. 哈希生成流程

### 3.1 生成时机

- 管理员账号初始化时（`schema.sql`）
- 管理员修改密码时（如实现）

### 3.2 生成代码

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String rawPassword = "admin123";
String hashedPassword = encoder.encode(rawPassword);
// 结果: "$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW"
```

### 3.3 生成过程

```
输入: "admin123"
        ↓
┌─────────────────────────────────────┐
│  1. 生成 128 位随机盐值 (16字节)       │
│     Base64 编码为 22 字符             │
└─────────────────────────────────────┘
        ↓
┌─────────────────────────────────────┐
│  2. 使用盐值 + 成本因子进行加密        │
│     - 成本因子 10 = 1024 次迭代       │
│     - Blowfish 密钥调度              │
└─────────────────────────────────────┘
        ↓
┌─────────────────────────────────────┐
│  3. 拼接输出:                        │
│     $2a$ + 成本因子$ + 盐值 + 哈希结果 │
└─────────────────────────────────────┘
        ↓
输出: "$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW"
```

### 3.4 重要特性

**同一密码每次生成的哈希不同：**

```java
encoder.encode("admin123")  // → "$2a$10$CBr8M3z..."
encoder.encode("admin123")  // → "$2a$10$Xk9P2m..." (不同！)
encoder.encode("admin123")  // → "$2a$10$Qw7R5t..." (不同！)
```

这是因为每次生成都使用**新的随机盐值**，但验证时都能正确匹配。

---

## 4. 登录验证流程

### 4.1 完整流程图

```
┌─────────────────────────────────────────────────────────────────────┐
│                          前端 (Vue 3)                               │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  LoginPage.vue                                                │  │
│  │  form = { username: 'admin', password: 'admin123' }          │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  api/admin.js → login(form)                                   │  │
│  │  POST /api/admin/auth/login                                   │  │
│  │  Body: { "username": "admin", "password": "admin123" }       │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
                              ↓ HTTP POST
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                         后端 (Spring Boot)                          │
│                                                                     │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  AdminAuthController.login(@RequestBody AdminLoginDTO dto)    │  │
│  │  路径: /api/admin/auth/login                                  │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  AuthServiceImpl.login(AdminLoginDTO dto)                     │  │
│  │                                                               │  │
│  │  步骤 1: 参数校验                                              │  │
│  │  - dto != null                                                │  │
│  │  - username 非空                                              │  │
│  │  - password 非空                                              │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  步骤 2: 查询数据库                                            │  │
│  │                                                               │  │
│  │  SELECT * FROM admin_user                                     │  │
│  │  WHERE username = 'admin'                                     │  │
│  │    AND status = 1                                             │  │
│  │  LIMIT 1                                                      │  │
│  │                                                               │  │
│  │  返回: AdminUser {                                            │  │
│  │    id: 1,                                                     │  │
│  │    username: "admin",                                         │  │
│  │    password: "$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ...",       │  │
│  │    realName: "系统管理员",                                      │  │
│  │    status: 1                                                  │  │
│  │  }                                                            │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  步骤 3: BCrypt 密码验证                                       │  │
│  │                                                               │  │
│  │  BCryptPasswordEncoder.matches(                               │  │
│  │    rawPassword: "admin123",          ← 用户输入的明文密码       │  │
│  │    hashedPassword: "$2a$10$CBr..."   ← 数据库存储的哈希        │  │
│  │  )                                                            │  │
│  │                                                               │  │
│  │  内部验证过程:                                                  │  │
│  │  ┌─────────────────────────────────────────────────────────┐  │  │
│  │  │ a) 从 hashedPassword 提取盐值                            │  │  │
│  │  │    盐值 = "CBr8M3z3oPFt2kD2D1BD" (前22字符)             │  │  │
│  │  │                                                         │  │  │
│  │  │ b) 使用相同盐值对 rawPassword 进行加密                    │  │  │
│  │  │    newHash = BCrypt("admin123", 盐值, 成本因子10)        │  │  │
│  │  │                                                         │  │  │
│  │  │ c) 比较两个哈希值                                         │  │  │
│  │  │    newHash == hashedPassword 的哈希部分 ?                │  │  │
│  │  └─────────────────────────────────────────────────────────┘  │  │
│  │                                                               │  │
│  │  结果: true (匹配成功) 或 false (匹配失败)                      │  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                      │
│                    ┌────────┴────────┐                              │
│                    ↓                 ↓                              │
│              匹配成功 ✅          匹配失败 ❌                        │
│                    ↓                 ↓                              │
│  ┌─────────────────────────────┐  ┌─────────────────────────────┐  │
│  │  步骤 4: 生成 JWT Token     │  │  抛出 UnauthorizedException │  │
│  │                             │  │  "用户名或密码错误"          │  │
│  │  token = JwtUtil            │  │                             │  │
│  │    .generateToken("admin")  │  │  返回: { code: 401,         │  │
│  │                             │  │         message: "..." }    │  │
│  │  返回: AdminLoginVO {       │  └─────────────────────────────┘  │
│  │    token: "eyJhbG...",      │                                   │
│  │    username: "admin",       │                                   │
│  │    realName: "系统管理员"    │                                   │
│  │  }                          │                                   │
│  └─────────────────────────────┘                                   │
└─────────────────────────────────────────────────────────────────────┘
                              ↓
                              ↓ HTTP Response
                              ↓
┌─────────────────────────────────────────────────────────────────────┐
│                          前端 (Vue 3)                               │
│                                                                     │
│  响应: {                                                            │
│    code: 200,                                                       │
│    message: "success",                                              │
│    data: {                                                          │
│      token: "eyJhbGciOiJIUzI1NiJ9...",                             │
│      username: "admin",                                             │
│      realName: "系统管理员"                                          │
│    }                                                                │
│  }                                                                  │
│                              ↓                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  保存到 localStorage:                                         │  │
│  │  - course_manager_token = "eyJhbG..."                         │  │
│  │  - course_manager_user = { username: "admin", realName: "..." }│  │
│  └───────────────────────────────────────────────────────────────┘  │
│                              ↓                                      │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │  跳转到 /admin/dashboard                                      │  │
│  └───────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 5. 关键代码文件

| 文件 | 职责 |
|------|------|
| `database/schema.sql` | 数据库建表 + 初始管理员数据（含 BCrypt 哈希） |
| `backend/.../entity/AdminUser.java` | 管理员实体类，映射 `admin_user` 表 |
| `backend/.../mapper/AdminUserMapper.java` | 数据访问层，查询管理员数据 |
| `backend/.../service/impl/AuthServiceImpl.java` | 认证服务，包含登录验证逻辑 |
| `backend/.../controller/admin/AdminAuthController.java` | 认证控制器，暴露登录 API |
| `backend/.../dto/AdminLoginDTO.java` | 登录请求 DTO |
| `backend/.../vo/AdminLoginVO.java` | 登录响应 VO |
| `backend/.../util/JwtUtil.java` | JWT 工具类 |
| `frontend/src/views/admin/LoginPage.vue` | 登录页面组件 |
| `frontend/src/api/admin.js` | 登录 API 定义 |
| `frontend/src/utils/auth.js` | Token/User 存储工具 |

---

## 6. 数据库初始数据

### 6.1 管理员表结构

```sql
CREATE TABLE `admin_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录用户名',
  `password`    VARCHAR(255) NOT NULL COMMENT 'BCrypt 加密密码',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
);
```

### 6.2 初始管理员数据

```sql
-- 默认账号: admin / admin123
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `status`)
VALUES ('admin', '$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW', '系统管理员', 1);
```

---

## 7. 问题排查记录

### 7.1 问题描述

默认管理员账号 `admin / admin123` 无法登录系统。

### 7.2 排查过程

| 步骤 | 检查内容 | 结果 |
|------|---------|------|
| 1 | 前端登录页面默认值 | ✅ `username: 'admin', password: 'admin123'` |
| 2 | 前端 API 调用路径 | ✅ `POST /api/admin/auth/login` |
| 3 | 后端 Controller 映射 | ✅ `@RequestMapping("/api/admin/auth")` |
| 4 | 后端 Service 逻辑 | ✅ 使用 `BCryptPasswordEncoder.matches()` |
| 5 | 测试数据哈希验证 | ✅ 测试哈希匹配 `admin123` |
| 6 | 生产数据哈希验证 | ❌ **生产哈希不匹配 `admin123`** |

### 7.3 根本原因

`database/schema.sql` 中的 BCrypt 哈希值错误：

```sql
-- 错误的哈希（不匹配 admin123）
'$2a$10$N.ZOn9G6w3Fz4nFHRXn5GOe9Th2jKZqK7TAKpXv4pG1wFkBmvUYCi'

-- 正确的哈希（匹配 admin123）
'$2a$10$CBr8M3z3oPFt2kD2D1BDG.WY5eJ.0PJcdJ6fLsAPrLv4K/0./QWmW'
```

### 7.4 修复方案

将 `schema.sql` 中的哈希替换为正确的值。

### 7.5 验证结果

```
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

所有登录相关测试通过。

---

## 8. 安全建议

### 8.1 密码策略

- 最小长度：8 字符
- 复杂度要求：包含大小写字母、数字、特殊字符（建议）
- 定期更换：建议 90 天更换一次

### 8.2 BCrypt 参数

- **成本因子：** 建议 10-12（当前使用 10）
- **盐值长度：** 128 位（16 字节），由 BCrypt 自动生成
- **迭代次数：** 2^cost 次（cost=10 时为 1024 次）

### 8.3 存储安全

- ✅ 密码使用 BCrypt 单向加密存储
- ✅ 不存储明文密码
- ✅ 每个密码使用独立随机盐值
- ❌ 不要将哈希值暴露给前端
- ❌ 不要在日志中记录密码或哈希

---

## 9. 常见问题

### Q1: 为什么同一密码每次生成的哈希不同？

BCrypt 每次生成时使用随机盐值，所以输出不同。但验证时会从存储的哈希中提取盐值，用相同盐值重新计算，因此能正确匹配。

### Q2: 如何生成新的 BCrypt 哈希？

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("your_password");
System.out.println(hash);
```

### Q3: 如何验证哈希是否正确？

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
boolean matches = encoder.matches("admin123", "$2a$10$CBr8...");
System.out.println(matches); // true 或 false
```

### Q4: 成本因子应该设置多少？

| 成本因子 | 迭代次数 | 建议场景 |
|---------|---------|---------|
| 10 | 1,024 | 默认值，适合大多数场景 |
| 12 | 4,096 | 更高安全要求 |
| 14 | 16,384 | 高安全要求（会明显变慢） |

---

## 10. 测试用例

| 编号 | 测试场景 | 输入 | 预期结果 |
|------|---------|------|---------|
| TC-LOGIN-01 | 正常登录 | admin / admin123 | 返回 token |
| TC-LOGIN-02 | 用户名为空 | "" / admin123 | 返回 400 |
| TC-LOGIN-03 | 密码为空 | admin / "" | 返回 400 |
| TC-LOGIN-04 | 都为空 | "" / "" | 返回 400 |
| TC-LOGIN-05 | 错误密码 | admin / wrong | 返回 401 |
| TC-LOGIN-06 | 不存在用户 | nonexistent / admin123 | 返回 401 |
| TC-LOGIN-07 | 空请求体 | {} | 返回 400 |
| TC-LOGIN-08 | 无 Content-Type | 无 | 返回 415 |

---

## 附录：哈希生成工具

如需手动生成 BCrypt 哈希，可使用以下方式：

### Java 代码

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Verify: " + encoder.matches(password, hash));
    }
}
```

### 在线工具

- https://bcrypt-generator.com/
- https://www.browserling.com/tools/bcrypt

**注意：** 在线工具仅用于开发测试，生产环境请使用代码生成。

---

*文档版本: 1.0*
*最后更新: 2026-06-12*
*维护者: 系统管理员*
