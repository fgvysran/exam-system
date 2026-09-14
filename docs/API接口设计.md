# API 接口设计

> 在线调试：启动后端后访问 `http://localhost:8080/doc.html`（Knife4j）

## 一、通用约定

### 1. 统一前缀

所有业务接口以 `/api` 开头。

### 2. 认证方式

使用 **Sa-Token**，登录成功后返回 token，请求时携带（`satoken` header 或 cookie）。除登录接口外均需登录态，各接口另做角色校验。

### 3. 统一响应格式

```json
{ "code": 200, "message": "success", "data": { ... } }
```

分页接口的 `data` 为：

```json
{ "total": 123, "records": [ ... ] }
```

### 4. 角色编码

`ADMIN`（管理员）、`TEACHER`（教师）、`STUDENT`（学生）。

---

## 二、认证模块 `/api/auth`

| 方法 | 路径 | 权限 | 说明 | 请求体 / 参数 |
|------|------|------|------|--------------|
| POST | `/api/auth/login` | 公开 | 登录 | `LoginDTO`（用户名/密码） |
| POST | `/api/auth/logout` | 登录 | 登出 | - |
| GET | `/api/auth/me` | 登录 | 获取当前用户信息 | - |
| PUT | `/api/auth/me` | 登录 | 更新个人资料 | `ProfileDTO` |

## 三、题库模块

### 学科 `/api/subjects`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/subjects` | 登录 | 学科列表 |
| POST | `/api/subjects` | ADMIN/TEACHER | 新增学科 |
| PUT | `/api/subjects/{id}` | ADMIN/TEACHER | 修改学科 |
| DELETE | `/api/subjects/{id}` | ADMIN/TEACHER | 删除学科 |

### 知识点 `/api/knowledge-points`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/knowledge-points/tree?subjectId=` | 登录 | 知识点树（按学科） |
| POST | `/api/knowledge-points` | ADMIN/TEACHER | 新增知识点 |
| PUT | `/api/knowledge-points/{id}` | ADMIN/TEACHER | 修改知识点 |
| DELETE | `/api/knowledge-points/{id}` | ADMIN/TEACHER | 删除知识点 |

### 题目 `/api/questions`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/questions` | 登录 | 题目分页列表（`QuestionQueryDTO`） |
| GET | `/api/questions/{id}` | 登录 | 题目详情 |
| POST | `/api/questions` | ADMIN/TEACHER | 新增题目（`QuestionDTO`） |
| PUT | `/api/questions/{id}` | ADMIN/TEACHER | 修改题目 |
| DELETE | `/api/questions/{id}` | ADMIN/TEACHER | 删除题目 |
| DELETE | `/api/questions/batch` | ADMIN/TEACHER | 批量删除（`List<Long>`） |
| PATCH | `/api/questions/{id}/status` | ADMIN/TEACHER | 启用/停用（`StatusDTO`） |
| GET | `/api/questions/export` | ADMIN/TEACHER | 导出题目 Excel（`QuestionQueryDTO`） |
| POST | `/api/questions/import` | ADMIN/TEACHER | 导入题目 Excel（multipart file） |

## 四、试卷模块 `/api/papers`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/papers` | 登录 | 试卷分页列表（`PaperQueryDTO`） |
| GET | `/api/papers/{id}` | 登录 | 试卷详情（含题目） |
| POST | `/api/papers` | ADMIN/TEACHER | 新增试卷（`PaperDTO`） |
| PUT | `/api/papers/{id}` | ADMIN/TEACHER | 修改试卷 |
| DELETE | `/api/papers/{id}` | ADMIN/TEACHER | 删除试卷 |
| PATCH | `/api/papers/{id}/status` | ADMIN/TEACHER | 发布/取消发布（`StatusDTO`） |
| PUT | `/api/papers/{id}/questions` | ADMIN/TEACHER | 保存组卷（`List<PaperQuestionDTO>`） |

## 五、考试模块

### 考试管理 `/api/exams`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/exams` | ADMIN/TEACHER | 考试分页列表（`ExamQueryDTO`） |
| GET | `/api/exams/{id}` | ADMIN/TEACHER | 考试详情 |
| POST | `/api/exams` | ADMIN/TEACHER | 创建考试（`ExamDTO`） |
| PUT | `/api/exams/{id}` | ADMIN/TEACHER | 修改考试 |
| DELETE | `/api/exams/{id}` | ADMIN/TEACHER | 删除考试 |

### 学生在线答题 `/api/exams`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/exams/my` | 登录 | 我的考试列表 |
| POST | `/api/exams/{id}/start` | 登录 | 开始考试（返回试卷） |
| POST | `/api/exams/{id}/answers` | 登录 | 暂存答案（`List<AnswerDTO>`） |
| POST | `/api/exams/{id}/submit` | 登录 | 交卷并判分（`List<AnswerDTO>`） |
| GET | `/api/exams/{id}/result` | 登录 | 查看考试结果 |

## 六、阅卷模块 `/api/grading`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/grading/pending?examId=` | ADMIN/TEACHER | 待判分列表 |
| POST | `/api/grading/{detailId}` | ADMIN/TEACHER | 给某题打分 + 评语（`GradeDTO`） |
| POST | `/api/grading/regrade?examId=` | ADMIN/TEACHER | 重新判分整场考试 |

## 七、成绩统计模块 `/api/stats`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/stats/exam/{examId}` | ADMIN/TEACHER | 成绩汇总（平均/最高/最低/及格率） |
| GET | `/api/stats/exam/{examId}/ranking` | ADMIN/TEACHER | 成绩排名 |
| GET | `/api/stats/exam/{examId}/class-compare` | ADMIN/TEACHER | 班级对比 |
| GET | `/api/stats/exam/{examId}/export` | ADMIN/TEACHER | 导出成绩 Excel |

## 八、系统管理模块

### 班级管理 `/api/classes`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/classes` | ADMIN/TEACHER | 班级列表 |
| POST | `/api/classes` | ADMIN | 新增班级 |
| PUT | `/api/classes/{id}` | ADMIN | 修改班级 |
| DELETE | `/api/classes/{id}` | ADMIN | 删除班级 |

### 用户管理 `/api/users`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/users` | ADMIN | 用户分页列表（`UserQueryDTO`） |
| POST | `/api/users` | ADMIN | 新增用户（`UserDTO`） |
| PUT | `/api/users/{id}` | ADMIN | 修改用户 |
| DELETE | `/api/users/{id}` | ADMIN | 删除用户 |

---

## 九、关键数据对象（DTO）速查

| DTO | 用途 |
|-----|------|
| `LoginDTO` | 登录（用户名/密码） |
| `QuestionDTO` / `QuestionQueryDTO` | 题目新增/查询 |
| `OptionDTO` / `ProgrammingQuestionDTO` / `TestCaseDTO` | 题目选项 / 编程题 / 测试用例 |
| `PaperDTO` / `PaperQueryDTO` / `PaperQuestionDTO` | 试卷 / 查询 / 组卷题目（含分值排序） |
| `ExamDTO` / `ExamQueryDTO` | 考试创建 / 查询 |
| `AnswerDTO` | 学生提交答案 |
| `GradeDTO` | 阅卷打分 + 评语 |
| `StatusDTO` | 通用状态切换（启用/发布等） |
| `SubjectDTO` / `KnowledgePointDTO` / `SysClassDTO` | 学科 / 知识点 / 班级 |
| `UserDTO` / `UserQueryDTO` / `ProfileDTO` | 用户 / 查询 / 个人资料 |
