package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.lbzxks.common.Result;
import com.lbzxks.excel.ScoreExcel;
import com.lbzxks.service.StatsService;
import com.lbzxks.vo.ClassStatsVO;
import com.lbzxks.vo.ExamStatsVO;
import com.lbzxks.vo.RankingItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@Tag(name = "成绩统计")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "单场考试统计汇总")
    @GetMapping("/exam/{examId}")
    public Result<ExamStatsVO> summary(@PathVariable Long examId) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(statsService.summary(examId));
    }

    @Operation(summary = "成绩排名")
    @GetMapping("/exam/{examId}/ranking")
    public Result<List<RankingItemVO>> ranking(@PathVariable Long examId) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(statsService.ranking(examId));
    }

    @Operation(summary = "班级对比")
    @GetMapping("/exam/{examId}/class-compare")
    public Result<List<ClassStatsVO>> classCompare(@PathVariable Long examId) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(statsService.classCompare(examId));
    }

    @Operation(summary = "导出成绩(Excel)")
    @GetMapping("/exam/{examId}/export")
    public void export(@PathVariable Long examId, HttpServletResponse response) throws IOException {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        List<ScoreExcel> data = statsService.exportScore(examId);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("成绩表.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        EasyExcel.write(response.getOutputStream(), ScoreExcel.class).sheet("成绩").doWrite(data);
    }
}
