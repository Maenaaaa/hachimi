package com.campus.exchange.service;

import com.campus.exchange.vo.*;
import java.util.List;

public interface AdminService {
    DashboardVO getDashboard();
    List<AdminUserVO> listUsers(String keyword, int page, int size);
    AdminUserVO getUserDetail(Long userId);
    void disableUser(Long userId);
    void enableUser(Long userId);
    void approveGoods(Long goodsId);
    void rejectGoods(Long goodsId, String reason);
    void takeDownGoods(Long goodsId, String reason);
    void deleteGoods(Long goodsId);
    List<AdminGoodsVO> listGoods(String status, String keyword, int page, int size);
    void approveVerification(Long authId);
    void rejectVerification(Long authId, String reason);
    List<AdminUserVO> listVerifications(String status, int page, int size);
}
