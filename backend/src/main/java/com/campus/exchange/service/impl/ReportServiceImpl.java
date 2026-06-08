package com.campus.exchange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.dto.CreateReportDTO;
import com.campus.exchange.dto.HandleReportDTO;
import com.campus.exchange.entity.*;
import com.campus.exchange.exception.BusinessException;
import com.campus.exchange.mapper.*;
import com.campus.exchange.service.NotificationService;
import com.campus.exchange.service.CreditScoreService;
import com.campus.exchange.service.ReportService;
import com.campus.exchange.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final CreditScoreService creditScoreService;
    private final NotificationService notificationService;

    @Override
    public void create(Long reporterId, CreateReportDTO dto) {
        if ("GOODS".equals(dto.getType())) {
            Goods goods = goodsMapper.selectById(dto.getTargetId());
            if (goods == null) throw new BusinessException("商品不存在");
            if (goods.getUserId().equals(reporterId)) throw new BusinessException("不能举报自己的商品");
        }

        if (reportMapper.selectCount(new LambdaQueryWrapper<Report>()
                .eq(Report::getReporterId, reporterId)
                .eq(Report::getType, dto.getType())
                .eq(Report::getTargetId, dto.getTargetId())
                .eq(Report::getStatus, "PENDING")) > 0) {
            throw new BusinessException("您已举报过，请等待处理");
        }

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setType(dto.getType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setDescription(dto.getDescription());
        report.setStatus("PENDING");
        reportMapper.insert(report);
    }

    @Override
    public List<ReportVO> getMyReports(Long userId, int page, int size) {
        Page<Report> p = new Page<>(page, size);
        return reportMapper.selectPage(p,
                new LambdaQueryWrapper<Report>().eq(Report::getReporterId, userId)
                        .orderByDesc(Report::getCreateTime)).getRecords().stream().map(this::toVO).toList();
    }

    @Override
    public List<ReportVO> getPendingReports(int page, int size) {
        Page<Report> p = new Page<>(page, size);
        return reportMapper.selectPage(p,
                new LambdaQueryWrapper<Report>().orderByAsc(Report::getStatus)
                        .orderByDesc(Report::getCreateTime)).getRecords().stream().map(this::toVO).toList();
    }

    @Override
    @Transactional
    public void handle(Long reportId, Long handlerId, HandleReportDTO dto) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) throw new BusinessException("举报不存在");

        report.setStatus(dto.getStatus());
        report.setHandlerId(handlerId);
        report.setHandleNote(dto.getHandleNote());
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);

        if ("APPROVED".equals(dto.getStatus())) {
            if ("GOODS".equals(report.getType())) {
                Goods goods = goodsMapper.selectById(report.getTargetId());
                if (goods != null) {
                    goods.setStatus("TAKEN_DOWN");
                    goodsMapper.updateById(goods);
                    notificationService.create(goods.getUserId(), "REVIEW", "您的商品已被下架",
                            "商品「" + goods.getTitle() + "」因被举报违规已被下架", goods.getId());
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
        }

        notificationService.create(report.getReporterId(), "REVIEW", "举报处理结果",
                "您提交的举报已处理，结果：" + ("APPROVED".equals(dto.getStatus()) ? "举报成立" : "举报不成立"), reportId);
    }

    private ReportVO toVO(Report report) {
        User reporter = userMapper.selectById(report.getReporterId());
        User handler = report.getHandlerId() != null ? userMapper.selectById(report.getHandlerId()) : null;

        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setReporterId(report.getReporterId());
        vo.setReporterNickname(reporter != null ? reporter.getNickname() : "");
        vo.setType(report.getType());
        vo.setTargetId(report.getTargetId());
        vo.setReason(report.getReason());
        vo.setDescription(report.getDescription());
        vo.setStatus(report.getStatus());
        vo.setHandlerId(report.getHandlerId());
        vo.setHandlerNickname(handler != null ? handler.getNickname() : "");
        vo.setHandleNote(report.getHandleNote());
        vo.setHandleTime(report.getHandleTime());
        vo.setCreateTime(report.getCreateTime());
        return vo;
    }
}
