package com.example.AI_SQL_Assistant.controller;

import com.example.AI_SQL_Assistant.dto.QueryRequest;
import com.example.AI_SQL_Assistant.dto.QueryResponse;
import com.example.AI_SQL_Assistant.service.AiQueryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AiQueryController {

    private final AiQueryService aiQueryService;

    public AiQueryController(AiQueryService aiQueryService) {
        this.aiQueryService = aiQueryService;
    }

    @PostMapping("/query")
    public ResponseEntity<QueryResponse> query(@Valid @RequestBody QueryRequest queryRequest) {
        return ResponseEntity.ok(aiQueryService.processQuestion(queryRequest.getPrompt().trim()));
    }
}
