package com.sky.mapper;

import java.util.List;


public interface SetmealDishMapper {
    /**
     * 根据套餐id查询菜品
     * @param setmealId
     * @return
     */

    public Integer countByDishid(List<Long> dishIds);
}
