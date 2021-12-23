package com.xxxx.server.controller;


import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.pojo.Role;
import com.xxxx.server.service.IAdminService;
import com.xxxx.server.service.IRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/system/admin")
public class AdminController {
    @Autowired
    private IAdminService adminService;
    @ApiOperation("获取所有操作员")
    @GetMapping("/")
    public List<Admin> getAllAdmin(String keywords){
        return adminService.getAllAdmin(keywords);
    }
    @ApiOperation("更新操作员")
    @PutMapping("/")
    public RespBean updateAdmin(@RequestBody Admin admin){
        if(adminService.updateById(admin)){
            return RespBean.success("更新成功");
        }
        return RespBean.error("更新失败");
    }
    @ApiOperation("删除操作员")
    @DeleteMapping("/{id}")
    public RespBean deleteAdmin(@PathVariable Integer id){
        if(adminService.removeById(id)){
            return RespBean.success("删除成功");
        }
        return RespBean.error("删除失败");
    }
    @Autowired
    private IRoleService roleService;
    @ApiOperation(value = "获取角色")
    @GetMapping("/roles")
    public List<Role> getAllRoles(){
        return roleService.list();
    }
    @ApiOperation(value = "更新操作员角色")
    @PutMapping("/role")
    public RespBean updateAdminRole(Integer adminId,Integer[] rids){
        return adminService.updateAdminRole(adminId,rids);

    }
}
