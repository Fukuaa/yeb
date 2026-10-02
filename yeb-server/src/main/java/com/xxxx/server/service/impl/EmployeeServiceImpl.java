package com.xxxx.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxxx.server.mapper.EmployeeMapper;
import com.xxxx.server.mapper.MailLogMapper;
import com.xxxx.server.pojo.*;
import com.xxxx.server.service.IEmployeeService;
import com.xxxx.server.service.MailOutboxService;
import org.springframework.transaction.annotation.Transactional;
import javafx.scene.input.DataFormat;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-10
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements IEmployeeService {
    @Autowired
    private EmployeeMapper employeeMapper;
    @Override
    public RespPageBean getEmployeeByPage(Integer currentPage, Integer size, Employee employee, LocalDate[] beginDateScope) {
        Page<Employee> page = new Page(currentPage,size);
        IPage<Employee> employeeByPage = employeeMapper.getEmployeeByPage(page,employee,beginDateScope);

        RespPageBean respPageBean = new RespPageBean(employeeByPage.getTotal(),employeeByPage.getRecords());
        return respPageBean;
    }

    @Override
    public RespBean maxWordID() {
        List<Map<String,Object>> maps = employeeMapper.selectMaps(new QueryWrapper<Employee>().select("MAX(workID) AS maxWorkID"));
        Object maxWorkID = maps.isEmpty() ? null : maps.get(0).get("maxWorkID");
        int nextWorkID = maxWorkID == null ? 1 : Integer.parseInt(maxWorkID.toString()) + 1;
        return RespBean.success(null, String.format("%08d", nextWorkID));
    }
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MailLogMapper mailLogMapper;
    @Autowired
    private MailOutboxService mailOutbox;
    @Override
    @Transactional
    public RespBean addEmp(Employee employee) {
        LocalDate beginContract = employee.getBeginContract();
        LocalDate endContract = employee.getEndContract();
        long days = beginContract.until(endContract, ChronoUnit.DAYS);
        DecimalFormat decimalFormat = new DecimalFormat("##.00");
        employee.setContractTerm(Double.parseDouble(decimalFormat.format(days/365.00)));
        if(1==employeeMapper.insert(employee)){
            mailOutbox.enqueueWelcome(employee.getId());
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }

    @Override
    public List<Employee> getEmployee(Integer id) {
        return employeeMapper.getEmployee(id);
    }


    @Override
    public RespPageBean getEmployeeWithSalary(Integer currentPage, Integer size) {
        Page<Employee> page = new Page<>(currentPage,size);
        IPage<Employee> iPage = employeeMapper.getEmployeeWithSalary(page);
        return new RespPageBean(iPage.getTotal(), iPage.getRecords());
    }

}
