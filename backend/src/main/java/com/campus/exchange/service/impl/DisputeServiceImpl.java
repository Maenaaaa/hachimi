package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.ai.AiJudgmentType;
import com.campus.exchange.ai.dto.DisputeContext;
import com.campus.exchange.ai.prompt.PromptBuilder;
import com.campus.exchange.common.PageResult;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.AiJudgeService;
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
    private final GoodsImageMapper goodsImageMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final CreditScoreService creditScoreService;
    private final AiJudgeService aiJudgeService;
    private final AiConfigMapper configMapper;
    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;

    @Override
    @Transactional
    public DisputeVO create(Long orderId, Long applicantId, String reason, List<Long> selectedChatMessageIds) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        if (!order.getBuyerId().equals(applicantId) && !order.getSellerId().equals(applicantId))
            throw new BusinessException(403, "无权操作此订单");
        if (!"CANCEL_REQUESTED".equals(order.getStatus()) && !"IN_PROGRESS".equals(order.getStatus()))
            throw new BusinessException("当前订单状态不允许申请仲裁");

        // 检查每个订单只能申请一次仲裁
        Long existingDisputeCount = disputeMapper.selectCount(
                new LambdaQueryWrapper<OrderDispute>()
                        .eq(OrderDispute::getOrderId, orderId)
                        .eq(OrderDispute::getDeleted, 0));
        if (existingDisputeCount > 0) {
            throw new BusinessException("该订单已申请过仲裁，无法再次申请");
        }

        order.setStatus("DISPUTED");
        orderMapper.updateById(order);

        OrderDispute dispute = new OrderDispute();
        dispute.setOrderId(orderId);
        dispute.setApplicantId(applicantId);
        dispute.setReason(reason);
        dispute.setStatus("PENDING");
        if (selectedChatMessageIds != null && !selectedChatMessageIds.isEmpty()) {
            dispute.setSelectedChatMessageIds(selectedChatMessageIds.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(""));
        }
        disputeMapper.insert(dispute);

        Long targetUserId = order.getBuyerId().equals(applicantId) ? order.getSellerId() : order.getBuyerId();
        User applicant = userMapper.selectById(applicantId);
        String applicantName = applicant != null ? applicant.getNickname() : "用户";

        notificationService.create(targetUserId, "DISPUTE", "对方申请了仲裁",
                applicantName + " 申请管理员介入仲裁，原因：" + reason, orderId);

        triggerAiArbitration(dispute, order);

        return toVO(dispute);
    }

    private void triggerAiArbitration(OrderDispute dispute, Order order) {
        try {
            AiConfig config = configMapper.selectOne(
                    new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getConfigKey, "dispute_auto_arbitrate"));
            if (config != null && "false".equals(config.getConfigValue())) {
                return;
            }

            User applicant = userMapper.selectById(dispute.getApplicantId());
            User respondent = userMapper.selectById(
                    order.getBuyerId().equals(dispute.getApplicantId()) ? order.getSellerId() : order.getBuyerId());

            // 获取商品详情
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            String goodsInfo = "";
            String goodsImageUrls = "";
            if (goods != null) {
                goodsInfo = "商品标题: " + goods.getTitle() + "\n" +
                        "商品描述: " + (goods.getDescription() != null ? goods.getDescription() : "无") + "\n" +
                        "转让价格: " + goods.getPrice() + " 元\n" +
                        "原价: " + goods.getOriginalPrice() + " 元\n" +
                        "成色: " + goods.getCondition() + "\n" +
                        "交易类型: " + goods.getTradeType();

                // 获取商品图片
                List<GoodsImage> goodsImages = goodsImageMapper.selectList(
                        new LambdaQueryWrapper<GoodsImage>()
                                .eq(GoodsImage::getGoodsId, goods.getId())
                                .orderByAsc(GoodsImage::getSortOrder));
                if (!goodsImages.isEmpty()) {
                    goodsImageUrls = goodsImages.stream()
                            .map(GoodsImage::getImageUrl)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                }
            }

            String orderInfo = "订单金额: " + order.getAmount() + " 元\n订单状态: " + order.getStatus() + "\n" + goodsInfo;

            // 获取聊天记录
            String chatSummary = "聊天记录摘要暂无";
            if (dispute.getSelectedChatMessageIds() != null && !dispute.getSelectedChatMessageIds().isEmpty()) {
                List<Long> messageIds = java.util.Arrays.stream(dispute.getSelectedChatMessageIds().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .toList();
                if (!messageIds.isEmpty()) {
                    List<Message> selectedMessages = messageMapper.selectBatchIds(messageIds);
                    if (!selectedMessages.isEmpty()) {
                        StringBuilder chatBuilder = new StringBuilder();
                        for (Message msg : selectedMessages) {
                            User sender = userMapper.selectById(msg.getSenderId());
                            String senderName = sender != null ? sender.getNickname() : "用户";
                            chatBuilder.append(senderName).append(": ").append(msg.getContent()).append("\n");
                        }
                        chatSummary = chatBuilder.toString();
                    }
                }
            } else {
                // 如果没有选择聊天记录，尝试获取与该订单相关的聊天记录
                // 通过conversation表找到与该商品相关的会话
                Conversation conversation = conversationMapper.selectOne(
                        new LambdaQueryWrapper<Conversation>()
                                .eq(Conversation::getGoodsId, order.getGoodsId())
                                .eq(Conversation::getBuyerId, order.getBuyerId())
                                .eq(Conversation::getSellerId, order.getSellerId()));
                if (conversation != null) {
                    List<Message> messages = messageMapper.selectList(
                            new LambdaQueryWrapper<Message>()
                                    .eq(Message::getConversationId, conversation.getId())
                                    .orderByAsc(Message::getCreateTime)
                                    .last("LIMIT 20"));
                    if (!messages.isEmpty()) {
                        StringBuilder chatBuilder = new StringBuilder();
                        for (Message msg : messages) {
                            User sender = userMapper.selectById(msg.getSenderId());
                            String senderName = sender != null ? sender.getNickname() : "用户";
                            chatBuilder.append(senderName).append(": ").append(msg.getContent()).append("\n");
                        }
                        chatSummary = chatBuilder.toString();
                    }
                }
            }

            double applicantCredit = applicant != null && applicant.getCreditScore() != null ? applicant.getCreditScore().doubleValue() : 5.0;
            double respondentCredit = respondent != null && respondent.getCreditScore() != null ? respondent.getCreditScore().doubleValue() : 5.0;

            String prompt = PromptBuilder.buildDisputePrompt(
                    dispute.getReason(),
                    orderInfo,
                    goodsImageUrls,
                    chatSummary,
                    applicantCredit,
                    respondentCredit
            );

            // 如果有商品图片，使用带图片的AI判决
            if (!goodsImageUrls.isEmpty()) {
                List<String> imageUrls = java.util.Arrays.stream(goodsImageUrls.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
                aiJudgeService.judgeAsyncWithImages(AiJudgmentType.DISPUTE, dispute.getId(), prompt, imageUrls);
            } else {
                aiJudgeService.judgeAsync(AiJudgmentType.DISPUTE, dispute.getId(), prompt);
            }

            dispute.setAiAutoHandled(true);
            disputeMapper.updateById(dispute);
        } catch (Exception e) {
            // AI 仲裁失败不影响争议创建
        }
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

            notificationService.create(order.getBuyerId(), "DISPUTE", "仲裁结果：取消交易",
                    "管理员已判定取消订单，" + handleNote, dispute.getOrderId());
            notificationService.create(order.getSellerId(), "DISPUTE", "仲裁结果：取消交易",
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

            notificationService.create(order.getBuyerId(), "DISPUTE", "仲裁结果：恢复交易",
                    "管理员已驳回取消申请，交易继续", dispute.getOrderId());
            notificationService.create(order.getSellerId(), "DISPUTE", "仲裁结果：恢复交易",
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
    public PageResult<DisputeVO> list(String status, int page, int size) {
        LambdaQueryWrapper<OrderDispute> wrapper = new LambdaQueryWrapper<OrderDispute>()
                .eq(OrderDispute::getDeleted, 0);
        if (status != null && !status.isEmpty()) wrapper.eq(OrderDispute::getStatus, status);
        wrapper.orderByDesc(OrderDispute::getCreateTime);

        Page<OrderDispute> p = new Page<>(page, size);
        disputeMapper.selectPage(p, wrapper);
        List<DisputeVO> records = p.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(records, p.getTotal(), page, size);
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
        vo.setSelectedChatMessageIds(dispute.getSelectedChatMessageIds());
        return vo;
    }
}
