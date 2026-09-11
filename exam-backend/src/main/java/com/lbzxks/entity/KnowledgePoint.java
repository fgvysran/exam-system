package com.lbzxks.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点表 (knowledge_point)
 */
@Data
@TableName("knowledge_point")
public class KnowledgePoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    private String name;

    /** 父知识点ID, 0 表示根节点 */
    private Long parentId;

    private LocalDateTime createTime;
}
