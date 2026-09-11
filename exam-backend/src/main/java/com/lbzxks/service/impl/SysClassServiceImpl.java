package com.lbzxks.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lbzxks.common.exception.BusinessException;
import com.lbzxks.dto.SysClassDTO;
import com.lbzxks.entity.ExamClass;
import com.lbzxks.entity.SysClass;
import com.lbzxks.entity.SysUser;
import com.lbzxks.mapper.ExamClassMapper;
import com.lbzxks.mapper.SysClassMapper;
import com.lbzxks.mapper.SysUserMapper;
import com.lbzxks.service.SysClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysClassServiceImpl implements SysClassService {

    private final SysClassMapper sysClassMapper;
    private final SysUserMapper sysUserMapper;
    private final ExamClassMapper examClassMapper;

    @Override
    public List<SysClass> list() {
        return sysClassMapper.selectList(
                Wrappers.<SysClass>lambdaQuery().orderByAsc(SysClass::getId));
    }

    @Override
    public void add(SysClassDTO dto) {
        long count = sysClassMapper.selectCount(
                Wrappers.<SysClass>lambdaQuery().eq(SysClass::getClassName, dto.getClassName()));
        if (count > 0) {
            throw new BusinessException("班级名称已存在");
        }
        SysClass c = new SysClass();
        c.setClassName(dto.getClassName());
        c.setGrade(dto.getGrade());
        c.setMajor(dto.getMajor());
        sysClassMapper.insert(c);
    }

    @Override
    public void update(Long id, SysClassDTO dto) {
        SysClass c = sysClassMapper.selectById(id);
        if (c == null) {
            throw new BusinessException("班级不存在");
        }
        long count = sysClassMapper.selectCount(
                Wrappers.<SysClass>lambdaQuery()
                        .eq(SysClass::getClassName, dto.getClassName())
                        .ne(SysClass::getId, id));
        if (count > 0) {
            throw new BusinessException("班级名称已存在");
        }
        c.setClassName(dto.getClassName());
        c.setGrade(dto.getGrade());
        c.setMajor(dto.getMajor());
        sysClassMapper.updateById(c);
    }

    @Override
    public void delete(Long id) {
        if (sysClassMapper.selectById(id) == null) {
            throw new BusinessException("班级不存在");
        }
        long studentCount = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getClassId, id));
        if (studentCount > 0) {
            throw new BusinessException("该班级下还有学生，无法删除");
        }
        long examCount = examClassMapper.selectCount(
                Wrappers.<ExamClass>lambdaQuery().eq(ExamClass::getClassId, id));
        if (examCount > 0) {
            throw new BusinessException("该班级已被考试引用，无法删除");
        }
        sysClassMapper.deleteById(id);
    }
}
