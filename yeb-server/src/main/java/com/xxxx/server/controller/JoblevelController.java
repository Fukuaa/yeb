package com.xxxx.server.controller;


import com.xxxx.server.pojo.Joblevel;
import com.xxxx.server.pojo.RespBean;
import com.xxxx.server.service.IJoblevelService;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
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
@RequestMapping("/system/basic/joblevel")
public class JoblevelController {
    @Autowired
    private IJoblevelService joblevelService;
    @ApiOperation("获取所有职称")
    @GetMapping("/")
    public List<Joblevel> getAllJobLevels(){
        return joblevelService.list();
    }
    @ApiOperation("添加职称")
    @PostMapping("/")
    public RespBean addJobLevels(@RequestBody Joblevel joblevel){
        joblevel.setCreateDate(LocalDateTime.now());
        if (joblevelService.save(joblevel)){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }
    @ApiOperation("更新职称")
    @PutMapping("/")
    public RespBean updateJobLevels(@RequestBody Joblevel joblevel){
        if (joblevelService.updateById(joblevel)){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }
    @ApiOperation("删除职称")
    @DeleteMapping("/{id}")
    public RespBean deleteJobLevels(@PathVariable Integer id){
        if (joblevelService.removeById(id)){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }
    @ApiOperation("批量删除职称")
    @DeleteMapping("/")
    public RespBean deleteJobLevelsByIds(Integer[] ids){
        System.out.println(ids);
        if (joblevelService.removeByIds(Arrays.asList(ids))){
            return RespBean.success("成功");
        }
        return RespBean.error("失败");
    }
}
