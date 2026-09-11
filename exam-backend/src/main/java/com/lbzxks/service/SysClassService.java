package com.lbzxks.service;

import com.lbzxks.dto.SysClassDTO;
import com.lbzxks.entity.SysClass;

import java.util.List;

public interface SysClassService {

    List<SysClass> list();

    void add(SysClassDTO dto);

    void update(Long id, SysClassDTO dto);

    void delete(Long id);
}
