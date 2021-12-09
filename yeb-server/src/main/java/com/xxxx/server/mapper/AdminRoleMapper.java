package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.server.pojo.AdminRole;
import com.xxxx.server.pojo.RespBean;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zouqingfeng
 * @since 2021-12-05
 */
public interface AdminRoleMapper extends BaseMapper<AdminRole> {

    Integer updateAdminRole(@Param("adminId") Integer adminId,@Param("rids") Integer[] rdis);
}
