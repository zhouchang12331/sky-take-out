package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    //void insert(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void update(ShoppingCart cart);

    @Insert("insert into shopping_cart (user_id, dish_id, setmeal_id, name, image, amount, number, create_time) " +
            "values (#{userId}, #{dishId}, #{setmealId}, #{name}, #{image}, #{amount}, #{number}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where id=#{id}")
    void delete(Long id);

    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);


    void insertBatch(List<ShoppingCart> shoppingCartList);
}
