package com.xxxx.server.service.impl;

import com.xxxx.server.pojo.Department;
import com.xxxx.server.mapper.DepartmentMapper;
import com.xxxx.server.service.IDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-05
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

}
