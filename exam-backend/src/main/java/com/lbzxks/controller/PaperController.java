package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.Result;
import com.lbzxks.dto.PaperDTO;
import com.lbzxks.dto.PaperQueryDTO;
import com.lbzxks.dto.PaperQuestionDTO;
import com.lbzxks.dto.StatusDTO;
import com.lbzxks.service.PaperService;
import com.lbzxks.vo.PaperDetailVO;
import com.lbzxks.vo.PaperListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/papers")
@Tag(name = "试卷管理")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @Operation(summary = "试卷分页列表")
    @GetMapping
    public Result<PageResult<PaperListVO>> page(PaperQueryDTO query) {
        return Result.success(paperService.page(query));
    }

    @Operation(summary = "试卷详情")
    @GetMapping("/{id}")
    public Result<PaperDetailVO> detail(@PathVariable Long id) {
        return Result.success(paperService.detail(id));
    }

    @Operation(summary = "创建试卷")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody PaperDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        paperService.add(dto);
        return Result.success();
    }

    @Operation(summary = "修改试卷")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PaperDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        paperService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除试卷")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        paperService.delete(id);
        return Result.success();
    }

    @Operation(summary = "发布/取消发布试卷")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        paperService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "保存试卷题目(组卷)")
    @PutMapping("/{id}/questions")
    public Result<Void> saveQuestions(@PathVariable Long id, @RequestBody List<PaperQuestionDTO> questions) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        paperService.saveQuestions(id, questions);
        return Result.success();
    }
}
