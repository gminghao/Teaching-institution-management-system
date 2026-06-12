# JWT 认证机制说明文档

## 1. JWT 概述

### 1.1 什么是 JWT？

**JWT（JSON Web Token）** 是一种基于 JSON 的开放标准（RFC 7519），用于在各方之间安全地传输信息。在 Web 应用中，JWT 最常见的用途是**身份认证**和**信息交换**。

### 1.2 为什么选择 JWT？

| 特性 | 说明 | 优势 |
|------|------|------|
| **无状态** | 服务器不需要保存会话信息 | 易于水平扩展，减轻服务器负担 |
| **自包含** | Token 中包含用户信息 | 减少数据库查询次数 |
| **跨域支持** | 可在不同域名间传递 | 支持前后端分离架构 |
| **标准化** | RFC 7519 国际标准 | 广泛支持，互操作性强 |

### 1.3 JWT 与传统 Session 对比

```
传统 Session 认证：
┌──────────┐    登录     ┌──────────┐    创建 Session    ┌──────────┐
│  客户端   │ ────────► │  服务器   │ ────────────────► │  Session  │
│          │           │          │                    │  存储     │
│          │ ◄──────── │          │ ◄──────────────── │          │
└──────────┘  SessionID └──────────┘    验证 SessionID   └──────────┘

JWT 认证：
┌──────────┐    登录     ┌──────────┐    生成 Token     ┌──────────┐
│  客户端   │ ────────► │  服务器   │ ────────────────► │  客户端   │
│          │           │          │                    │  存储     │
│          │ ◄──────── │          │ ◄──────────────── │  Token    │
└──────────┘   Token    └──────────┘    验证 Token      └──────────┘
```

**关键区别**：
- Session：服务器存储会话状态，客户端只保存 SessionID
- JWT：客户端存储完整 Token，服务器无需存储状态

---

## 2. JWT 的组成结构

JWT 由三部分组成，用点号（`.`）分隔：

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxNzU4NDAwMCwiZXhwIjoxNzE3NTkxMjAwfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
│                     │                                                │
├── Header (头部) ─────┼── Payload (载荷) ──────────────────────────────┼── Signature (签名)
```

### 2.1 Header（头部）

```json
{
  "alg": "HS256",  // 签名算法：HMAC SHA-256
  "typ": "JWT"     // 令牌类型：JWT
}
```

**作用**：声明令牌的类型和使用的签名算法。

### 2.2 Payload（载荷）

```json
{
  "sub": "admin",           // 主题：用户名
  "iat": 1717584000,        // 签发时间：Unix 时间戳
  "exp": 1717591200         // 过期时间：Unix 时间戳（2小时后）
}
```

**作用**：存放实际需要传输的数据（声明/Claims）。

**常见声明类型**：

| 声明 | 全称 | 说明 | 本项目使用 |
|------|------|------|------------|
| `sub` | Subject | 主题（通常是用户标识） | ✅ 用户名 |
| `iat` | Issued At | 签发时间 | ✅ |
| `exp` | Expiration | 过期时间 | ✅ 2小时 |
| `iss` | Issuer | 签发者 | ❌ |
| `aud` | Audience | 接收者 | ❌ |

### 2.3 Signature（签名）

```java
// 签名计算公式
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret  // ← 这就是 JWT_SECRET
)
```

**作用**：确保 Token 没有被篡改，验证发送者的身份。

**关键点**：
- 签名使用 **JWT_SECRET** 作为密钥
- 只有拥有相同密钥的服务器才能验证签名
- 如果 Token 被篡改，签名验证会失败

---

## 3. 项目中的 JWT 实现

### 3.1 核心组件

```
com.institution.coursemanager
├── util
│   └── JwtUtil.java              # JWT 工具类（生成/验证 Token）
├── service
│   └── impl
│       └── AuthServiceImpl.java  # 认证服务（登录/登出/验证）
├── interceptor
│   └── AdminAuthInterceptor.java # 管理员认证拦截器
└── config
    └── WebMvcConfig.java         # 拦截器配置
