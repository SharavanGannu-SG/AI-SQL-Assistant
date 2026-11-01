package com.example.AI_SQL_Assistant.service;

import com.example.AI_SQL_Assistant.dto.QueryResponse;
import com.example.AI_SQL_Assistant.tools.DatabaseTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiQueryService {

    private final ChatClient chatClient;
    private final DatabaseTool databaseTool;
    private final Resource systemPrompt;

    public AiQueryService(ChatClient chatClient, DatabaseTool databaseTool,
                          @Value("classpath:/promptTemplates/SystemPromptTemplate.st") Resource systemPrompt) {
        this.chatClient = chatClient;
        this.databaseTool = databaseTool;
        this.systemPrompt = systemPrompt;
    }

    public QueryResponse processQuestion(String userPrompt) {

        databaseTool.reset();

        String answer = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .tools(databaseTool)
                .call()
                .content();

        if (databaseTool.getData() == null) {
            return new QueryResponse(
                    "No database query was executed. " + answer,
                    databaseTool.getSql(),
                    List.of()
            );
        }

        return new QueryResponse(
                answer,
                databaseTool.getSql(),
                databaseTool.getData()
        );

    }

}
