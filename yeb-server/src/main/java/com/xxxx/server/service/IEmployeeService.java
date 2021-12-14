package com.xxxx.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.server.pojo.Employee;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.pojo.RespPageBean;
import com.xxxx.server.pojo.Salary;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-10
 */
public interface IEmployeeService extends IService<Employee> {

    RespPageBean getEmployeeByPage(Integer currentPage, Integer size, Employee employee, LocalDate[] beginDateScope);

    RespBean maxWordID();

    RespBean addEmp(Employee employee);

    List<Employee> getEmployee(Integer id);

    List<Salary> getEmployeeWithSalary(Integer currentPage, Integer size);
}
