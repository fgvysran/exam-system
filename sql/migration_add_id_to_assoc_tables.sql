-- =============================================================
-- 迁移脚本: 为三张多对多关联表添加自增代理主键 id
-- -------------------------------------------------------------
-- 背景: 原 sys_user_role / exam_class / question_knowledge 使用
--       (a, b) 联合主键, 而 MyBatis-Plus 不支持复合主键, 导致启动时
--       告警 "Not found @TableId annotation" 且 xxById 系列方法不可用。
-- 方案: 新增 id 自增主键, 原联合主键改为唯一键, 语义不变
--       (仍保证同一关联不重复), 实体侧同步加 @TableId(AUTO)。
-- 数据: 已有行由 MySQL 自动填充 id=1,2,3..., 不丢数据。
-- 执行: mysql -u root -p exam_system < sql/migration_add_id_to_assoc_tables.sql
-- =============================================================

USE exam_system;

-- 1. 用户-角色关联表
ALTER TABLE sys_user_role DROP PRIMARY KEY;
ALTER TABLE sys_user_role ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
ALTER TABLE sys_user_role ADD UNIQUE KEY uk_user_role (user_id, role_id);

-- 2. 考试-班级关联表
ALTER TABLE exam_class DROP PRIMARY KEY;
ALTER TABLE exam_class ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
ALTER TABLE exam_class ADD UNIQUE KEY uk_exam_class (exam_id, class_id);

-- 3. 题目-知识点关联表
ALTER TABLE question_knowledge DROP PRIMARY KEY;
ALTER TABLE question_knowledge ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
ALTER TABLE question_knowledge ADD UNIQUE KEY uk_question_knowledge (question_id, knowledge_point_id);
