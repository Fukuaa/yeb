package com.xxxx.server.service;

import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.Menu;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.pojo.Role;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-05
 */
public interface IAdminService extends IService<Admin> {

    RespBean login(String username, String password, String code, HttpServletRequest request);

    Admin getAdminByUserName(String username);
    List<Role> getRoles(Integer adminId);

    List<Admin> getAllAdmin(String keywords);

    RespBean updateAdminRole(Integer adminId, Integer[] rdis);

    RespBean updateAdminPassword(String oldPass, String pass, Integer adminId);
}
