package com.lbzxks.service;

import com.lbzxks.excel.ScoreExcel;
import com.lbzxks.vo.ClassStatsVO;
import com.lbzxks.vo.ExamStatsVO;
import com.lbzxks.vo.RankingItemVO;

import java.util.List;

public interface StatsService {

    ExamStatsVO summary(Long examId);

    List<RankingItemVO> ranking(Long examId);

    List<ClassStatsVO> classCompare(Long examId);

    List<ScoreExcel> exportScore(Long examId);
}
