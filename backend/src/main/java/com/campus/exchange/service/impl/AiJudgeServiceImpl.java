package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.exchange.ai.AiGateway;
import com.campus.exchange.ai.AiJudgment;
import com.campus.exchange.ai.AiJudgmentType;
import com.campus.exchange.ai.AiVerdict;
import com.campus.exchange.entity.AiConfig;
import com.campus.exchange.entity.AiJudgmentRecord;
import com.campus.exchange.entity.Goods;
import com.campus.exchange.entity.Order;
import com.campus.exchange.entity.OrderDispute;
import com.campus.exchange.entity.OrderLog;
import com.campus.exchange.entity.Report;
import com.campus.exchange.entity.User;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.AiConfigMapper;
import com.campus.exchange.mapper.AiJudgmentMapper;
import com.campus.exchange.mapper.GoodsMapper;
import com.campus.exchange.mapper.OrderDisputeMapper;
import com.campus.exchange.mapper.OrderLogMapper;
import com.campus.exchange.mapper.OrderMapper;
import com.campus.exchange.mapper.ReportMapper;
import com.campus.exchange.mapper.UserMapper;
import com.campus.exchange.service.AiJudgeService;
import com.campus.exchange.service.CreditScoreService;
import com.campus.exchange.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiJudgeServiceImpl implements AiJudgeService {

    private final AiGateway aiGateway;
    private final AiJudgmentMapper judgmentMapper;
    private final AiConfigMapper configMapper;
    private final ReportMapper reportMapper;
    private final OrderMapper orderMapper;
    private final OrderDisputeMapper disputeMapper;
    private final OrderLogMapper orderLogMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final CreditScoreService creditScoreService;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public AiJudgment judge(AiJudgmentType type, Long sourceId, String prompt) {
        double autoThreshold = getDoubleConfig("confidence_auto_threshold", 0.8);
        double escalateThreshold = getDoubleConfig("confidence_escalate_threshold", 0.5);

        AiJudgment judgment;
        try {
            judgment = aiGateway.analyze(type, prompt);
        } catch (Exception e) {
            log.error("AI 分析失败，转人工处理", e);
            judgment = AiJudgment.builder()
                    .verdict(AiVerdict.ESCALATED)
                    .confidence(0.0)
                    .reasoning("AI 服务不可用，转人工处理")
                    .evidence(List.of("AI 调用失败: " + e.getMessage()))
                    .build();
        }

        AiJudgmentRecord record = new AiJudgmentRecord();
        record.setSourceType(type.name());
        record.setSourceId(sourceId);
        record.setAiModel("mimo-v2.5-pro");
        record.setConfidence(judgment.getConfidence());
        record.setVerdict(judgment.getVerdict().name());
        record.setReasoning(judgment.getReasoning());
        try {
            record.setEvidence(objectMapper.writeValueAsString(judgment.getEvidence()));
        } catch (JsonProcessingException e) {
            record.setEvidence("[]");
        }
        record.setStatus("PENDING");

        if (judgment.getConfidence() >= autoThreshold && judgment.getVerdict() != AiVerdict.ESCALATED) {
            record.setAutoHandled(true);
            judgmentMapper.insert(record);
            executeVerdictInternal(record, type, sourceId, judgment);
        } else {
            record.setAutoHandled(false);
            record.setStatus("ESCALATED");
            judgmentMapper.insert(record);
            notifyAdminForReview(type, sourceId, record.getId(), judgment);
            notifyReporterOfEscalation(type, sourceId, record.getId(), judgment);
        }

        return judgment;
    }

    @Override
    @Async
    public void judgeAsync(AiJudgmentType type, Long sourceId, String prompt) {
        judge(type, sourceId, prompt);
    }

    @Override
    @Transactional
    public AiJudgment judgeWithImages(AiJudgmentType type, Long sourceId, String prompt, List<String> imageUrls) {
        double autoThreshold = getDoubleConfig("confidence_auto_threshold", 0.8);
        double escalateThreshold = getDoubleConfig("confidence_escalate_threshold", 0.5);

        AiJudgment judgment;
        try {
            judgment = aiGateway.analyzeWithImages(type, prompt, imageUrls);
        } catch (Exception e) {
            log.error("AI 分析失败，转人工处理", e);
            judgment = AiJudgment.builder()
                    .verdict(AiVerdict.ESCALATED)
                    .confidence(0.0)
                    .reasoning("AI 服务不可用，转人工处理")
                    .evidence(List.of("AI 调用失败: " + e.getMessage()))
                    .build();
        }

        AiJudgmentRecord record = new AiJudgmentRecord();
        record.setSourceType(type.name());
        record.setSourceId(sourceId);
        record.setAiModel("mimo-v2.5-pro");
        record.setConfidence(judgment.getConfidence());
        record.setVerdict(judgment.getVerdict().name());
        record.setReasoning(judgment.getReasoning());
        try {
            record.setEvidence(objectMapper.writeValueAsString(judgment.getEvidence()));
        } catch (JsonProcessingException e) {
            record.setEvidence("[]");
        }
        record.setStatus("PENDING");

        if (judgment.getConfidence() >= autoThreshold && judgment.getVerdict() != AiVerdict.ESCALATED) {
            record.setAutoHandled(true);
            judgmentMapper.insert(record);
            executeVerdictInternal(record, type, sourceId, judgment);
        } else {
            record.setAutoHandled(false);
            record.setStatus("ESCALATED");
            judgmentMapper.insert(record);
            notifyAdminForReview(type, sourceId, record.getId(), judgment);
            notifyReporterOfEscalation(type, sourceId, record.getId(), judgment);
        }

        return judgment;
    }

    @Override
    @Async
    public void judgeAsyncWithImages(AiJudgmentType type, Long sourceId, String prompt, List<String> imageUrls) {
        judgeWithImages(type, sourceId, prompt, imageUrls);
    }

    private void executeVerdictInternal(AiJudgmentRecord record, AiJudgmentType type, Long sourceId, AiJudgment judgment) {
        try {
            log.info("开始执行判决: type={}, sourceId={}, verdict={}", type, sourceId, judgment.getVerdict());
            switch (type) {
                case REPORT -> executeReportVerdict(sourceId, judgment);
                case DISPUTE -> executeDisputeVerdict(sourceId, judgment);
                case GOODS_REVIEW -> executeGoodsReviewVerdict(sourceId, judgment);
            }
            record.setStatus("PROCESSED");
            record.setHandleNote("AI 自动处理");
            log.info("判决执行成功: type={}, sourceId={}", type, sourceId);
        } catch (Exception e) {
            log.error("执行 AI 判决失败: type={}, sourceId={}", type, sourceId, e);
            record.setStatus("ESCALATED");
            record.setHandleNote("自动执行失败，转人工: " + e.getMessage());
        }
        record.setHandleTime(LocalDateTime.now());
        judgmentMapper.updateById(record);
    }

    @Override
    @Transactional
    public void executeVerdict(Long judgmentId) {
        AiJudgmentRecord record = judgmentMapper.selectById(judgmentId);
        if (record == null) throw new BusinessException("判决记录不存在");

        AiJudgmentType type = AiJudgmentType.valueOf(record.getSourceType());
        AiJudgment judgment = AiJudgment.builder()
                .verdict(AiVerdict.valueOf(record.getVerdict()))
                .confidence(record.getConfidence())
                .reasoning(record.getReasoning())
                .build();

        executeVerdictInternal(record, type, record.getSourceId(), judgment);
    }

    private void executeReportVerdict(Long reportId, AiJudgment judgment) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) return;

        if (judgment.getVerdict() == AiVerdict.APPROVED) {
            if ("GOODS".equals(report.getType())) {
                Goods goods = goodsMapper.selectById(report.getTargetId());
                if (goods != null) {
                    goods.setStatus("TAKEN_DOWN");
                    goodsMapper.updateById(goods);
                    notificationService.create(goods.getUserId(), "REVIEW", "您的商品已被下架",
                            "商品因被举报违规已被AI自动下架", goods.getId());
                    creditScoreService.deductScoreOnReportApproved(goods.getUserId());
                }
            } else if ("USER".equals(report.getType())) {
                User targetUser = userMapper.selectById(report.getTargetId());
                if (targetUser != null && !"ADMIN".equals(targetUser.getRole())) {
                    targetUser.setStatus("DISABLED");
                    userMapper.updateById(targetUser);
                    creditScoreService.deductScoreOnReportApproved(targetUser.getId());
                }
            }
            report.setStatus("APPROVED");
        } else if (judgment.getVerdict() == AiVerdict.REJECTED) {
            report.setStatus("REJECTED");
        }

        report.setAiAutoHandled(true);
        reportMapper.updateById(report);

        // 通知举报人，relatedId 存储被举报的商品/用户ID
        Long notifyRelatedId = report.getTargetId();
        notificationService.create(report.getReporterId(), "REVIEW", "举报处理结果",
                "AI 审核完成，结果: " + (judgment.getVerdict() == AiVerdict.APPROVED ? "举报成立" : "举报不成立"),
                notifyRelatedId);
    }

    private void executeDisputeVerdict(Long disputeId, AiJudgment judgment) {
        OrderDispute dispute = disputeMapper.selectById(disputeId);
        if (dispute == null) return;

        Order order = orderMapper.selectById(dispute.getOrderId());
        if (order == null) return;

        if (judgment.getVerdict() == AiVerdict.APPROVED) {
            // 取消订单
            order.setStatus("CANCELLED");
            orderMapper.updateById(order);

            // 恢复商品状态
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null) {
                goods.setStatus("ACTIVE");
                goodsMapper.updateById(goods);
            }

            // 通知双方
            notificationService.create(order.getBuyerId(), "DISPUTE", "仲裁结果：取消交易",
                    "AI仲裁已判定取消订单，" + judgment.getReasoning(), dispute.getOrderId());
            notificationService.create(order.getSellerId(), "DISPUTE", "仲裁结果：取消交易",
                    "AI仲裁已判定取消订单，" + judgment.getReasoning(), dispute.getOrderId());

            // 记录日志
            OrderLog log = new OrderLog();
            log.setOrderId(order.getId());
            log.setAction("DISPUTE_RESOLVED");
            log.setOperatorId(0L);
            log.setRemark("AI仲裁判取消：" + judgment.getReasoning());
            orderLogMapper.insert(log);

            dispute.setStatus("RESOLVED");
        } else if (judgment.getVerdict() == AiVerdict.REJECTED) {
            // 驳回仲裁，继续交易
            order.setStatus("IN_PROGRESS");
            orderMapper.updateById(order);

            // 通知双方
            notificationService.create(order.getBuyerId(), "DISPUTE", "仲裁结果：恢复交易",
                    "AI仲裁已驳回取消申请，交易继续", dispute.getOrderId());
            notificationService.create(order.getSellerId(), "DISPUTE", "仲裁结果：恢复交易",
                    "AI仲裁已驳回取消申请，交易继续", dispute.getOrderId());

            // 记录日志
            OrderLog log = new OrderLog();
            log.setOrderId(order.getId());
            log.setAction("DISPUTE_REJECTED");
            log.setOperatorId(0L);
            log.setRemark("AI仲裁驳回：" + judgment.getReasoning());
            orderLogMapper.insert(log);

            dispute.setStatus("REJECTED");
        }

        disputeMapper.updateById(dispute);
        log.info("争议 AI 判决执行完成: disputeId={}, verdict={}", disputeId, judgment.getVerdict());
    }

    private void executeGoodsReviewVerdict(Long goodsId, AiJudgment judgment) {
        log.info("开始执行商品审核判决: goodsId={}, verdict={}, confidence={}", goodsId, judgment.getVerdict(), judgment.getConfidence());
        
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.error("商品不存在: goodsId={}", goodsId);
            throw new BusinessException("商品不存在，无法执行审核判决");
        }

        log.info("找到商品: goodsId={}, 当前状态={}, aiReviewStatus={}", goods.getId(), goods.getStatus(), goods.getAiReviewStatus());

        if (judgment.getVerdict() == AiVerdict.APPROVED) {
            goods.setStatus("ACTIVE");
            goods.setAiReviewStatus("APPROVED");
            notificationService.create(goods.getUserId(), "REVIEW", "商品审核通过",
                    "您的商品已通过AI审核，现已上架", goods.getId());
        } else if (judgment.getVerdict() == AiVerdict.REJECTED) {
            goods.setStatus("REJECTED");
            goods.setAiReviewStatus("REJECTED");
            notificationService.create(goods.getUserId(), "REVIEW", "商品审核未通过",
                    "您的商品未通过AI审核，原因: " + judgment.getReasoning(), goods.getId());
        } else {
            goods.setAiReviewStatus("ESCALATED");
        }

        goods.setAiReviewConfidence(judgment.getConfidence());
        goods.setAiReviewNote(judgment.getReasoning());
        goodsMapper.updateById(goods);
        
        log.info("商品审核判决执行完成: goodsId={}, 新状态={}, 新aiReviewStatus={}", goods.getId(), goods.getStatus(), goods.getAiReviewStatus());
    }

    private void notifyAdminForReview(AiJudgmentType type, Long sourceId, Long judgmentId, AiJudgment judgment) {
        if (type == AiJudgmentType.DISPUTE) {
            OrderDispute dispute = disputeMapper.selectById(sourceId);
            if (dispute != null) {
                Order order = orderMapper.selectById(dispute.getOrderId());
                if (order != null) {
                    notificationService.create(0L, "DISPUTE", "仲裁待处理",
                            "AI仲裁置信度不足(%.1f%%)，需要人工审核是否取消订单".formatted(judgment.getConfidence() * 100),
                            dispute.getOrderId());
                }
            }
        }
        log.info("通知管理员审核: type={}, sourceId={}, judgmentId={}", type, sourceId, judgmentId);
    }

    private void notifyReporterOfEscalation(AiJudgmentType type, Long sourceId, Long judgmentId, AiJudgment judgment) {
        try {
            if (type == AiJudgmentType.REPORT) {
                Report report = reportMapper.selectById(sourceId);
                if (report != null) {
                    report.setStatus("ESCALATED");
                    reportMapper.updateById(report);
                    // relatedId 存储被举报的商品/用户ID
                    notificationService.create(report.getReporterId(), "REVIEW", "举报处理进度",
                            "您的举报已收到，正在等待人工审核，预计很快会有结果", report.getTargetId());
                }
            } else if (type == AiJudgmentType.DISPUTE) {
                OrderDispute dispute = disputeMapper.selectById(sourceId);
                if (dispute != null) {
                    dispute.setStatus("ESCALATED");
                    disputeMapper.updateById(dispute);
                    // 获取商品ID用于跳转
                    Order order = orderMapper.selectById(dispute.getOrderId());
                    Long goodsId = order != null ? order.getGoodsId() : null;
                    notificationService.create(dispute.getApplicantId(), "DISPUTE", "仲裁处理进度",
                            "您的仲裁申请已收到，AI无法自动判决，正在等待管理员审核", goodsId);
                }
            } else if (type == AiJudgmentType.GOODS_REVIEW) {
                Goods goods = goodsMapper.selectById(sourceId);
                if (goods != null) {
                    goods.setAiReviewStatus("ESCALATED");
                    goodsMapper.updateById(goods);
                    notificationService.create(goods.getUserId(), "REVIEW", "商品审核中",
                            "AI审核暂时不可用，您的商品正在等待人工审核", goods.getId());
                    log.info("商品AI审核转人工: goodsId={}, 原因={}", goods.getId(), judgment.getReasoning());
                }
            }
        } catch (Exception e) {
            log.error("通知举报人失败", e);
        }
    }

    @Override
    @Transactional
    public void appeal(Long judgmentId, Long userId, String reason) {
        AiJudgmentRecord record = judgmentMapper.selectById(judgmentId);
        if (record == null) throw new BusinessException("判决记录不存在");
        if (!"PROCESSED".equals(record.getStatus())) {
            throw new BusinessException("该判决不可上诉");
        }

        record.setStatus("APPEALED");
        record.setAppealReason(reason);
        judgmentMapper.updateById(record);

        log.info("用户提交上诉: judgmentId={}, userId={}", judgmentId, userId);
    }

    @Override
    @Transactional
    public void handleAppeal(Long judgmentId, Long handlerId, boolean override, String note) {
        AiJudgmentRecord record = judgmentMapper.selectById(judgmentId);
        if (record == null) throw new BusinessException("判决记录不存在");
        if (!"APPEALED".equals(record.getStatus())) {
            throw new BusinessException("该判决未处于上诉状态");
        }

        if (override) {
            record.setStatus("OVERRIDDEN");
            record.setHandleNote("上诉成功: " + note);
        } else {
            record.setStatus("FINAL");
            record.setHandleNote("上诉驳回(终审): " + note);
        }

        record.setHandlerId(handlerId);
        record.setHandleTime(LocalDateTime.now());
        judgmentMapper.updateById(record);

        // 上诉成功时恢复相关业务状态
        if (override) {
            try {
                restoreBusinessStatus(record);
            } catch (Exception e) {
                log.error("恢复业务状态失败: judgmentId={}", judgmentId, e);
            }
        } else {
            // 终审驳回时，设置商品状态为终审驳回（只能删除）
            try {
                setFinalRejectedStatus(record);
            } catch (Exception e) {
                log.error("设置终审驳回状态失败: judgmentId={}", judgmentId, e);
            }
        }

        // 发送通知给用户
        try {
            String title = override ? "上诉成功" : "上诉驳回(终审)";
            String content = override 
                    ? "您的申诉已通过，原判已被推翻。" + (note != null ? " 备注: " + note : "")
                    : "您的申诉已被驳回，此为终审结果，不可再上诉。" + (note != null ? " 备注: " + note : "");
            
            // 根据来源类型和结果获取要通知的用户和关联ID
            Long notifyUserId = getNotifyUserIdForAppeal(record, override);
            Long relatedId = getRelatedIdForAppeal(record);
            if (notifyUserId != null) {
                notificationService.create(notifyUserId, "REVIEW", title, content, relatedId);
            }
        } catch (Exception e) {
            log.error("发送上诉结果通知失败: judgmentId={}", judgmentId, e);
        }

        log.info("处理上诉: judgmentId={}, override={}, handlerId={}", judgmentId, override, handlerId);
    }

    private Long getNotifyUserIdForAppeal(AiJudgmentRecord record, boolean override) {
        try {
            if ("GOODS_REVIEW".equals(record.getSourceType())) {
                Goods goods = goodsMapper.selectById(record.getSourceId());
                return goods != null ? goods.getUserId() : null;
            } else if ("REPORT".equals(record.getSourceType())) {
                Report report = reportMapper.selectById(record.getSourceId());
                if (report == null) return null;
                if (override) {
                    // 上诉成功（推翻举报）→ 通知被举报人
                    if ("GOODS".equals(report.getType())) {
                        Goods goods = goodsMapper.selectById(report.getTargetId());
                        return goods != null ? goods.getUserId() : null;
                    } else if ("USER".equals(report.getType())) {
                        return report.getTargetId();
                    }
                    return null;
                } else {
                    // 上诉失败（维持举报）→ 通知举报人
                    return report.getReporterId();
                }
            } else if ("DISPUTE".equals(record.getSourceType())) {
                OrderDispute dispute = disputeMapper.selectById(record.getSourceId());
                return dispute != null ? dispute.getApplicantId() : null;
            }
        } catch (Exception e) {
            log.error("获取通知用户失败", e);
        }
        return null;
    }

    private Long getRelatedIdForAppeal(AiJudgmentRecord record) {
        try {
            if ("GOODS_REVIEW".equals(record.getSourceType())) {
                return record.getSourceId();
            } else if ("REPORT".equals(record.getSourceType())) {
                Report report = reportMapper.selectById(record.getSourceId());
                return report != null ? report.getTargetId() : null;
            } else if ("DISPUTE".equals(record.getSourceType())) {
                OrderDispute dispute = disputeMapper.selectById(record.getSourceId());
                if (dispute != null) {
                    Order order = orderMapper.selectById(dispute.getOrderId());
                    return order != null ? order.getGoodsId() : null;
                }
            }
        } catch (Exception e) {
            log.error("获取关联ID失败", e);
        }
        return null;
    }

    private void restoreBusinessStatus(AiJudgmentRecord record) {
        if ("GOODS_REVIEW".equals(record.getSourceType())) {
            // 商品审核上诉成功，恢复为待审核状态
            Goods goods = goodsMapper.selectById(record.getSourceId());
            if (goods != null) {
                goods.setStatus("ACTIVE");
                goods.setAiReviewStatus("APPEAL_OVERRIDDEN");
                goodsMapper.updateById(goods);
                log.info("商品审核上诉成功，已恢复上架: goodsId={}", goods.getId());
            }
        } else if ("REPORT".equals(record.getSourceType())) {
            // 举报上诉成功，恢复商品为在售状态
            Report report = reportMapper.selectById(record.getSourceId());
            if (report != null && "GOODS".equals(report.getType())) {
                Goods goods = goodsMapper.selectById(report.getTargetId());
                if (goods != null) {
                    goods.setStatus("ACTIVE");
                    goodsMapper.updateById(goods);
                    log.info("举报上诉成功，已恢复商品在售状态: goodsId={}", goods.getId());
                }
            }
        }
        // DISPUTE 类型的上诉成功需要恢复订单状态，但订单状态恢复比较复杂，这里先不处理
    }

    private void setFinalRejectedStatus(AiJudgmentRecord record) {
        if ("GOODS_REVIEW".equals(record.getSourceType())) {
            // 商品审核终审驳回，设置为终审驳回状态（只能删除）
            Goods goods = goodsMapper.selectById(record.getSourceId());
            if (goods != null) {
                goods.setStatus("FINAL_REJECTED");
                goodsMapper.updateById(goods);
                log.info("商品审核终审驳回: goodsId={}", goods.getId());
            }
        } else if ("REPORT".equals(record.getSourceType())) {
            // 举报终审驳回，设置商品为终审驳回状态（只能删除）
            Report report = reportMapper.selectById(record.getSourceId());
            if (report != null && "GOODS".equals(report.getType())) {
                Goods goods = goodsMapper.selectById(report.getTargetId());
                if (goods != null) {
                    goods.setStatus("FINAL_REJECTED");
                    goodsMapper.updateById(goods);
                    log.info("举报终审驳回: goodsId={}", goods.getId());
                }
            }
        }
    }

    @Override
    public AiJudgmentRecord getBySource(String sourceType, Long sourceId) {
        return judgmentMapper.selectOne(
                new LambdaQueryWrapper<AiJudgmentRecord>()
                        .eq(AiJudgmentRecord::getSourceType, sourceType)
                        .eq(AiJudgmentRecord::getSourceId, sourceId)
                        .eq(AiJudgmentRecord::getDeleted, 0)
                        .orderByDesc(AiJudgmentRecord::getCreateTime)
                        .last("LIMIT 1"));
    }

    @Override
    public AiJudgmentRecord getReportJudgmentByGoodsId(Long goodsId) {
        // 先找到针对该商品的举报记录
        Report report = reportMapper.selectOne(
                new LambdaQueryWrapper<Report>()
                        .eq(Report::getType, "GOODS")
                        .eq(Report::getTargetId, goodsId)
                        .eq(Report::getDeleted, 0)
                        .orderByDesc(Report::getCreateTime)
                        .last("LIMIT 1"));
        if (report == null) {
            return null;
        }
        // 再查找该举报的AI判决
        return getBySource("REPORT", report.getId());
    }

    @Override
    public Map<String, String> getConfig() {
        List<AiConfig> configs = configMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, String> result = new HashMap<>();
        configs.forEach(c -> result.put(c.getConfigKey(), c.getConfigValue()));
        return result;
    }

    @Override
    @Transactional
    public void updateConfig(String key, String value) {
        AiConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getConfigKey, key));
        if (config == null) throw new BusinessException("配置项不存在");

        config.setConfigValue(value);
        configMapper.updateById(config);
    }

    private double getDoubleConfig(String key, double defaultValue) {
        AiConfig config = configMapper.selectOne(
                new LambdaQueryWrapper<AiConfig>().eq(AiConfig::getConfigKey, key));
        if (config == null) return defaultValue;
        try {
            return Double.parseDouble(config.getConfigValue());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
