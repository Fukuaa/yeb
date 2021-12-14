package com.xxxx.server.controller;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import com.xxxx.server.pojo.*;
import com.xxxx.server.service.*;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-10
 */
@RestController
@RequestMapping("/employee/basic")
public class EmployeeController {
    @Autowired
    private IEmployeeService employeeService;
    @ApiOperation("获取所有员工(分页)")
    @GetMapping("/")
    public RespPageBean getEmployee(@RequestParam(defaultValue = "1") Integer currentPage,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    Employee employee,
                                    LocalDate[] beginDateScope){
        return employeeService.getEmployeeByPage(currentPage,size,employee,beginDateScope);
    }
    @Autowired
    private IPoliticsStatusService politicsStatusService;
    @ApiOperation("获取所有政治面貌")
    @GetMapping("/politicsStatus")
    public List<PoliticsStatus> getAllPoliticsStatus(){
        return politicsStatusService.list();
    }
    @Autowired
    private IJoblevelService joblevelService;
    @ApiOperation("获取所有职称")
    @GetMapping("/joblevels")
    public List<Joblevel> getAllJoblevel(){
        return joblevelService.list();
    }
    @Autowired
    private INationService nationService;
    @ApiOperation("获取所有民族")
    @GetMapping("/nations")
    public List<Nation> getAllNation(){
        return nationService.list();
    }
    @Autowired
    private IPositionService positionService;
    @ApiOperation("获取所有职位")
    @GetMapping("/positions")
    public List<Position> getAllPosition(){
        return positionService.list();
    }
    @Autowired
    private IDepartmentService departmentService;
    @ApiOperation("获取所有部门")
    @GetMapping("/deps")
    public List<Department> getAllDepartments(){
        return departmentService.getAllDepartments();
    }
    @ApiOperation("获取工号")
    @GetMapping("/maxWordID")
    public RespBean maxWordID(){
        return employeeService.maxWordID();
    }
    @ApiOperation("添加员工")
    @PostMapping ("/")
    public RespBean addEmp(@RequestBody Employee employee){
        return employeeService.addEmp(employee);
    }
    @ApiOperation("更新员工")
    @PutMapping("/")
    public RespBean updateEmp(@RequestBody Employee employee){
        if(employeeService.updateById(employee)){
            return RespBean.success("更新成功");
        }
        return RespBean.error("更新失败");
    }
    @ApiOperation("删除员工")
    @DeleteMapping("/")
    public RespBean deleteEmp(@RequestBody Integer id){
        if(employeeService.removeById(id)){
            return RespBean.success("删除成功");
        }
        return RespBean.error("删除失败");
    }
    @ApiOperation(value = "导出员工数据")
    @GetMapping(value = "/export",produces = "application/octet-stream")
    public void exportEmployee(HttpServletResponse response) throws IOException {
        List<Employee> list = employeeService.getEmployee(null);
        ExportParams params = new ExportParams("员工表","员工表", ExcelType.HSSF);
        Workbook sheets = ExcelExportUtil.exportExcel(params, Employee.class, list);
        ServletOutputStream outputStream = null;
        try {
            response.setHeader("content-type","application/octet-stream");
            response.setHeader("content-disposition","attachment;filename="+ URLEncoder.encode("员工表.xls","UTF-8"));
            outputStream = response.getOutputStream();
            sheets.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null!=outputStream){
                outputStream.close();
            }
        }
    }
    @ApiOperation("导入员工数据")
    @PostMapping("/import")
    public RespBean importEmployee(MultipartFile file) throws Exception {
        ImportParams params = new ImportParams();
        params.setTitleRows(1);
        List<Nation> nationList = nationService.list();
        List<PoliticsStatus> politicsStatusList = politicsStatusService.list();
        List<Department> departmentList = departmentService.list();
        List<Joblevel> joblevelList = joblevelService.list();
        List<Position> positionList = positionService.list();
        List<Employee> list = ExcelImportUtil.importExcel(file.getInputStream(),Employee.class,params);
        list.forEach(employee -> {
            employee.setNationId(nationList.get(nationList.indexOf(new Nation(employee.getNation().getName()))).getId());
            employee.setPoliticId(politicsStatusList.get(politicsStatusList.indexOf(new PoliticsStatus(employee.getPoliticsStatus().getName()))).getId());
            employee.setDepartmentId(departmentList.get(departmentList.indexOf(new Department(employee.getDepartment().getName()))).getId());
            employee.setJobLevelId(joblevelList.get(joblevelList.indexOf(new Joblevel(employee.getJoblevel().getName()))).getId());
            employee.setPosId(positionList.get(positionList.indexOf(new Position(employee.getPosition().getName()))).getId());

        });
        if (employeeService.saveBatch(list)){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }

}
