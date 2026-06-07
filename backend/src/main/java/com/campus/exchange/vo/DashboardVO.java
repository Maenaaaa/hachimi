package com.campus.exchange.vo;

import lombok.Data;

@Data
public class DashboardVO {
    private Long totalUsers;
    private Long totalGoods;
    private Long totalOrders;
    private Long todayActiveUsers;
    private Long pendingReviewGoods;
    private Long pendingReports;
    private Long todayNewUsers;
    private Long todayNewOrders;
}
