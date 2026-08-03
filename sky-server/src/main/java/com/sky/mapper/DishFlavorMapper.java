package com.sky.mapper;

import com.sky.entity.DishFlavor;

import java.util.List;

public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);

    void deleteBatch(List<Long> ids);

    List<DishFlavor> selectByDishId(Long id);

    void deleteByDishId(Long dishId);
}
