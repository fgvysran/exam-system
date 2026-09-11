package com.lbzxks.controller;

import com.lbzxks.common.Result;
import com.lbzxks.dto.AnswerDTO;
import com.lbzxks.service.AnswerService;
import com.lbzxks.service.ExamService;
import com.lbzxks.vo.ExamPaperVO;
import com.lbzxks.vo.ExamResultVO;
import com.lbzxks.vo.MyExamVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exams")
@Tag(name = "在线答题")
@RequiredArgsConstructor
public class ExamAnswerController {

    private final ExamService examService;
    private final AnswerService answerService;

    @Operation(summary = "我参加的考试")
    @GetMapping("/my")
    public Result<List<MyExamVO>> my() {
        return Result.success(examService.myExams());
    }

    @Operation(summary = "开始考试(获取试卷)")
    @PostMapping("/{id}/start")
    public Result<ExamPaperVO> start(@PathVariable Long id) {
        return Result.success(answerService.start(id));
    }

    @Operation(summary = "保存答案")
    @PostMapping("/{id}/answers")
    public Result<Void> saveAnswers(@PathVariable Long id, @RequestBody List<AnswerDTO> answers) {
        answerService.saveAnswers(id, answers);
        return Result.success();
    }

    @Operation(summary = "交卷(自动判分)")
    @PostMapping("/{id}/submit")
    public Result<ExamResultVO> submit(@PathVariable Long id, @RequestBody List<AnswerDTO> answers) {
        return Result.success(answerService.submit(id, answers));
    }

    @Operation(summary = "查看我的成绩")
    @GetMapping("/{id}/result")
    public Result<ExamResultVO> result(@PathVariable Long id) {
        return Result.success(answerService.result(id));
    }
}
