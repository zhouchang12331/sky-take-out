package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult historyOrder(Integer page, Integer pageSize, Integer status);

    OrderVO orderDetail(Long id);

    void repetition(Long id);

    void cancel(Long id);

    PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    void confirm(OrdersConfirmDTO dto);

    void delivery(Long id);

    void complete(Long id);

    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    void orderCancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    OrderStatisticsVO orderStatistics();

//    void reminder(Long id);
}
