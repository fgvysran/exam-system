package com.lbzxks.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.lbzxks.common.PageResult;
import com.lbzxks.common.Result;
import com.lbzxks.dto.QuestionDTO;
import com.lbzxks.dto.QuestionQueryDTO;
import com.lbzxks.dto.StatusDTO;
import com.lbzxks.excel.QuestionExcel;
import com.lbzxks.service.QuestionService;
import com.lbzxks.vo.ImportResultVO;
import com.lbzxks.vo.QuestionListVO;
import com.lbzxks.vo.QuestionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "题目管理")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "题目分页列表")
    @GetMapping
    public Result<PageResult<QuestionListVO>> page(QuestionQueryDTO query) {
        return Result.success(questionService.page(query));
    }

    @Operation(summary = "题目详情")
    @GetMapping("/{id}")
    public Result<QuestionVO> detail(@PathVariable Long id) {
        return Result.success(questionService.detail(id));
    }

    @Operation(summary = "新增题目")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody QuestionDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        questionService.add(dto);
        return Result.success();
    }

    @Operation(summary = "修改题目")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        questionService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        questionService.delete(id);
        return Result.success();
    }

    @Operation(summary = "批量删除题目")
    @DeleteMapping("/batch")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        questionService.batchDelete(ids);
        return Result.success();
    }

    @Operation(summary = "启用/停用题目")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        questionService.updateStatus(id, dto.getStatus());
        return Result.success();
    }

    @Operation(summary = "导出题目(Excel, 含答案)")
    @GetMapping("/export")
    public void export(QuestionQueryDTO query, HttpServletResponse response) throws IOException {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        List<QuestionExcel> data = questionService.listForExport(query);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("题目导出.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
        EasyExcel.write(response.getOutputStream(), QuestionExcel.class).sheet("题目").doWrite(data);
    }

    @Operation(summary = "批量导入题目(Excel)")
    @PostMapping("/import")
    public Result<ImportResultVO> importExcel(@RequestParam("file") MultipartFile file,
                                              @RequestParam Long subjectId) throws IOException {
        StpUtil.checkRoleOr("ADMIN", "TEACHER");
        return Result.success(questionService.importExcel(file, subjectId));
    }
}
