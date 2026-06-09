package com.campus.exchange.controller;

import com.campus.exchange.common.Result;
import com.campus.exchange.entity.AiJudgmentRecord;
import com.campus.exchange.entity.User;
import com.campus.exchange.mapper.AiJudgmentMapper;
import com.campus.exchange.security.CurrentUser;
import com.campus.exchange.service.AiJudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai-judgment")
@RequiredArgsConstructor
public class AiJudgmentController {

    private final AiJudgeService aiJudgeService;
    private final AiJudgmentMapper judgmentMapper;

    @GetMapping("/{id}")
    public Result<AiJudgmentRecord> getById(@PathVariable Long id) {
        AiJudgmentRecord record = judgmentMapper.selectById(id);
        if (record == null) {
            return Result.notFound("判决记录不存在");
        }
        return Result.ok(record);
    }

    @GetMapping("/source/{type}/{sourceId}")
    public Result<AiJudgmentRecord> getBySource(@PathVariable String type, @PathVariable Long sourceId) {
        AiJudgmentRecord record = aiJudgeService.getBySource(type, sourceId);
        return Result.ok(record);
    }

    @GetMapping("/goods/{goodsId}")
    public Result<Map<String, AiJudgmentRecord>> getByGoodsId(@PathVariable Long goodsId) {
        // 查询商品审核和举报相关的AI判决
        AiJudgmentRecord goodsReview = aiJudgeService.getBySource("GOODS_REVIEW", goodsId);
        AiJudgmentRecord report = aiJudgeService.getReportJudgmentByGoodsId(goodsId);
        return Result.ok(Map.of(
            "goodsReview", goodsReview != null ? goodsReview : new AiJudgmentRecord(),
            "report", report != null ? report : new AiJudgmentRecord()
        ));
    }

    @PutMapping("/{id}/appeal")
    public Result<Void> appeal(@CurrentUser User user, @PathVariable Long id,
                               @RequestBody Map<String, String> body) {
        aiJudgeService.appeal(id, user.getId(), body.get("reason"));
        return Result.ok();
    }

    @PutMapping("/{id}/handle-appeal")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> handleAppeal(@CurrentUser User user, @PathVariable Long id,
                                     @RequestBody Map<String, Object> body) {
        boolean override = (boolean) body.get("override");
        String note = (String) body.get("note");
        aiJudgeService.handleAppeal(id, user.getId(), override, note);
        return Result.ok();
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, String>> getConfig() {
        return Result.ok(aiJudgeService.getConfig());
    }

    @PutMapping("/config/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        aiJudgeService.updateConfig(key, body.get("value"));
        return Result.ok();
    }
}
