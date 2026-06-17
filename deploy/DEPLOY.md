# DevFlow 部署指南

前后端已打包为 **单个 Docker 镜像**（Spring Boot 托管前端 + API），一条命令即可对外访问。

默认账号：`admin` / `admin123`

---

## 方案一：Docker Compose（推荐，适合免费云主机）

适用：**Oracle Cloud 免费 VM**、**阿里云/腾讯云轻量**、有公网 IP 的电脑。

### 1. 安装 Docker

- Windows：安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- Linux：`curl -fsSL https://get.docker.com | sh`

### 2. 启动

```powershell
cd D:\项目\ProductionPlatform
docker compose up -d --build
```

首次构建约 5～10 分钟。

### 3. 访问

- 本机：http://localhost:8080
- 其他电脑：http://你的公网IP:8080

### 4. 云服务器安全组

在控制台 **入站规则** 放行 **TCP 8080**（或你设置的 `APP_PORT`）。

### 5. 常用命令

```powershell
docker compose logs -f app    # 查看日志
docker compose down         # 停止
docker compose up -d --build  # 更新后重新部署
```

环境变量（可选，新建 `.env` 文件）：

```env
APP_PORT=8080
MYSQL_ROOT_PASSWORD=devflow123
MYSQL_PORT=3306
```

---

## 方案二：Render 免费 Web 服务 + 免费 MySQL

适用：无自己的服务器，用 Render 提供 HTTPS 公网地址。

> Render 免费实例 **15 分钟无访问会休眠**，首次打开需等待约 30～60 秒唤醒。

### 1. 准备代码仓库

将项目推送到 **GitHub**（私有/公开均可）。

### 2. 申请免费 MySQL

任选其一（注册后创建 MySQL 8 数据库，记下连接信息）：

| 平台 | 说明 |
|------|------|
| [TiDB Cloud](https://tidbcloud.com) Serverless | MySQL 兼容，有免费额度 |
| [Railway](https://railway.app) | 每月约 $5 免费额度，可建 MySQL |
| [Aiven](https://aiven.io) | 有 MySQL 试用 |

连接串示例：

```text
jdbc:mysql://主机:4000/devflow?useSSL=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8
```

### 3. 在 Render 部署

1. 打开 https://render.com 注册并关联 GitHub
2. **New → Blueprint** 选择本仓库（会自动读取 `render.yaml`）
3. 在环境变量中填写：

| 变量 | 值 |
|------|-----|
| `DATABASE_URL` | 上一步的 JDBC 连接串 |
| `DATABASE_USERNAME` | 数据库用户名 |
| `DATABASE_PASSWORD` | 数据库密码 |
| `SQL_INIT_MODE` | `always`（首次部署；数据稳定后改为 `never`） |

4. 部署完成后访问：`https://devflow-xxxx.onrender.com`

---

## 方案三：前后端分离（可选）

前端部署到 **Vercel / Cloudflare Pages**，后端单独部署。

1. 前端构建时设置环境变量：`VITE_API_BASE_URL=https://你的后端域名/api`
2. 后端设置：`CORS_ALLOWED_ORIGINS=https://你的前端域名`

---

## 故障排查

| 现象 | 处理 |
|------|------|
| 页面打不开 | 检查安全组/防火墙是否放行 8080 |
| 接口 500 | `docker compose logs app` 查看数据库是否连上 |
| 数据库连接失败 | 确认 `DATABASE_URL` 含 `allowPublicKeyRetrieval=true`（非 SSL 时） |
| Render 冷启动慢 | 免费版正常现象，可定时 ping 保活 |

---

## 文件说明

| 文件 | 作用 |
|------|------|
| `Dockerfile` | 构建前后端一体镜像 |
| `docker-compose.yml` | 本地/云主机一键启动 MySQL + 应用 |
| `render.yaml` | Render 平台 Blueprint |
| `application-prod.yaml` | 生产环境配置（读环境变量） |
| `schema-cloud.sql` | 云端建表与初始数据（不删表） |
