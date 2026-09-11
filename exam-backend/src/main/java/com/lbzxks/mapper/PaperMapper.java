package com.lbzxks.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lbzxks.entity.Paper;
import org.apache.ibatis.annotations.Select;

public interface PaperMapper extends BaseMapper<Paper> {

    /**
     * 统计试卷被考试引用的数量(删除校验用)
     */
    @Select("SELECT COUNT(*) FROM exam WHERE paper_id = #{paperId}")
    long countExamByPaperId(Long paperId);
}
