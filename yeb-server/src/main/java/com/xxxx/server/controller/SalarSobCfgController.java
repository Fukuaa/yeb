package com.xxxx.server.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxxx.server.pojo.Employee;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.pojo.Salary;
import com.xxxx.server.service.IEmployeeService;
import com.xxxx.server.service.ISalaryService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salary/sobcfg")
public class SalarSobCfgController {
    @Autowired
    private ISalaryService salaryService;
    @Autowired
    private IEmployeeService employeeService;
    @ApiOperation("获取所有工资账套")
    @GetMapping("/salaries")
    public List<Salary> getAllSalaries(){
        return salaryService.list();
    }
    @ApiOperation("获取所有员工账套")
    @GetMapping("/")
    public List<Salary> getEmployeeWithSalary(@RequestParam(defaultValue = "1") Integer currentPage
            ,@RequestParam(defaultValue = "10") Integer size){

        return employeeService.getEmployeeWithSalary(currentPage,size);
    }

    @ApiOperation("更新员工账套")
    @PutMapping("/")
    public RespBean updateEmployeeSalarry(Integer eid,Integer sid){
        if (employeeService.update(new UpdateWrapper<Employee>().set("sqlaryId",sid).eq("id",eid))){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }
}
