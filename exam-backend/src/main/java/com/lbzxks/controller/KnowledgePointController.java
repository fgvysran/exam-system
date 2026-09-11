package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lbzxks.common.Result;
import com.lbzxks.dto.KnowledgePointDTO;
import com.lbzxks.service.KnowledgePointService;
import com.lbzxks.vo.KnowledgePointVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-points")
@Tag(name = "知识点管理")
@RequiredArgsConstructor
public class KnowledgePointController {

    private final KnowledgePointService knowledgePointService;

    @Operation(summary = "知识点树")
    @GetMapping("/tree")
    public Result<List<KnowledgePointVO>> tree(@RequestParam(required = false) Long subjectId) {
        return Result.success(knowledgePointService.tree(subjectId));
    }

    @Operation(summary = "新增知识点")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody KnowledgePointDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        knowledgePointService.add(dto);
        return Result.success();
    }

    @Operation(summary = "修改知识点")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody KnowledgePointDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        knowledgePointService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除知识点")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        knowledgePointService.delete(id);
        return Result.success();
    }
}
