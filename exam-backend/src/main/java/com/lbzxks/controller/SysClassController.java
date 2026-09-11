package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lbzxks.common.Result;
import com.lbzxks.dto.SysClassDTO;
import com.lbzxks.entity.SysClass;
import com.lbzxks.service.SysClassService;
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
@RequestMapping("/api/classes")
@Tag(name = "班级管理")
@RequiredArgsConstructor
public class SysClassController {

    private final SysClassService sysClassService;

    @Operation(summary = "班级列表")
    @GetMapping
    public Result<List<SysClass>> list() {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(sysClassService.list());
    }

    @Operation(summary = "新增班级")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SysClassDTO dto) {
        StpUtil.checkRole("ADMIN");
        sysClassService.add(dto);
        return Result.success();
    }

    @Operation(summary = "修改班级")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SysClassDTO dto) {
        StpUtil.checkRole("ADMIN");
        sysClassService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StpUtil.checkRole("ADMIN");
        sysClassService.delete(id);
        return Result.success();
    }
}
