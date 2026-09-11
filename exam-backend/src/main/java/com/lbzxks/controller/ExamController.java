package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.Result;
import com.lbzxks.dto.ExamDTO;
import com.lbzxks.dto.ExamQueryDTO;
import com.lbzxks.service.ExamService;
import com.lbzxks.vo.ExamDetailVO;
import com.lbzxks.vo.ExamListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exams")
@Tag(name = "考试管理")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "考试分页列表")
    @GetMapping
    public Result<PageResult<ExamListVO>> page(ExamQueryDTO query) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(examService.page(query));
    }

    @Operation(summary = "考试详情")
    @GetMapping("/{id}")
    public Result<ExamDetailVO> detail(@PathVariable Long id) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(examService.detail(id));
    }

    @Operation(summary = "创建考试")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ExamDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        examService.add(dto);
        return Result.success();
    }

    @Operation(summary = "修改考试")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ExamDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        examService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除考试")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        examService.delete(id);
        return Result.success();
    }
}
