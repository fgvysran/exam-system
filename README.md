# 智考在线考试系统（SmartExam）

一个前后端分离的在线考试系统，覆盖**题库管理 → 试卷组卷 → 在线答题 → 自动判分 → 阅卷 → 成绩统计导出**的完整业务闭环。

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.5、MyBatis-Plus、Sa-Token、MySQL 8 |
| 前端 | Vue3、Element Plus、Vite、Pinia、Vue Router、Axios |
| 其他 | EasyExcel（导入导出）、Knife4j（接口文档）、Hutool |

## 功能特性

- **题库管理**：支持 7 种题型（单选 / 多选 / 判断 / 填空 / 简答 / 论述 / 编程），Excel 批量导入导出
- **试卷组卷**：题目与试卷解耦（多对多 + 分值快照），拖拽排序、实时算总分
- **在线答题**：学生开始考试、保存答案、交卷
- **自动判分引擎**：单选 / 多选 / 判断 / 填空 / 简答自动判分，简答题支持「得分点 + 等价答案 + 权重」自定义判分语法
- **人工阅卷**：论述 / 编程等主观题走待判队列，教师打分 + 评语
- **成绩统计**：单场汇总（平均 / 最高 / 最低 / 及格率）、排名、班级对比、Excel 导出
- **RBAC 三级权限**：管理员 / 教师 / 学生，接口级角色鉴权、读写分离

## 项目结构

```
.
├── exam-backend/          # 后端（Spring Boot）
│   └── src/main/
│       ├── java/com/lbzxks/
│       │   ├── controller/   # 控制层
│       │   ├── service/      # 业务层
│       │   ├── mapper/       # 持久层
│       │   ├── entity/       # 实体
│       │   ├── dto/ vo/      # 入参/出参
│       │   ├── common/       # 统一响应、异常、常量
│       │   └── config/       # 配置
│       └── resources/application.yml
├── exam-frontend/         # 前端（Vue3）
│   └── src/
│       ├── api/           # 接口封装
│       ├── views/         # 页面
│       ├── router/ store/ layout/
│       └── utils/
├── sql/schema.sql         # 建库脚本（15 张表）
└── docs/                  # 设计文档
    ├── 数据库设计.md
    ├── 后端工程骨架.md
    └── API接口设计.md
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 18+

### 1. 建库

用 MySQL 执行 `sql/schema.sql`（创建 `exam_system` 库 + 15 张表 + 3 个角色）。

```bash
mysql -u root -p < sql/schema.sql
```

### 2. 启动后端

```bash
cd exam-backend
mvn spring-boot:run
```

- 默认管理员账号 `admin / 123456`（启动时自动创建）
- 数据库账号密码通过环境变量配置（见下）

### 3. 启动前端

```bash
cd exam-frontend
npm install
npm run dev
```

访问 http://localhost:5173

### 4. 配置数据库连接

`application.yml` 中数据库连接通过环境变量配置：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `DB_HOST` | 数据库地址 | localhost |
| `DB_PORT` | 端口 | 3306 |
| `DB_NAME` | 库名 | exam_system |
| `DB_USERNAME` | 用户名 | root |
| `DB_PASSWORD` | 密码 | 123456 |

本地运行时在 IDEA 的 Run Configuration 里设置环境变量，或命令行 `-DDB_PASSWORD=你的密码`。

### 接口文档

启动后端后访问 http://localhost:8080/doc.html （Knife4j 在线调试）。

## 数据库设计

共 15 张表，题目与试卷解耦、答题明细支撑多维统计。详见 [`docs/数据库设计.md`](docs/数据库设计.md)。

## 界面截图

> TODO：补充主要功能界面截图

## License

MIT
