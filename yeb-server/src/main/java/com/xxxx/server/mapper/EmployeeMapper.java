package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxxx.server.pojo.Employee;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-10
 */
public interface EmployeeMapper extends BaseMapper<Employee> {

    IPage<Employee> getEmployeeByPage(Page<Employee> page, @Param("employee") Employee employee,@Param("beginDateScope") LocalDate[] beginDateScope);

    List<Employee> getEmployee(Integer id);

    Page getEmployeeWithSalary(Page<Employee> page);
}
