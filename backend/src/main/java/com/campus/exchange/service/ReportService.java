package com.campus.exchange.service;

import com.campus.exchange.dto.CreateReportDTO;
import com.campus.exchange.dto.HandleReportDTO;
import com.campus.exchange.vo.ReportVO;
import java.util.List;

public interface ReportService {
    void create(Long reporterId, CreateReportDTO dto);
    List<ReportVO> getMyReports(Long userId, int page, int size);
    List<ReportVO> getPendingReports(int page, int size);
    void handle(Long reportId, Long handlerId, HandleReportDTO dto);
}
