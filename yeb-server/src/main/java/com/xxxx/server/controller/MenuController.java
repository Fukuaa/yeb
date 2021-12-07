package com.xxxx.server.controller;


import com.xxxx.server.pojo.Menu;
import com.xxxx.server.service.IAdminService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/system/cfg")
public class MenuController {
    @Autowired
    private IAdminService adminService;
    @ApiOperation(value = "通过用户id查询")
    @GetMapping("/menu")
    public List<Menu> getMenusByAdminId(){
        return adminService.getMenusByAdminId();
    }
}
