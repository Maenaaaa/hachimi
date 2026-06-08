package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.CreditScoreService;
import com.campus.exchange.service.DisputeService;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.vo.DisputeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final OrderDisputeMapper disputeMapper;
    private final OrderMapper orderMapper;
    private final OrderLogMapper orderLogMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final CreditScoreService creditScoreService;

    @Override
    @Transactional
    public DisputeVO create(Long orderId, Long applicantId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(applicantId) && !order.getSellerId().equals(applicantId))
            throw new BusinessException(403, "无权操作此订单");
        if (!"CANCEL_REQUESTED".equals(order.getStatus()) && !"IN_PROGRESS".equals(order.getStatus()))
            throw new BusinessException("当前订单状态不允许申请仲裁");

        order.setStatus("DISPUTED");
        orderMapper.updateById(order);

        OrderDispute dispute = new OrderDispute();
        dispute.setOrderId(orderId);
        dispute.setApplicantId(applicantId);
        dispute.setReason(reason);
        dispute.setStatus("PENDING");
        disputeMapper.insert(dispute);

        Long targetUserId = order.getBuyerId().equals(applicantId) ? order.getSellerId() : order.getBuyerId();
        User applicant = userMapper.selectById(applicantId);
        String applicantName = applicant != null ? applicant.getNickname() : "用户";

        notificationService.create(targetUserId, "ORDER", "对方申请了仲裁",
                applicantName + " 申请管理员介入仲裁，原因：" + reason, orderId);

        return toVO(dispute);
    }

    @Override
    @Transactional
    public DisputeVO handle(Long disputeId, Long handlerId, String status, String handleNote) {
        OrderDispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) throw new BusinessException("争议不存在");

        dispute.setStatus(status);
        dispute.setHandlerId(handlerId);
        dispute.setHandleNote(handleNote);
        dispute.setHandleTime(LocalDateTime.now());
        disputeMapper.updateById(dispute);

        Order order = orderMapper.selectById(dispute.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");

        if ("RESOLVED".equals(status)) {
            order.setStatus("CANCELLED");
            orderMapper.updateById(order);

            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null) {
                goods.setStatus("ACTIVE");
                goodsMapper.updateById(goods);
            }

            notificationService.create(order.getBuyerId(), "ORDER", "仲裁结果：取消交易",
                    "管理员已判定取消订单，" + handleNote, dispute.getOrderId());
            notificationService.create(order.getSellerId(), "ORDER", "仲裁结果：取消交易",
                    "管理员已判定取消订单，" + handleNote, dispute.getOrderId());

            OrderLog log = new OrderLog();
            log.setOrderId(order.getId());
            log.setAction("DISPUTE_RESOLVED");
            log.setOperatorId(handlerId);
            log.setRemark("管理员仲裁判取消：" + handleNote);
            orderLogMapper.insert(log);
        } else if ("REJECTED".equals(status)) {
            order.setStatus("IN_PROGRESS");
            orderMapper.updateById(order);

            notificationService.create(order.getBuyerId(), "ORDER", "仲裁结果：恢复交易",
                    "管理员已驳回取消申请，交易继续", dispute.getOrderId());
            notificationService.create(order.getSellerId(), "ORDER", "仲裁结果：恢复交易",
                    "管理员已驳回取消申请，交易继续", dispute.getOrderId());

            OrderLog log = new OrderLog();
            log.setOrderId(order.getId());
            log.setAction("DISPUTE_REJECTED");
            log.setOperatorId(handlerId);
            log.setRemark("管理员仲裁驳回：" + handleNote);
            orderLogMapper.insert(log);
        }

        return toVO(dispute);
    }

    @Override
    public List<DisputeVO> list(String status, int page, int size) {
        LambdaQueryWrapper<OrderDispute> wrapper = new LambdaQueryWrapper<OrderDispute>()
                .eq(OrderDispute::getDeleted, 0);
        if (status != null && !status.isEmpty()) wrapper.eq(OrderDispute::getStatus, status);
        wrapper.orderByDesc(OrderDispute::getCreateTime);

        Page<OrderDispute> p = new Page<>(page, size);
        return disputeMapper.selectPage(p, wrapper).getRecords().stream().map(this::toVO).toList();
    }

    @Override
    public DisputeVO getById(Long disputeId) {
        OrderDispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) throw new BusinessException("争议不存在");
        return toVO(dispute);
    }

    private DisputeVO toVO(OrderDispute dispute) {
        User applicant = userMapper.selectById(dispute.getApplicantId());
        User handler = dispute.getHandlerId() != null ? userMapper.selectById(dispute.getHandlerId()) : null;
        Order order = orderMapper.selectById(dispute.getOrderId());
        Goods goods = order != null ? goodsMapper.selectById(order.getGoodsId()) : null;

        DisputeVO vo = new DisputeVO();
        vo.setId(dispute.getId());
        vo.setOrderId(dispute.getOrderId());
        vo.setGoodsTitle(goods != null ? goods.getTitle() : "");
        vo.setApplicantId(dispute.getApplicantId());
        vo.setApplicantNickname(applicant != null ? applicant.getNickname() : "");
        vo.setApplicantAvatar(applicant != null ? applicant.getAvatar() : "");
        vo.setReason(dispute.getReason());
        vo.setStatus(dispute.getStatus());
        vo.setHandlerId(dispute.getHandlerId());
        vo.setHandlerNickname(handler != null ? handler.getNickname() : "");
        vo.setHandleNote(dispute.getHandleNote());
        vo.setHandleTime(dispute.getHandleTime());
        vo.setCreateTime(dispute.getCreateTime());
        return vo;
    }
}
