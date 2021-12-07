package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.Menu;

import java.util.List;


/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-05
 */
public interface AdminMapper extends BaseMapper<Admin> {

    List<Menu> getMenusByAdminId(Integer id);
}