```

### 3.2 JWT 工具类 (JwtUtil.java)

**位置**：`backend/src/main/java/com/institution/coursemanager/util/JwtUtil.java`

**核心功能**：

| 方法 | 功能 | 使用场景 |
|------|------|----------|
| `generateToken(String username)` | 生成 JWT Token | 用户登录成功后 |
| `parseUsername(String token)` | 从 Token 中解析用户名 | 验证 Token 时 |
| `validateToken(String token)` | 验证 Token 是否有效 | 拦截器验证请求 |

**代码实现**：

```java
@Component
public class JwtUtil {

    private final SecretKey key;      // 签名密钥
    private final long expiration;    // 过期时间

    public JwtUtil(
            @Value("${jwt.secret}") String secret,        // 从配置读取 JWT_SECRET
            @Value("${jwt.expiration}") long expiration) { // 从配置读取过期时间
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token
     * @param username 用户名
     * @return JWT Token 字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(username)           // 设置主题（用户名）
                .issuedAt(now)               // 设置签发时间
                .expiration(expiresAt)       // 设置过期时间
                .signWith(key)               // 使用 JWT_SECRET 签名
                .compact();
    }

    /**
     * 从 Token 中解析用户名
     * @param token JWT Token
     * @return 用户名
     * @throws UnauthorizedException 如果 Token 无效或已过期
     */
    public String parseUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)                    // 使用 JWT_SECRET 验证签名
                    .build()
                    .parseSignedClaims(token)           // 解析并验证 Token
                    .getPayload();
            return claims.getSubject();                 // 返回用户名
        } catch (RuntimeException ex) {
            throw new UnauthorizedException("登录令牌无效或已过期");
        }
    }

    /**
     * 验证 Token 是否有效
     * @param token JWT Token
     * @return true 如果有效
     */
    public boolean validateToken(String token) {
        parseUsername(token);  // 内部调用 parseUsername，失败会抛异常
        return true;
    }
}
```

### 3.3 认证服务 (AuthServiceImpl.java)

**位置**：`backend/src/main/java/com/institution/coursemanager/service/impl/AuthServiceImpl.java`

**核心功能**：

| 方法 | 功能 | 说明 |
|------|------|------|
| `login(AdminLoginDTO dto)` | 管理员登录 | 验证用户名密码，生成 Token |
| `logout(String token)` | 管理员登出 | 将 Token 加入黑名单 |
| `validateToken(String token)` | 验证 Token | 检查 Token 有效性（含黑名单） |

**登录流程**：

```java
@Override
public AdminLoginVO login(AdminLoginDTO dto) {
    // 1. 参数校验
    if (dto == null || !StringUtils.hasText(dto.getUsername()) || !StringUtils.hasText(dto.getPassword())) {
        throw new UnauthorizedException("用户名或密码错误");
    }

    String username = dto.getUsername();

    // 2. 检查账号是否被锁定（登录失败次数限制）
    if (isAccountLocked(username)) {
        throw new UnauthorizedException("账号已锁定，请" + LOCKOUT_DURATION_MINUTES + "分钟后再试");
    }

    // 3. 查询用户并验证密码
    AdminUser adminUser = getOne(new LambdaQueryWrapper<AdminUser>()
            .eq(AdminUser::getUsername, username)
            .eq(AdminUser::getStatus, ENABLED)
            .last("LIMIT 1"));

    if (adminUser == null || !passwordEncoder.matches(dto.getPassword(), adminUser.getPassword())) {
        recordLoginFailure(username);  // 记录登录失败
        throw new UnauthorizedException("用户名或密码错误");
    }

    // 4. 登录成功，清除失败记录
    loginAttempts.remove(username);

    // 5. 生成 JWT Token
    AdminLoginVO vo = new AdminLoginVO();
    vo.setToken(jwtUtil.generateToken(adminUser.getUsername()));  // ← 使用 JWT_SECRET 生成 Token
    vo.setUsername(adminUser.getUsername());
    vo.setRealName(adminUser.getRealName());
    return vo;
}
```

**登出流程**：

```java
@Override
public void logout(String token) {
    if (!StringUtils.hasText(token)) {
        return;
    }
    // 移除 Bearer 前缀
    if (token.startsWith("Bearer ")) {
        token = token.substring(7);
    }
    // 将 Token 加入黑名单，设置过期时间为 24 小时后
    long expiration = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24);
    tokenBlacklist.put(token, expiration);
    // 清理过期的黑名单 Token
    cleanExpiredTokens();
}
```

### 3.4 认证拦截器 (AdminAuthInterceptor.java)

**位置**：`backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java`

**功能**：拦截所有管理员端请求，验证 JWT Token。

```java
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. OPTIONS 请求放行（CORS 预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取 Authorization 请求头
        String authHeader = request.getHeader("Authorization");

        // 3. 验证 Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // 使用 AuthService 验证 Token（包含黑名单检查）
                String username = authService.validateToken(token);
                // 将用户名存入请求属性，供后续使用
                request.setAttribute("currentUsername", username);
                return true;  // 验证通过
            } catch (Exception e) {
                // Token 无效，继续返回 401
            }
        }

        // 4. 验证失败，返回 401 未授权
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.unauthorized("未登录或登录已过期")));
        return false;
    }
}
```

### 3.5 拦截器配置 (WebMvcConfig.java)

**位置**：`backend/src/main/java/com/institution/coursemanager/config/WebMvcConfig.java`

**配置内容**：

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminAuthInterceptor)
            .addPathPatterns("/api/admin/**")                    // 拦截所有管理员接口
            .excludePathPatterns("/api/admin/auth/login", "/api/admin/auth/logout");  // 排除登录登出接口
}
```

