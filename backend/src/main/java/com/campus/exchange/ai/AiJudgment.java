package com.campus.exchange.ai;

import java.util.List;

public class AiJudgment {

    private AiVerdict verdict;
    private double confidence;
    private String reasoning;
    private List<String> evidence;

    public AiJudgment() {}

    public AiJudgment(AiVerdict verdict, double confidence, String reasoning, List<String> evidence) {
        this.verdict = verdict;
        this.confidence = confidence;
        this.reasoning = reasoning;
        this.evidence = evidence;
    }

    public static AiJudgmentBuilder builder() {
        return new AiJudgmentBuilder();
    }

    public AiVerdict getVerdict() { return verdict; }
    public void setVerdict(AiVerdict verdict) { this.verdict = verdict; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public List<String> getEvidence() { return evidence; }
    public void setEvidence(List<String> evidence) { this.evidence = evidence; }

    public static class AiJudgmentBuilder {
        private AiVerdict verdict;
        private double confidence;
        private String reasoning;
        private List<String> evidence;

        public AiJudgmentBuilder verdict(AiVerdict verdict) { this.verdict = verdict; return this; }
        public AiJudgmentBuilder confidence(double confidence) { this.confidence = confidence; return this; }
        public AiJudgmentBuilder reasoning(String reasoning) { this.reasoning = reasoning; return this; }
        public AiJudgmentBuilder evidence(List<String> evidence) { this.evidence = evidence; return this; }
        public AiJudgment build() { return new AiJudgment(verdict, confidence, reasoning, evidence); }
    }
}
