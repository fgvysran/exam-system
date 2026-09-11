package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.Result;
import com.lbzxks.dto.GradeDTO;
import com.lbzxks.service.GradingService;
import com.lbzxks.vo.GradingItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grading")
@Tag(name = "阅卷")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    @Operation(summary = "待阅卷列表")
    @GetMapping("/pending")
    public Result<PageResult<GradingItemVO>> pending(@RequestParam(required = false) Long examId,
                                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(gradingService.pending(examId, pageNum, pageSize));
    }

    @Operation(summary = "阅卷打分")
    @PostMapping("/{detailId}")
    public Result<Void> grade(@PathVariable Long detailId, @Valid @RequestBody GradeDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        gradingService.grade(detailId, dto);
        return Result.success();
    }
}
