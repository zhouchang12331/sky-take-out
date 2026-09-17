package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单数据
     * @param order
     */
    void insert(Orders order);

    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);


    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    @Select("select * from orders where status=#{status} and order_time < #{orderTime}")
    List<Orders> updateOrderStatus(@Param("status") Integer status, @Param("orderTime") LocalDateTime orderTime);

    /**
     * 根据动态条件统计订单金额（营业额）
     * @param map
     * @return
     */
    @Select("select sum(amount) from orders where status=#{status} and order_time >= #{begin} and order_time < #{end}")
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
}
