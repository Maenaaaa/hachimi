package com.campus.exchange.ai.prompt;

import com.campus.exchange.ai.AiJudgmentType;

import java.math.BigDecimal;

public class PromptBuilder {

    public static String buildReportPrompt(String reportType, String reason, String description,
                                           String targetInfo, double targetCreditScore) {
        return """
                你是一个校园交易平台的AI审核员。请分析以下举报并给出判决。

                【举报信息】
                - 举报类型: %s
                - 举报原因: %s
                - 举报描述: %s

                【被举报对象信息】
                %s

                【被举报者信用分】%.1f

                【图片审核要求】
                如果提供了图片，请仔细检查：
                1. 图片是否与商品描述相符
                2. 图片是否真实（非盗图、非网图）
                3. 是否存在虚假宣传

                请分析后按以下JSON格式返回:
                {
                    "verdict": "APPROVED或REJECTED",
                    "confidence": 0.0到1.0之间的置信度,
                    "reasoning": "详细分析理由",
                    "evidence": ["证据1", "证据2"]
                }

                判决标准:
                - APPROVED: 举报成立，被举报对象违规
                - REJECTED: 举报不成立，证据不足
                """.formatted(reportType, reason, description, targetInfo, targetCreditScore);
    }

    public static String buildDisputePrompt(String reason, String orderInfo, String goodsImageUrls,
                                            String chatSummary, double applicantCreditScore,
                                            double respondentCreditScore) {
        String imageInfo = goodsImageUrls != null && !goodsImageUrls.isEmpty() ? 
                "\n【商品图片】\n已提供商品图片，请仔细检查图片与商品描述是否相符，是否存在虚假宣传。" : "";
        return """
                你是一个校园交易平台的AI仲裁员。请分析以下订单争议，决定是否取消该订单。

                【争议信息】
                - 争议原因: %s

                【订单信息】
                %s%s

                【聊天记录摘要】
                %s

                【双方信用分】
                - 申请方: %.1f
                - 被申请方: %.1f

                请分析后按以下JSON格式返回:
                {
                    "verdict": "APPROVED(取消订单)或REJECTED(驳回仲裁，继续交易)",
                    "confidence": 0.0到1.0之间的置信度,
                    "reasoning": "详细分析理由",
                    "evidence": ["证据1", "证据2"]
                }

                仲裁原则:
                - 判断是否确实需要取消订单
                - 证据是否充分支持取消
                - 信用分作为参考
                - 保护双方合法权益
                - 如果提供了商品图片，请仔细检查图片与描述是否相符
                """.formatted(reason, orderInfo, imageInfo, chatSummary,
                applicantCreditScore, respondentCreditScore);
    }

    public static String buildGoodsReviewPrompt(String title, String description, BigDecimal price,
                                                BigDecimal originalPrice, String condition,
                                                String categoryName, double publisherCreditScore) {
        return """
                你是一个校园交易平台的AI审核员。请审核以下商品信息和图片的合规性。

                【商品信息】
                - 标题: %s
                - 描述: %s
                - 转让价格: %s 元
                - 原价: %s 元
                - 成色: %s
                - 分类: %s

                【发布者信用分】%.1f

                【图片审核要求】
                请仔细检查商品图片，确认：
                1. 图片是否包含违规内容（暴力、色情、政治敏感等）
                2. 图片是否与商品描述相符
                3. 图片是否清晰、真实（非盗图、非网图）
                4. 是否存在虚假宣传（如使用官方图代替实物图）

                请审核后按以下JSON格式返回:
                {
                    "verdict": "APPROVED(通过)或REJECTED(拒绝)",
                    "confidence": 0.0到1.0之间的置信度,
                    "reasoning": "详细审核理由",
                    "evidence": ["证据1", "证据2"]
                }

                审核标准:
                - 标题和描述是否包含违禁内容
                - 图片内容是否合规、真实
                - 是否为翻新/假冒商品特征
                - 是否符合校园交易定位
                - 价格审核：如果转让价格为0元，需要结合交易类型判断。置换交易（tradeType=EXCHANGE）中价格为0是正常的（以物易物），不应因此拒绝。仅在出售交易（tradeType=SELL）中，0元或异常低价才需要警惕诈骗风险。
                """.formatted(title, description, price.toPlainString(),
                originalPrice != null && originalPrice.compareTo(BigDecimal.ZERO) > 0 ? originalPrice.toPlainString() : "未填写",
                condition, categoryName, publisherCreditScore);
    }
}
