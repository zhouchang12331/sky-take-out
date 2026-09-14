package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //异常情况的处理（收货地址为空、购物车为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        //查询当前用户的购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //构造订单数据
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());

        //向订单表插入1条数据
        orderMapper.insert(order);

        //订单明细数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }

        //向明细表插入n条数据
        orderDetailMapper.insertBatch(orderDetailList);

        //清理购物车中的数据
        shoppingCartMapper.deleteByUserId(userId);

        //封装返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
//        // 当前登录用户id
//        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getById(userId);
//
//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return new OrderPaymentVO();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询当前用户的订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 查询历史订单
     *
     * @param
     * @return
     */
    @Override
    public PageResult historyOrder(Integer pagenum, Integer pageSizeNum, Integer status) {
        log.info("查询历史订单：{}", pagenum);
        PageHelper.startPage(pagenum, pageSizeNum);
        OrdersPageQueryDTO ordersPageQueryDTO =new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        List<OrderVO> list=new ArrayList<>();
        List<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);

        if(page!=null && page.size()>0){
            for(Orders orders:page){
                List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(orders.getId());
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetail);
                list.add(orderVO);
            }
        }

        return new PageResult(page.size(), list);
    }

    @Override
    public OrderVO orderDetail(Long id) {
        //拿到orderId,根据orderId查询订单详情
        Orders orders=orderMapper.getById(id);

        List<OrderDetail> detail = orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(detail);
        return orderVO;
    }

    @Override
    public void repetition(Long id) {
        //1,查询当前用户id
        Long userId=BaseContext.getCurrentId();
        //2,根据用户id查询订单详情
        List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(id);
        //3,将订单详情对象转换为购物车对象，逐条加入购物车
        for (OrderDetail orderDetails : orderDetail) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetails, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public void cancel(Long id) {
        //1,根据id查询订单
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //2,只有待付款、待接单状态可以取消
        Integer status = orders.getStatus();
        if (!Orders.PENDING_PAYMENT.equals(status) && !Orders.TO_BE_CONFIRMED.equals(status)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //3,修改订单状态为已取消
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        //已支付的订单取消后标记为退款
        if (Orders.PAID.equals(orders.getPayStatus())) {
            orders.setPayStatus(Orders.REFUND);
        }
        orderMapper.update(orders);
    }

    @Override
    public PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索：{}", ordersPageQueryDTO);
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> pageVOList = getOrderVOList(page);

        return new PageResult(page.size(), pageVOList);
        //return new PageResult(page.getTotal(), pageVOList);
    }

    @Override
    public void confirm(OrdersConfirmDTO dto) {
        //根据id拿到订单id
        Orders orders = new OrderVO();
        orders.setId(dto.getId());
        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders=Orders.builder()
                .id(orderMapper.getById(id).getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        Orders orders=Orders.builder()
                .id(orderMapper.getById(id).getId())
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if (orders == null||orders.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        orders.setId(ordersRejectionDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orderMapper.update(orders);

    }

    @Override
    public void orderCancel(OrdersCancelDTO ordersCancelDTO) throws Exception{
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if(orders==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orderMapper.update(orders);
    }

    private List<OrderVO> getOrderVOList(List<Orders> page) {
        List<OrderVO> orderVOs=new ArrayList<>();
        for(Orders orders:page){
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            String orderDishes=getOrderDishes(orders);
            orderVO.setOrderDishes(orderDishes);
            orderVOs.add(orderVO);
        }
        return orderVOs;
    }

    private String getOrderDishes(Orders orders) {
        List<OrderDetail> dishes=orderDetailMapper.getByOrderId(orders.getId());
        if (dishes != null && dishes.size() > 0) {
            StringBuilder orderDishes = new StringBuilder();
            for (OrderDetail dish : dishes) {
                orderDishes.append(dish.getName()).append(" x").append(dish.getNumber()).append(" ");
            }
            return orderDishes.toString();
        }
        return "";
    }


//    @Override
//    public void reminder(Long id) {
//
//    }


}