**拦截规则**：

| 路径 | 是否拦截 | 说明 |
|------|----------|------|
| `/api/admin/auth/login` | ❌ 排除 | 登录接口无需认证 |
| `/api/admin/auth/logout` | ❌ 排除 | 登出接口无需认证 |
| `/api/admin/**` | ✅ 拦截 | 其他管理员接口需要认证 |
| `/api/**` | ❌ 不拦截 | 访客端接口无需认证 |

---

## 4. JWT_SECRET 的作用与安全性

### 4.1 JWT_SECRET 是什么？

**JWT_SECRET** 是用于**签名和验证 JWT 令牌的密钥**。它是整个 JWT 认证机制的核心安全要素。

```
JWT_SECRET 的作用：

生成 Token 时：                    验证 Token 时：
┌──────────────┐                  ┌──────────────┐
│   Header     │                  │   Header     │
│   Payload    │  ──── 签名 ────► │   Payload    │  ──── 验证 ────►  有效/无效
│   JWT_SECRET │                  │   JWT_SECRET │
└──────────────┘                  └──────────────┘
```

### 4.2 为什么需要 JWT_SECRET？

| 场景 | 有 JWT_SECRET | 无 JWT_SECRET |
|------|---------------|---------------|
| 攻击者伪造 Token | ❌ 无法伪造（没有密钥） | ✅ 可以随意伪造 |
| 篡改 Token 内容 | ❌ 验证失败 | ✅ 无法检测 |
| 服务器信任 Token | ✅ 验证通过 | ❌ 无法验证 |

**核心作用**：确保只有拥有密钥的服务器才能生成和验证有效的 Token。

### 4.3 JWT_SECRET 的安全要求

| 要求 | 说明 | 建议 |
|------|------|------|
| **长度** | 至少 256 位（32 字符） | 64 字符以上更安全 |
| **复杂度** | 包含大小写字母、数字、特殊字符 | 避免使用常见词汇 |
| **保密性** | 不能泄露给他人 | 使用环境变量存储 |
| **唯一性** | 不同环境使用不同密钥 | 开发/测试/生产环境分离 |

**示例**：
```
弱密钥（不安全）：
- "secret"
- "123456"
- "jwt-key"

强密钥（安全）：
- "course-manager-jwt-secret-key-2026-must-be-at-least-256-bits"
- "K8mN2pQ7rT9vX3yA5cE1gH4jL6nO0sU8wB2dF4iK6mN8pQ"
```

### 4.4 项目中的配置

**配置文件**：`backend/src/main/resources/application.yml`

```yaml
# JWT 配置
jwt:
  secret: ${JWT_SECRET:course-manager-jwt-secret-key-2026-must-be-at-least-256-bits}
  expiration: 7200000  # 2小时（毫秒）
```

**配置说明**：

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `jwt.secret` | JWT 签名密钥 | `course-manager-jwt-secret-key-2026-must-be-at-least-256-bits` |
| `jwt.expiration` | Token 过期时间 | `7200000`（2小时） |

**环境变量配置**：

