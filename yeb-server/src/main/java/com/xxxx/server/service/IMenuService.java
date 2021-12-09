package com.xxxx.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.server.pojo.Menu;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-05
 */
public interface IMenuService extends IService<Menu> {

    List<Menu> getMenusByAdminId();
    List<Menu> getMenusWithRole();

    List<Menu> getAllMenus();
}
