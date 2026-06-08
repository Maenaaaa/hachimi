package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.CreateOrderDTO;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.service.CreditScoreService;
import com.campus.exchange.service.OrderService;
import com.campus.exchange.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderLogMapper orderLogMapper;
    private final GoodsMapper goodsMapper;
    private final GoodsImageMapper goodsImageMapper;
    private final UserMapper userMapper;
    private final CreditScoreService creditScoreService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OrderVO create(Long buyerId, CreateOrderDTO dto) {
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null || goods.getDeleted() == 1) throw new BusinessException("商品不存在");
        if (!"ACTIVE".equals(goods.getStatus())) throw new BusinessException("商品已下架或售出");
        if (goods.getUserId().equals(buyerId)) throw new BusinessException("不能购买自己的商品");

        Order order = new Order();
        order.setGoodsId(goods.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setStatus("PENDING");
        order.setAmount(goods.getPrice());
        order.setRemark(dto.getRemark());
        order.setMeetTime(dto.getMeetTime());
        order.setMeetPlace(dto.getMeetPlace());
        order.setExchangeGoodsId(dto.getExchangeGoodsId());
        orderMapper.insert(order);

        goods.setStatus("INACTIVE");
        goodsMapper.updateById(goods);

        if (dto.getExchangeGoodsId() != null) {
            Goods exGoods = goodsMapper.selectById(dto.getExchangeGoodsId());
            if (exGoods != null) { exGoods.setStatus("INACTIVE"); goodsMapper.updateById(exGoods); }
        }

        boolean isExchange = "EXCHANGE".equals(goods.getTradeType());
        addLog(order.getId(), "CREATE", buyerId, isExchange ? "买家发起置换" : "买家创建订单");
        notificationService.create(goods.getUserId(), "ORDER", "您有新的订单待确认",
                "买家已下单购买「" + goods.getTitle() + "」", order.getId());

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO confirm(Long orderId, Long sellerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getSellerId().equals(sellerId)) throw new BusinessException(403, "无权操作此订单");
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException("订单状态不正确");

        order.setStatus("IN_PROGRESS");
        orderMapper.updateById(order);

        addLog(order.getId(), "CONFIRM", sellerId, "卖家确认订单");
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        notificationService.create(order.getBuyerId(), "ORDER", "您的订单已被卖家确认",
                "卖家已确认订单「" + (goods != null ? goods.getTitle() : "") + "」", order.getId());

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO cancel(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))
            throw new BusinessException(403, "无权操作此订单");
        if (!"PENDING".equals(order.getStatus())) throw new BusinessException("订单已确认，无法取消");

        order.setStatus("CANCELLED");
        orderMapper.updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus("ACTIVE");
            goodsMapper.updateById(goods);
        }

        boolean isBuyer = order.getBuyerId().equals(userId);
        addLog(order.getId(), "CANCEL", userId, isBuyer ? "买家取消订单" : "卖家拒绝订单");
        Long notifyTarget = isBuyer ? order.getSellerId() : order.getBuyerId();
        String notifyMsg = isBuyer ? "买家已取消订单，商品已重新上架" : "卖家已拒绝订单，商品已重新上架";
        notificationService.create(notifyTarget, "ORDER", "订单已取消", notifyMsg, order.getId());

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO complete(Long orderId, Long buyerId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(buyerId)) throw new BusinessException(403, "无权操作此订单");
        if (!"IN_PROGRESS".equals(order.getStatus())) throw new BusinessException("订单状态不正确");

        order.setStatus("COMPLETED");
        orderMapper.updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus("SOLD");
            goodsMapper.updateById(goods);
        }

        addLog(order.getId(), "COMPLETE", buyerId, "买家确认完成");
        notificationService.create(order.getSellerId(), "ORDER", "订单已完成",
                "订单已完成，请及时评价买家", order.getId());
        creditScoreService.addScoreOnTradeComplete(order.getSellerId());

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO requestCancel(Long orderId, Long userId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))
            throw new BusinessException(403, "无权操作此订单");
        if (!"IN_PROGRESS".equals(order.getStatus())) throw new BusinessException("当前订单状态不允许申请取消");
        if (order.getCancelRequesterId() != null) throw new BusinessException("已存在待处理的取消申请");

        String role = order.getBuyerId().equals(userId) ? "买家" : "卖家";
        Long targetId = order.getBuyerId().equals(userId) ? order.getSellerId() : order.getBuyerId();

        order.setStatus("CANCEL_REQUESTED");
        order.setCancelReason(reason);
        order.setCancelRequesterId(userId);
        order.setCancelRequestTime(java.time.LocalDateTime.now());
        orderMapper.updateById(order);

        addLog(order.getId(), "CANCEL_REQUEST", userId, role + "申请取消：" + reason);
        notificationService.create(targetId, "ORDER", role + "申请取消交易",
                role + "申请取消订单，原因：" + reason, order.getId());

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO approveCancel(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))
            throw new BusinessException(403, "无权操作此订单");
        if (order.getCancelRequesterId() != null && order.getCancelRequesterId().equals(userId))
            throw new BusinessException("不能审批自己发起的取消申请");
        if (!"CANCEL_REQUESTED".equals(order.getStatus())) throw new BusinessException("当前订单状态不允许操作");

        String role = order.getBuyerId().equals(userId) ? "买家" : "卖家";
        String requesterRole = order.getBuyerId().equals(order.getCancelRequesterId()) ? "买家" : "卖家";

        order.setStatus("CANCELLED");
        orderMapper.updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus("ACTIVE");
            goodsMapper.updateById(goods);
        }

        addLog(order.getId(), "CANCEL_APPROVE", userId, role + "同意取消");
        notificationService.create(order.getCancelRequesterId(), "ORDER", "取消申请已通过",
                role + "已同意取消订单", order.getId());

        return toOrderVO(order);
    }

    @Override
    @Transactional
    public OrderVO rejectCancel(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))
            throw new BusinessException(403, "无权操作此订单");
        if (order.getCancelRequesterId() != null && order.getCancelRequesterId().equals(userId))
            throw new BusinessException("不能拒绝自己发起的取消申请");
        if (!"CANCEL_REQUESTED".equals(order.getStatus())) throw new BusinessException("当前订单状态不允许操作");

        String role = order.getBuyerId().equals(userId) ? "买家" : "卖家";
        Long requesterId = order.getCancelRequesterId();

        order.setStatus("IN_PROGRESS");
        orderMapper.updateById(order);

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getCancelReason, "")
                .set(Order::getCancelRequesterId, null)
                .set(Order::getCancelRequestTime, null));

        addLog(order.getId(), "CANCEL_REJECT", userId, role + "拒绝取消");
        if (requesterId != null) {
            notificationService.create(requesterId, "ORDER", "取消申请被拒绝",
                    role + "拒绝了取消订单", order.getId());
        }

        return toOrderVO(order);
    }

    @Override
    public List<OrderVO> getBuyerOrders(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, userId).eq(Order::getDeleted, 0);
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> p = new Page<>(page, size);
        return orderMapper.selectPage(p, wrapper).getRecords().stream().map(this::toOrderVO).toList();
    }

    @Override
    public List<OrderVO> getSellerOrders(Long userId, String status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, userId).eq(Order::getDeleted, 0);
        if (status != null && !status.isEmpty()) wrapper.eq(Order::getStatus, status);
        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> p = new Page<>(page, size);
        return orderMapper.selectPage(p, wrapper).getRecords().stream().map(this::toOrderVO).toList();
    }

    @Override
    public OrderDetailVO getDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        User currentUser = userMapper.selectById(userId);
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());
        if (!isAdmin && !order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId))
            throw new BusinessException(403, "无权查看此订单");

        OrderDetailVO vo = new OrderDetailVO();
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        User buyer = userMapper.selectById(order.getBuyerId());
        User seller = userMapper.selectById(order.getSellerId());

        vo.setId(order.getId());
        vo.setGoodsId(order.getGoodsId());
        vo.setGoodsTitle(goods != null ? goods.getTitle() : "");
        vo.setGoodsCoverImage(getFirstImage(order.getGoodsId()));
        vo.setGoodsTradeType(goods != null ? goods.getTradeType() : "SELL");
        vo.setExchangeGoodsId(order.getExchangeGoodsId());
        if (order.getExchangeGoodsId() != null) {
            Goods exG = goodsMapper.selectById(order.getExchangeGoodsId());
            vo.setExchangeGoodsTitle(exG != null ? exG.getTitle() : "");
            vo.setExchangeGoodsCoverImage(getFirstImage(order.getExchangeGoodsId()));
        }
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setMeetTime(order.getMeetTime());
        vo.setMeetPlace(order.getMeetPlace());
        vo.setBuyerId(order.getBuyerId());
        vo.setBuyerNickname(buyer != null ? buyer.getNickname() : "");
        vo.setBuyerAvatar(buyer != null ? buyer.getAvatar() : "");
        vo.setSellerId(order.getSellerId());
        vo.setSellerNickname(seller != null ? seller.getNickname() : "");
        vo.setSellerAvatar(seller != null ? seller.getAvatar() : "");
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        if (goods != null) vo.setGoodsDescription(goods.getDescription());

        vo.setCancelReason(order.getCancelReason());
        vo.setCancelRequesterId(order.getCancelRequesterId());
        vo.setCancelRequestTime(order.getCancelRequestTime());
        if (order.getCancelRequesterId() != null) {
            User requester = userMapper.selectById(order.getCancelRequesterId());
            vo.setCancelRequesterName(requester != null ? requester.getNickname() : "");
        }

        List<OrderLog> logs = orderLogMapper.selectList(
                new LambdaQueryWrapper<OrderLog>().eq(OrderLog::getOrderId, orderId).orderByAsc(OrderLog::getCreateTime));
        List<OrderLogVO> logVOs = new ArrayList<>();
        for (OrderLog log : logs) {
            User op = userMapper.selectById(log.getOperatorId());
            OrderLogVO lvo = new OrderLogVO();
            lvo.setId(log.getId());
            lvo.setAction(log.getAction());
            lvo.setOperatorId(log.getOperatorId());
            lvo.setOperatorName(op != null ? op.getNickname() : "");
            lvo.setRemark(log.getRemark());
            lvo.setCreateTime(log.getCreateTime());
            logVOs.add(lvo);
        }
        vo.setLogs(logVOs);

        return vo;
    }

    private OrderVO toOrderVO(Order order) {
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        User buyer = userMapper.selectById(order.getBuyerId());
        User seller = userMapper.selectById(order.getSellerId());

        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setGoodsId(order.getGoodsId());
        vo.setGoodsTitle(goods != null ? goods.getTitle() : "");
        vo.setGoodsCoverImage(getFirstImage(order.getGoodsId()));
        vo.setGoodsTradeType(goods != null ? goods.getTradeType() : "SELL");
        vo.setExchangeGoodsId(order.getExchangeGoodsId());
        if (order.getExchangeGoodsId() != null) {
            Goods exG = goodsMapper.selectById(order.getExchangeGoodsId());
            vo.setExchangeGoodsTitle(exG != null ? exG.getTitle() : "");
            vo.setExchangeGoodsCoverImage(getFirstImage(order.getExchangeGoodsId()));
        }
        vo.setAmount(order.getAmount());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setMeetTime(order.getMeetTime());
        vo.setMeetPlace(order.getMeetPlace());
        vo.setBuyerId(order.getBuyerId());
        vo.setBuyerNickname(buyer != null ? buyer.getNickname() : "");
        vo.setBuyerAvatar(buyer != null ? buyer.getAvatar() : "");
        vo.setSellerId(order.getSellerId());
        vo.setSellerNickname(seller != null ? seller.getNickname() : "");
        vo.setSellerAvatar(seller != null ? seller.getAvatar() : "");
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        return vo;
    }

    private void addLog(Long orderId, String action, Long operatorId, String remark) {
        OrderLog log = new OrderLog();
        log.setOrderId(orderId);
        log.setAction(action);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        orderLogMapper.insert(log);
    }

    private String getFirstImage(Long goodsId) {
        GoodsImage img = goodsImageMapper.selectOne(
                new LambdaQueryWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goodsId)
                        .orderByAsc(GoodsImage::getSortOrder).last("LIMIT 1"));
        return img != null ? img.getImageUrl() : "";
    }

    private String getGoodsTitle(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        return goods != null ? goods.getTitle() : "";
    }
}
