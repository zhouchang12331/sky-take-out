package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     *
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processTimeoutOrders() {
        log.info("处理超时订单...");
        LocalDateTime time=LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList= orderMapper.updateOrderStatus(Orders.PENDING_PAYMENT,time);
        if(ordersList!=null&&ordersList.size()>0){
            for(Orders order:ordersList){
                order.setStatus(Orders.CANCELLED);
                order.setCancelTime(LocalDateTime.now());
                order.setCancelReason("订单超时未支付，系统自动取消");
                orderMapper.update(order);
            }
        }
    }

    /**
     * 处理未结束订单,每天凌晨一点
     *
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void processUnfinishedOrders() {
        log.info("处理未结束订单...");
        orderMapper.updateOrderStatus(Orders.CANCELLED,LocalDateTime.now().minusMinutes(60));
        List<Orders> ordersList= orderMapper.updateOrderStatus(Orders.DELIVERY_IN_PROGRESS,LocalDateTime.now().minusMinutes(60));
        if(ordersList!=null&&ordersList.size()>0) {
            for (Orders order : ordersList) {
                order.setStatus(Orders.COMPLETED);
                order.setCheckoutTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }
}
