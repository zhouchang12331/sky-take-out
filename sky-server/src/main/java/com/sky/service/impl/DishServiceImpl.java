package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Transactional
    public void saveWishFlavor(DishDTO dishDTO) {
        //保存菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
        //获取菜品的主键值
        Long dishId = dish.getId();
        //保存菜品口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //三步：1.开始分页，2.查询数据，3.封装分页结果并返回
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Dish dish = dishMapper.selectById(id);
            //判断当前菜品是否在售
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //抛出异常
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });

        //判断当前菜品是否关联了套餐
        ids.forEach(id -> {
            Integer count = setmealDishMapper.countByDishid(ids);
            if (count > 0) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        });
        //删除菜品数据
        dishMapper.deleteBatch(ids);
        //删除菜品口味 数据
        dishFlavorMapper.deleteBatch(ids);
    }

    @Override
    public DishVO getById(Long id) {
        DishVO dishVO=new DishVO();
        //1.返回菜品基本信息
        Dish dish=dishMapper.selectById(id);
        BeanUtils.copyProperties(dish,dishVO);
        //2.返回菜品口味信息
        List<DishFlavor> dishFlavors=dishFlavorMapper.selectByDishId(id);
        dishVO.setFlavors(dishFlavors);
        //3.返回dishVo
        return dishVO;
    }

    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //1,修改菜品基本信息
        dishMapper.update(dish);
        //2,修改菜品口味信息
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if(flavors!=null&&!flavors.isEmpty()){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
    }
}
