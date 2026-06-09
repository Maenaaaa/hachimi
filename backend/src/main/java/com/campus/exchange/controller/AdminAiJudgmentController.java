package com.campus.exchange.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.exchange.common.PageResult;
import com.campus.exchange.common.Result;
import com.campus.exchange.entity.AiJudgmentRecord;
import com.campus.exchange.entity.User;
import com.campus.exchange.mapper.AiJudgmentMapper;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.AiJudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ai-judgment")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiJudgmentController {

    private final AiJudgmentMapper judgmentMapper;
    private final AiJudgeService aiJudgeService;

    @GetMapping
    public Result<PageResult<AiJudgmentRecord>> list(
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<AiJudgmentRecord> wrapper = new LambdaQueryWrapper<>();
        if (sourceType != null && !sourceType.isEmpty()) {
            wrapper.eq(AiJudgmentRecord::getSourceType, sourceType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AiJudgmentRecord::getStatus, status);
        }
        wrapper.orderByDesc(AiJudgmentRecord::getCreateTime);
        Page<AiJudgmentRecord> p = new Page<>(page, size);
        judgmentMapper.selectPage(p, wrapper);
        return Result.ok(PageResult.of(p.getRecords(), p.getTotal(), page, size));
    }

    @GetMapping("/{id}")
    public Result<AiJudgmentRecord> getById(@PathVariable Long id) {
        AiJudgmentRecord record = judgmentMapper.selectById(id);
        if (record == null) {
            return Result.notFound("判决记录不存在");
        }
        return Result.ok(record);
    }

    @PutMapping("/{id}/execute")
    public Result<Void> executeVerdict(@PathVariable Long id) {
        aiJudgeService.executeVerdict(id);
        return Result.ok();
    }

    @PutMapping("/{id}/handle")
    public Result<Void> handleManually(@CurrentUser User user, @PathVariable Long id,
                                       @RequestBody java.util.Map<String, String> body) {
        AiJudgmentRecord record = judgmentMapper.selectById(id);
        if (record == null) {
            return Result.notFound("判决记录不存在");
        }
        record.setStatus("PROCESSED");
        record.setHandlerId(user.getId());
        record.setHandleNote(body.get("note"));
        record.setAutoHandled(false);
        judgmentMapper.updateById(record);
        return Result.ok();
    }
}