```bash
# 开发环境（使用默认值）
# 无需设置，使用 : 后面的默认值

# 生产环境（必须设置）
export JWT_SECRET="your-very-secure-secret-key-here"
```

---

## 5. JWT 认证流程详解

### 5.1 登录流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. 管理员提交登录请求                                                        │
│    POST /api/admin/login                                                     │
│    Body: { "username": "admin", "password": "admin123" }                     │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. AdminAuthController 接收请求                                              │
│    调用 AuthService.login(dto)                                               │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. AuthServiceImpl.login() 执行                                              │
│    ├─ 参数校验（用户名密码不能为空）                                           │
│    ├─ 检查账号是否被锁定（登录失败次数限制）                                     │
│    ├─ 查询数据库验证用户名密码                                                 │
│    └─ 调用 JwtUtil.generateToken(username)                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. JwtUtil.generateToken() 生成 Token                                        │
│    ├─ 创建 Header: { "alg": "HS256", "typ": "JWT" }                         │
│    ├─ 创建 Payload: { "sub": "admin", "iat": ..., "exp": ... }              │
│    └─ 使用 JWT_SECRET 计算 Signature                                         │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5. 返回登录成功响应                                                          │
│    {                                                                         │
│      "code": 200,                                                            │
│      "message": "success",                                                   │
│      "data": {                                                               │
│        "token": "eyJhbGciOiJIUzI1NiJ9...",                                  │
│        "username": "admin",                                                  │
│        "realName": "管理员"                                                   │
│      }                                                                       │
│    }                                                                         │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5.2 访问受保护接口流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. 前端请求管理员接口                                                        │
│    GET /api/admin/dashboard                                                  │
│    Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. WebMvcConfig 拦截器配置                                                   │
│    匹配路径: /api/admin/**                                                   │
│    排除路径: /api/admin/auth/login, /api/admin/auth/logout                   │
│    → 匹配成功，进入拦截器                                                     │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. AdminAuthInterceptor.preHandle() 执行                                     │
│    ├─ 检查请求方法（OPTIONS 请求放行）                                         │
│    ├─ 获取 Authorization 请求头                                              │
│    └─ 调用 AuthService.validateToken(token)                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 4. AuthServiceImpl.validateToken() 执行                                      │
│    ├─ 检查 Token 是否在黑名单中                                               │
│    └─ 调用 JwtUtil.parseUsername(token)                                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 5. JwtUtil.parseUsername() 验证 Token                                        │
│    ├─ 使用 JWT_SECRET 验证签名                                               │
│    ├─ 检查 Token 是否过期                                                    │
│    └─ 解析出用户名                                                           │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 6. 验证结果处理                                                              │
│    ├─ 成功: 将用户名存入请求属性，允许访问                                      │
│    └─ 失败: 返回 401 未授权响应                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 5.3 登出流程

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ 1. 管理员提交登出请求                                                        │
│    POST /api/admin/auth/logout                                               │
│    Header: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 2. AuthServiceImpl.logout() 执行                                             │
│    ├─ 移除 Bearer 前缀                                                       │
│    ├─ 将 Token 加入黑名单                                                    │
│    └─ 设置黑名单过期时间（24小时）                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│ 3. 返回登出成功响应                                                          │
│    { "code": 200, "message": "success" }                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 6. 安全机制详解

### 6.1 Token 黑名单机制

**目的**：实现 Token 主动失效，防止已登出的 Token 被滥用。

**实现**：

```java
// Token 黑名单存储
private final ConcurrentHashMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

// 登出时加入黑名单
public void logout(String token) {
    long expiration = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(24);
    tokenBlacklist.put(token, expiration);
    cleanExpiredTokens();  // 清理过期的黑名单 Token
}

// 验证时检查黑名单
public String validateToken(String token) {
    if (tokenBlacklist.containsKey(token)) {
        throw new UnauthorizedException("登录令牌已失效");
    }
    return jwtUtil.parseUsername(token);
}
```

**流程**：

```
登出前：
Token A → 有效 ✓

登出时：
Token A → 加入黑名单

登出后：
Token A → 在黑名单中 → 无效 ✅
```

### 6.2 登录失败次数限制

**目的**：防止暴力破解密码。

**配置**：

```java
private static final int MAX_LOGIN_ATTEMPTS = 5;           // 最大失败次数
private static final long LOCKOUT_DURATION_MINUTES = 15;   // 锁定时间（分钟）
```

**流程**：

```
登录失败 1 次 → 记录失败次数
登录失败 2 次 → 记录失败次数
登录失败 3 次 → 记录失败次数
登录失败 4 次 → 记录失败次数
登录失败 5 次 → 账号锁定 15 分钟
                │
                ▼
           15 分钟后自动解锁
```

### 6.3 密码加密存储

**目的**：防止数据库泄露后密码被直接获取。

**实现**：

```java
// 密码加密（注册时）
String encodedPassword = passwordEncoder.encode(rawPassword);

// 密码验证（登录时）
boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
```

**特点**：
- 使用 BCrypt 加密算法
- 每次加密结果不同（加盐）
- 不可逆（无法从加密密码反推原始密码）

---

## 7. 前端集成说明

### 7.1 Token 存储

**位置**：`frontend/src/utils/auth.js`

```javascript
// 获取 Token
export function getToken() {
  return localStorage.getItem('token')
}

// 设置 Token
export function setToken(token) {
  localStorage.setItem('token', token)
}

// 移除 Token
export function removeToken() {
  localStorage.removeItem('token')
}
```

### 7.2 请求拦截器

**位置**：`frontend/src/api/request.js`

```javascript
// 请求拦截器：自动添加 Token
request.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`  // 添加 Bearer 前缀
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理 401 错误
request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      // Token 无效或过期，清除 Token 并跳转到登录页
      removeToken()
      router.push('/admin/login')
    }
    return Promise.reject(error)
  }
)
```

### 7.3 登录流程

**位置**：`frontend/src/views/admin/LoginPage.vue`

```javascript
async function handleLogin() {
  try {
    const res = await login({
      username: form.username,
      password: form.password
    })
    
    if (res.code === 200) {
      // 保存 Token
      setToken(res.data.token)
      // 保存用户信息
      setUser(res.data)
      // 跳转到管理后台
      router.push('/admin/dashboard')
    } else {
      ElMessage.error(res.message)
    }
  } catch (error) {
    ElMessage.error('登录失败')
  }
}
```

---

## 8. 测试用例

### 8.1 登录测试

| 测试编号 | 测试场景 | 输入 | 预期结果 |
|----------|----------|------|----------|
| TC-LOGIN-01 | 正确的用户名和密码 | admin/admin123 | 返回 Token |
| TC-LOGIN-02 | 错误的密码 | admin/wrong | 返回错误提示 |
| TC-LOGIN-03 | 不存在的用户名 | nonexistent/admin123 | 返回错误提示 |
| TC-LOGIN-04 | 用户名为空 | /admin123 | 返回参数错误 |
| TC-LOGIN-05 | 密码为空 | admin/ | 返回参数错误 |
| TC-LOGIN-06 | 连续失败 5 次 | 5 次错误密码 | 账号锁定 15 分钟 |
| TC-LOGIN-07 | 账号被禁用 | disabled/admin123 | 返回错误提示 |

### 8.2 Token 验证测试

| 测试编号 | 测试场景 | 输入 | 预期结果 |
|----------|----------|------|----------|
| TC-AUTH-01 | 有效 Token | 有效 JWT | 允许访问 |
| TC-AUTH-02 | 过期 Token | 过期 JWT | 返回 401 |
| TC-AUTH-03 | 无效签名 | 篡改的 JWT | 返回 401 |
| TC-AUTH-04 | 空 Token | 无 Authorization | 返回 401 |
| TC-AUTH-05 | 黑名单 Token | 已登出的 JWT | 返回 401 |

### 8.3 登出测试

| 测试编号 | 测试场景 | 输入 | 预期结果 |
|----------|----------|------|----------|
| TC-LOGOUT-01 | 正常登出 | 有效 Token | 登出成功 |
| TC-LOGOUT-02 | 使用已登出 Token | 黑名单 Token | 返回 401 |

---

## 9. 常见问题解答

### 9.1 JWT Token 过期后怎么办？

**答**：Token 过期后，用户需要重新登录获取新的 Token。本项目中 Token 有效期为 2 小时。

**前端处理**：
```javascript
// 响应拦截器中处理 401 错误
if (error.response && error.response.status === 401) {
  removeToken()
  router.push('/admin/login')
}
```

### 9.2 如何延长 Token 有效期？

**答**：修改 `application.yml` 中的 `jwt.expiration` 配置：

```yaml
jwt:
  expiration: 86400000  # 24小时（毫秒）
```

**注意**：Token 有效期越长，安全风险越高。建议根据业务需求设置合理的时间。

### 9.3 JWT_SECRET 泄露了怎么办？

**答**：
1. **立即更换** JWT_SECRET
2. **重启** 后端服务
3. **通知** 所有用户重新登录（所有旧 Token 将失效）

### 9.4 为什么使用环境变量存储 JWT_SECRET？

**答**：
1. **安全性**：避免密钥硬编码在代码中
2. **灵活性**：不同环境使用不同密钥
3. **保密性**：密钥不会被提交到代码仓库

### 9.5 Token 被盗用了怎么办？

**答**：
1. **立即登出**：调用登出接口，使 Token 加入黑名单
2. **修改密码**：修改管理员密码
3. **检查日志**：查看是否有异常操作

---

## 10. 相关文件索引

### 10.1 后端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| JwtUtil.java | `backend/src/main/java/com/institution/coursemanager/util/JwtUtil.java` | JWT 工具类 |
| AuthServiceImpl.java | `backend/src/main/java/com/institution/coursemanager/service/impl/AuthServiceImpl.java` | 认证服务实现 |
| AdminAuthInterceptor.java | `backend/src/main/java/com/institution/coursemanager/interceptor/AdminAuthInterceptor.java` | 管理员认证拦截器 |
| WebMvcConfig.java | `backend/src/main/java/com/institution/coursemanager/config/WebMvcConfig.java` | 拦截器配置 |
| application.yml | `backend/src/main/resources/application.yml` | JWT 配置 |

### 10.2 前端文件

| 文件 | 路径 | 说明 |
|------|------|------|
| auth.js | `frontend/src/utils/auth.js` | Token 管理工具 |
| request.js | `frontend/src/api/request.js` | Axios 请求封装 |
| LoginPage.vue | `frontend/src/views/admin/LoginPage.vue` | 登录页面 |

### 10.3 测试文件

| 文件 | 路径 | 说明 |
|------|------|------|
| AdminAuthControllerTest.java | `backend/src/test/java/com/institution/coursemanager/controller/AdminAuthControllerTest.java` | 登录接口测试 |

---

## 11. 总结

### 11.1 JWT 在本项目中的作用

| 功能 | 实现方式 | 说明 |
|------|----------|------|
| **身份认证** | JWT Token | 管理员登录后获取 Token，访问接口时携带 Token |
| **访问控制** | 拦截器验证 | 所有管理员接口需要有效 Token |
| **安全保护** | JWT_SECRET 签名 | 防止 Token 伪造和篡改 |
| **主动失效** | Token 黑名单 | 登出后 Token 立即失效 |
| **暴力破解防护** | 登录失败限制 | 连续失败 5 次锁定 15 分钟 |

### 11.2 安全特性总结

| 特性 | 实现 | 说明 |
|------|------|------|
| ✅ 无状态认证 | JWT Token | 服务器无需存储会话 |
| ✅ 防伪造 | JWT_SECRET 签名 | 只有服务器能生成有效 Token |
| ✅ 防篡改 | 签名验证 | Token 内容被篡改会验证失败 |
| ✅ 过期机制 | exp 声明 | Token 自动过期 |
| ✅ 主动失效 | Token 黑名单 | 登出后立即失效 |
| ✅ 暴力破解防护 | 登录失败限制 | 防止密码被暴力破解 |
| ✅ 密码加密 | BCrypt | 密码不可逆加密存储 |

### 11.3 适用场景

**本项目 JWT 认证适用于**：
- 前后端分离的 Web 应用
- 需要无状态认证的场景
- 需要跨域支持的场景
- 管理后台访问控制

**不适用于**：
- 需要即时失效的场景（JWT 本身无法即时失效，需要黑名单机制）
- 需要存储大量用户信息的场景（Token 大小有限）
- 对安全性要求极高的场景（建议结合 OAuth 2.0）

---

> **文档版本**：v1.0
> **最后更新**：2026-06-12
> **维护者**：ABS
