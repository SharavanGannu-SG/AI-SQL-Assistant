package com.example.AI_SQL_Assistant.dto;

import java.util.List;
import java.util.Map;

public class QueryResponse {

    String answer;
    String sql;
    List<Map<String, Object>> data;

    public QueryResponse(String answer, String sql, List<Map<String, Object>> data) {
        this.answer = answer;
        this.sql = sql;
        this.data = data;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
