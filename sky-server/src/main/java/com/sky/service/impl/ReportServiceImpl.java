package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;


    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //1,日期表,dateList
        List<LocalDate> dateList = dateList(begin, end);

        //2,营业额表,turnoverList：逐天统计已完成订单的金额之和
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            //当天 [00:00:00, 次日00:00:00)，配合 SQL 的 >= 和 <
            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", date.plusDays(1).atStartOfDay());
            Double turnover = orderMapper.sumByMap(map);
            //当天没有已完成的订单时，求和结果为null，按0处理
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }

        //3,封装返回数据，两个列表都以逗号分隔的字符串返回
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //1,日期表,dateList
        List<LocalDate> dateList = dateList(begin, end);


        //2,新增用户表
        // 3,总用户表
        List<Integer> newUserList=new ArrayList<>();
        List<Integer> totalUserList=new ArrayList<>();


        for (LocalDate date : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            Map<String, Object> map = new HashMap<>();

            map.put("endTime", endTime);
            Integer totalUserCount = userMapper.countByMap(map);
            totalUserList.add(totalUserCount);
            map.put("beginTime", beginTime);
            Integer newUserCount = userMapper.countByMap(map);
            newUserList.add(newUserCount);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        log.info("订单统计");
        //1,日期表,dateList
        List<LocalDate> dateList = dateList(begin, end);

        //2,订单数列表,orderCountList
        List<Integer> orderCountList = new ArrayList<>();
        for(LocalDate date : dateList){
            Map<String, Object> map = new HashMap<>();
            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", date.plusDays(1).atStartOfDay());
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount==null?0:orderCount);
        }

        //3,有效订单数列表,validOrderCountList（已完成状态）
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Map<String, Object> map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("begin", LocalDateTime.of(date, LocalTime.MIN));
            map.put("end", date.plusDays(1).atStartOfDay());
            Integer validOrderCountThisDate = orderMapper.validCountByMap(map);
            validOrderCountList.add(validOrderCountThisDate==null?0:validOrderCountThisDate);
        }

        //4,订单总数、有效订单数

        Integer totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();

        //5,订单完成率（比率，订单总数为0时按0处理，避免除零得到NaN）
        Double orderCompletionRate = totalOrderCount == 0 ? 0.0
                : validOrderCount.doubleValue() / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        log.info("销售top10");

        Map<String, Object> map = new HashMap<>();
        map.put("status", Orders.COMPLETED);
        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", end.plusDays(1).atStartOfDay());
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.top10(map);
        log.info("goodsSalesDTOList={}", goodsSalesDTOList);
        //状态为已完成的订单
        //1,商品名称列表
        //查询order_detail表
        List<String> nameList = new ArrayList<>();
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            nameList.add(goodsSalesDTO.getName());
        }
        //2,商品销售量列表
        List<Integer> numberList = new ArrayList<>();

        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            numberList.add(goodsSalesDTO.getNumber());
        }

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
    }

    //日期列表函数
    public List<LocalDate> dateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }

}
