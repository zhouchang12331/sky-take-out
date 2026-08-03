package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "管理端-菜品管理")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品，参数：{}",dishDTO);
        dishService.saveWishFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("分页查询，参数：{}",dishPageQueryDTO);
        PageResult pageResult =dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("批量删除菜品")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品，ids：{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
      *回显菜品数据
     **/
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        log.info("回显菜品数据，id：{}",id);
        DishVO dishVo=dishService.getById(id);
        return Result.success(dishVo);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品，参数：{}",dishDTO);
        dishService.update(dishDTO);
        return Result.success();
    }
}
