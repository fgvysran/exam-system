-- =============================================================
-- 在线考试系统 数据库设计 (MySQL 8.0+)
-- 数据库: exam_system   字符集: utf8mb4
-- 设计要点:
--   1. 题目(question)与试卷(paper)解耦, 通过 paper_question 关联并快照分值
--   2. 答题明细(answer_detail)作为成绩统计的原始数据, 支撑多维聚合与报表导出
--   3. 全题型支持: 单选/多选/判断/填空/简答/论述/编程(填空简答可配置自动/人工判分)
-- =============================================================

DROP DATABASE IF EXISTS exam_system;
CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE exam_system;

-- ---------------------------------------------------------------
-- 一、用户与权限域
-- ---------------------------------------------------------------

-- 角色表
CREATE TABLE sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_code   VARCHAR(50)  NOT NULL COMMENT '角色编码: ADMIN/TEACHER/STUDENT',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 班级表 (学生归属, 用于班级/年级维度统计)
CREATE TABLE sys_class (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '班级ID',
    class_name  VARCHAR(50) NOT NULL COMMENT '班级名称',
    grade       VARCHAR(20) COMMENT '年级, 如 2023级',
    major       VARCHAR(50) COMMENT '专业/院系',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_class_name (class_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 用户表
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '登录名',
    password    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt存储)',
    real_name   VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    email       VARCHAR(100) COMMENT '邮箱',
    phone       VARCHAR(20)  COMMENT '手机号',
    class_id    BIGINT       COMMENT '所属班级(仅学生)',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_class_id (class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户-角色关联表
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ---------------------------------------------------------------
-- 二、题库域
-- ---------------------------------------------------------------

-- 学科/科目表
CREATE TABLE subject (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '学科ID',
    name        VARCHAR(50) NOT NULL COMMENT '学科名称',
    code        VARCHAR(50) COMMENT '学科编码',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

-- 知识点表 (题目按知识点归类, 支持按知识点统计正确率)
CREATE TABLE knowledge_point (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
    subject_id BIGINT      NOT NULL COMMENT '所属学科',
    name       VARCHAR(100) NOT NULL COMMENT '知识点名称',
    parent_id  BIGINT      DEFAULT 0 COMMENT '父知识点(树形, 0为根)',
    create_time DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点表';

-- 题目表 (题库核心)
CREATE TABLE question (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    subject_id    BIGINT        NOT NULL COMMENT '所属学科',
    question_type TINYINT       NOT NULL COMMENT '题型: 1单选 2多选 3判断 4填空 5简答 6论述(主观) 7编程',
    content       TEXT          NOT NULL COMMENT '题干',
    options       JSON          COMMENT '选项(单选/多选), 如 [{"key":"A","text":"..."}]',
    answer        TEXT          NOT NULL COMMENT '标准答案: 选择存"A"或"A,C"; 判断存"T"/"F"; 填空存JSON数组["北京","上海"]',
    analysis      TEXT          COMMENT '答案解析',
    difficulty    TINYINT       NOT NULL DEFAULT 2 COMMENT '难度: 1易 2中 3难',
    default_score DECIMAL(5,1)  NOT NULL DEFAULT 5 COMMENT '默认分值',
    creator_id    BIGINT        COMMENT '创建人',
    status        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1启用 0停用',
    judge_mode    TINYINT       NOT NULL DEFAULT 1 COMMENT '判分方式: 1自动 2人工(仅填空/简答生效)',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_subject_type (subject_id, question_type),
    KEY idx_difficulty (difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 题目-知识点关联表 (多对多)
CREATE TABLE question_knowledge (
    question_id        BIGINT NOT NULL,
    knowledge_point_id BIGINT NOT NULL,
    PRIMARY KEY (question_id, knowledge_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目知识点关联表';

-- 编程题扩展表 (仅题型=7时存在)
CREATE TABLE programming_question (
    question_id  BIGINT       NOT NULL COMMENT '题目ID',
    languages    VARCHAR(200) COMMENT '支持语言, 逗号分隔, 如 java,cpp,python',
    time_limit   INT          COMMENT '时间限制(毫秒)',
    memory_limit INT          COMMENT '内存限制(MB)',
    test_cases   JSON         COMMENT '测试用例, 如 [{"input":"1 2","output":"3"}]',
    PRIMARY KEY (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编程题扩展表';

-- ---------------------------------------------------------------
-- 三、试卷域
-- ---------------------------------------------------------------

-- 试卷表
CREATE TABLE paper (
    id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '试卷ID',
    name        VARCHAR(100)  NOT NULL COMMENT '试卷名称',
    subject_id  BIGINT        NOT NULL COMMENT '所属学科',
    total_score DECIMAL(6,1)  NOT NULL DEFAULT 100 COMMENT '总分',
    duration    INT           NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
    difficulty  TINYINT       COMMENT '整体难度',
    description VARCHAR(500)  COMMENT '试卷说明',
    creator_id  BIGINT        COMMENT '创建人',
    status      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1草稿 2发布',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_subject (subject_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 试卷-题目关联表 (记录每题在该试卷中的分值)
CREATE TABLE paper_question (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    paper_id    BIGINT       NOT NULL,
    question_id BIGINT       NOT NULL,
    score       DECIMAL(5,1) NOT NULL COMMENT '该题在本试卷中的分值',
    sort        INT          NOT NULL DEFAULT 0 COMMENT '题目顺序',
    PRIMARY KEY (id),
    UNIQUE KEY uk_paper_question (paper_id, question_id),
    KEY idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表';

-- ---------------------------------------------------------------
-- 四、考试与答题域
-- ---------------------------------------------------------------

-- 考试安排表
CREATE TABLE exam (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '考试ID',
    name        VARCHAR(100) NOT NULL COMMENT '考试名称',
    paper_id    BIGINT       NOT NULL COMMENT '试卷ID',
    start_time  DATETIME     NOT NULL COMMENT '开始时间',
    end_time    DATETIME     NOT NULL COMMENT '结束时间',
    duration    INT          COMMENT '作答时长(分钟), 为空则取试卷时长',
    creator_id  BIGINT       COMMENT '创建人',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1未开始 2进行中 3已结束',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_paper (paper_id),
    KEY idx_time (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试安排表';

-- 考试-班级关联表 (指定参加考试的班级)
CREATE TABLE exam_class (
    exam_id  BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    PRIMARY KEY (exam_id, class_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试班级关联表';

-- 作答记录表 (一次考试一条记录, 承载最终成绩)
CREATE TABLE exam_record (
    id               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    exam_id          BIGINT        NOT NULL COMMENT '考试ID',
    paper_id         BIGINT        NOT NULL COMMENT '试卷ID',
    user_id          BIGINT        NOT NULL COMMENT '考生ID',
    start_time       DATETIME      COMMENT '开始作答时间',
    submit_time      DATETIME      COMMENT '交卷时间',
    objective_score  DECIMAL(6,1)  NOT NULL DEFAULT 0 COMMENT '客观题得分',
    subjective_score DECIMAL(6,1)  NOT NULL DEFAULT 0 COMMENT '主观题得分',
    total_score      DECIMAL(6,1)  NOT NULL DEFAULT 0 COMMENT '总分',
    status           TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 1进行中 2已交卷 3已判分',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id),
    KEY idx_paper (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='作答记录表';

-- 答题明细表 (成绩统计的原始数据)
CREATE TABLE answer_detail (
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    exam_record_id BIGINT       NOT NULL COMMENT '作答记录ID',
    question_id    BIGINT       NOT NULL COMMENT '题目ID',
    question_type  TINYINT      NOT NULL COMMENT '冗余题型, 便于统计',
    user_answer    TEXT         COMMENT '考生答案',
    is_correct     TINYINT      COMMENT '是否正确(客观题): 1对 0错',
    score          DECIMAL(5,1) NOT NULL DEFAULT 0 COMMENT '本题得分',
    judge_status   TINYINT      NOT NULL DEFAULT 0 COMMENT '判分状态: 0待判 1判分中 2已判',
    judge_by       BIGINT       COMMENT '阅卷人',
    judge_time     DATETIME     COMMENT '阅卷时间',
    comment        VARCHAR(500) COMMENT '阅卷评语',
    PRIMARY KEY (id),
    UNIQUE KEY uk_record_question (exam_record_id, question_id),
    KEY idx_question (question_id),
    KEY idx_judge (judge_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题明细表';

-- ---------------------------------------------------------------
-- 初始化数据: 角色
-- 说明: 默认管理员账号 admin/123456 由后端 DataInitializer 启动时自动创建,
--       密码用 BCrypt 在运行时生成, 避免在 SQL 里硬编码哈希。
-- ---------------------------------------------------------------

INSERT INTO sys_role (role_name, role_code, description) VALUES
('管理员', 'ADMIN',   '系统管理员'),
('教师',   'TEACHER', '教师/出题人/阅卷人'),
('学生',   'STUDENT', '考生');