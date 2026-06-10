# 部署说明

## 1. 环境要求

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Spring Boot 3 最低要求 |
| Maven | 3.8+ | 后端构建工具 |
| MySQL | 8.x | 数据库 |
| Node.js | 18+ | 前端运行环境 |
| pnpm | 8+ | 前端包管理器 |

## 2. 数据库部署

### 2.1 创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE course_manager
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
```

### 2.2 导入表结构和初始数据

```bash
mysql -u root -p course_manager < database/schema.sql
```

### 2.3 验证

```sql
USE course_manager;
SHOW TABLES;
SELECT * FROM admin_user;
```

应看到 5 张表和 1 条管理员记录。

## 3. 后端部署

### 3.1 修改数据库配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/course_manager?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 3.2 构建

```bash
cd backend
mvn clean package -DskipTests
```

### 3.3 运行

**开发模式：**

```bash
mvn spring-boot:run
```

**生产模式：**

```bash
java -jar target/course-manager-0.0.1-SNAPSHOT.jar
```

后端默认端口：8080

### 3.4 验证

```bash
curl http://localhost:8080/api/public/courses
```

应返回 JSON 响应（code=200）。

## 4. 前端部署

### 4.1 安装依赖

```bash
cd frontend
pnpm install
```

### 4.2 开发模式

```bash
pnpm dev
```

访问 `http://localhost:5173`。

### 4.3 生产构建

```bash
pnpm build
```

构建产物输出到 `frontend/dist/` 目录。

### 4.4 部署到 Nginx（生产环境）

将 `dist/` 目录部署到 Nginx，并配置反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 5. 开发环境配置

### 5.1 Vite 开发代理

`frontend/vite.config.js` 中配置代理，开发模式下将 `/api` 请求转发到后端：

```js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

### 5.2 MySQL 字符集

确保 MySQL 配置文件（my.cnf / my.ini）中设置了：

```ini
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_general_ci
```

## 6. 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 后端启动报数据库连接失败 | MySQL 未启动或配置错误 | 检查 MySQL 服务和 application.yml |
| 前端请求 404 | 代理未配置 | 检查 vite.config.js 中的 proxy 配置 |
| 登录后接口仍返回 401 | Token 未正确携带 | 检查 Axios 拦截器是否在 Header 中添加 Authorization |
| 中文乱码 | 字符集配置不正确 | 确认数据库和连接都使用 utf8mb4 |
| BCrypt 密码校验失败 | 初始密码 hash 不匹配 | 重新生成 BCrypt hash 并更新 admin_user 表 |

## 7. 端口规划

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端开发服务器 | 5173 | Vite 默认端口 |
| 后端 API 服务 | 8080 | Spring Boot 默认端口 |
| MySQL | 3306 | MySQL 默认端口 |
