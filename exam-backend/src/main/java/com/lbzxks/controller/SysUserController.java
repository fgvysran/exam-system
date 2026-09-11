package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.Result;
import com.lbzxks.dto.UserDTO;
import com.lbzxks.dto.UserQueryDTO;
import com.lbzxks.service.SysUserService;
import com.lbzxks.vo.UserListVO;
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
@RequestMapping("/api/users")
@Tag(name = "学生管理")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    @Operation(summary = "学生分页列表")
    @GetMapping
    public Result<PageResult<UserListVO>> page(UserQueryDTO query) {
        StpUtil.checkRole("ADMIN");
        return Result.success(sysUserService.page(query));
    }

    @Operation(summary = "新增学生")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody UserDTO dto) {
        StpUtil.checkRole("ADMIN");
        sysUserService.add(dto);
        return Result.success();
    }

    @Operation(summary = "修改学生")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        StpUtil.checkRole("ADMIN");
        sysUserService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StpUtil.checkRole("ADMIN");
        sysUserService.delete(id);
        return Result.success();
    }
}
