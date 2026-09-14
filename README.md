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

## 9大业务模块

| #    | 模块     | 干什么的                                 | 对应后端 Controller                 |
| ---- | -------- | ---------------------------------------- | ----------------------------------- |
| 1    | 用户认证 | 登录 / 登出 / 获取当前用户               | AuthController                      |
| 2    | 题库管理 | 学科、知识点、题目 CRUD + Excel 导入导出 | Subject / KnowledgePoint / Question |
| 3    | 试卷管理 | 组卷（选题设分排序）、发布               | PaperController                     |
| 4    | 考试管理 | 创建考试（指定试卷 + 班级 + 时间）       | ExamController                      |
| 5    | 在线答题 | 学生开始考试、保存、交卷                 | ExamAnswerController                |
| 6    | 自动判分 | 交卷时客观题自动判分、主观题挂待判       | AnswerServiceImpl（答题服务内）     |
| 7    | 阅卷     | 教师对待判题打分 + 评语                  | GradingController                   |
| 8    | 成绩统计 | 汇总 / 排名 / 班级对比 / 导出 Excel      | StatsController                     |
| 9    | 系统管理 | 班级管理 + 用户（学生 / 教师）管理       | SysClass / SysUser                  |

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

数据库连接参数通过环境变量 / 本地配置文件注入，**密码不写入仓库**：

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `DB_HOST` | 数据库地址 | localhost |
| `DB_PORT` | 端口 | 3306 |
| `DB_NAME` | 库名 | exam_system |
| `DB_USERNAME` | 用户名 | root |
| `DB_PASSWORD` | 密码 | 无（必填） |

密码有 3 种注入方式（任选其一）：

1. **本地配置文件（推荐）**：复制 `exam-backend/.env.properties.example` 为 `exam-backend/.env.properties` 并填入真实密码。该文件已被 `.gitignore` 忽略，`application.yml` 通过 `spring.config.import` 自动加载。
2. **环境变量**：在 IDEA 的 Run Configuration 里设置 `DB_PASSWORD`，或命令行 `-DDB_PASSWORD=你的密码`。
3. 其它连接参数（`DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME`）同理可用环境变量覆盖。

### 接口文档

启动后端后访问 http://localhost:8080/doc.html （Knife4j 在线调试）。

## 数据库设计

共 15 张表，题目与试卷解耦、答题明细支撑多维统计。详见 [`docs/数据库设计.md`](docs/数据库设计.md)。

## 界面截图

> TODO：补充主要功能界面截图

 ####  登录页：

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\login.png)

####  首页仪表盘

##### 管理员

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\首页仪表盘.png)

##### 老师

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\首页仪表盘老师.png)

##### 学生

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\首页仪表盘学生.png)

#### 题目管理

老师和管理员才有

###### 学科管理

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\学科管理.png)

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\学科管理添加.png)

###### 知识点管理

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\知识点管理树状结构.png)

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\知识点添加.png)

###### 题目管理

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\题目管理.png)

###### 单选题添加

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\单选题添加.png)

###### 多选题添加

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\多选题添加.png)

###### 判断题添加

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\判断题添加.png)

###### 填空题添加

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\填空题添加.png)

###### 简答题添加

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\简答题添加.png)

###### 论述题添加

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\论述题添加.png)

#### 试卷管理

老师和管理员才有

###### 试卷管理

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\试卷管理.png)

###### 试卷管理添加试卷

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\试卷管理添加试卷.png)

###### 试卷管理组卷

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\试卷管理组卷.png)

#### 考试管理

老师和管理员才有

###### 考试管理

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\考试管理.png)

###### 考试管理创建考试

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\考试管理新增考试.png)

###### 考试管理编辑考试

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\考试管理编辑考试.png)

#### 班级管理

管理员才有

###### 班级管理

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\班级管理.png)

###### 班级管理新增班级

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\班级管理新增班级.png)

###### 班级管理编辑班级

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\班级管理编辑编辑班级.png)

###### 班级管理删除班级

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\班级管理删除班级.png)

#### 用户管理

管理员才有

###### 用户管理

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\用户管理.png)

###### 用户管理新增用户

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\用户管理新增用户.png)

###### 用户管理编辑用户

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\用户管理编辑用户.png)

###### 用户管理删除用户

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\用户管理删除用户.png)

#### 阅卷

管理员和老师可以阅卷

###### 阅卷主页

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\阅卷.png)

###### 阅卷批改

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\阅卷批改示例.png)

#### 成绩统计

老师和管理员才可以

###### 成绩统计

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\成绩统计.png)

###### 成绩导出excel

![](C:\Users\30867\Desktop\Desktop2\项目系统\截图\成绩统计导出.png)

## License

MIT
