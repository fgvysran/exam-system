package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lbzxks.common.Result;
import com.lbzxks.dto.SubjectDTO;
import com.lbzxks.service.SubjectService;
import com.lbzxks.vo.SubjectVO;
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

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@Tag(name = "学科管理")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @Operation(summary = "学科列表(含题目数量)")
    @GetMapping
    public Result<List<SubjectVO>> list() {
        return Result.success(subjectService.list());
    }

    @Operation(summary = "新增学科")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SubjectDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        subjectService.add(dto);
        return Result.success();
    }

    @Operation(summary = "修改学科")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SubjectDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        subjectService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除学科")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        subjectService.delete(id);
        return Result.success();
    }
}
