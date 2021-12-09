package com.xxxx.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxxx.server.pojo.Admin;
import com.xxxx.server.pojo.Menu;
import org.apache.ibatis.annotations.Param;

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

    List<Admin> getAllAdmin(@Param("id") Integer id,@Param("keywords") String keywords);
}
