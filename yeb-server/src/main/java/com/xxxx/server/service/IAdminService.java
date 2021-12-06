package com.xxxx.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.RespBean;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-05
 */
public interface IAdminService extends IService<Admin> {

    RespBean login(String username, String password, HttpServletRequest request);

    Admin getAdminByUserName(String username);
}
