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

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-05
 */
@RestController
@RequestMapping("/salary/sob")
public class SalaryController {
    @Autowired
    private ISalaryService salaryService;
    @ApiOperation("获取所有工资账套")
    @GetMapping("/")
    public List<Salary> getAllSalaries(){
        return salaryService.list();
    }
    @ApiOperation("添加工资账套")
    @PostMapping("/")
    public RespBean addSalary(@RequestBody Salary salary){
        salary.setCreateDate(LocalDateTime.now());
        if (salaryService.save(salary)){
            return RespBean.success("添加成功");
        }
        return RespBean.error("添加失败");
    }
    @ApiOperation("删除工资账套")
    @DeleteMapping("/{1}")
    public RespBean deleteSalary(@PathVariable Integer id){
        if (salaryService.removeById(id)){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }
    @ApiOperation("更新员工账套")
    @PutMapping("/")
    public RespBean updateSalary(@PathVariable Salary salary){
        if (salaryService.updateById(salary)){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }

}
