package com.visitalk.backend.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// 周报结构化输出数据传输对象 (C-3 / C-4)
public class WeeklyReportResponse {
    private String status;
    private String message;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<Map<String, Object>> chartData; // 用于前端渲染情绪趋势折线图
    private List<String> top3Triggers;          -- 高频触发源聚合

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }
    public LocalDate getWeekEndDate() { return weekEndDate; }
    public void setWeekEndDate(LocalDate weekEndDate) { this.weekEndDate = weekEndDate; }
    public List<Map<String, Object>> getChartData() { return chartData; }
    public void setChartData(List<Map<String, Object>> chartData) { this.chartData = chartData; }
    public List<String> getTop3Triggers() { return top3Triggers; }
    public void setTop3Triggers(List<String> top3Triggers) { this.top3Triggers = top3Triggers; }
}