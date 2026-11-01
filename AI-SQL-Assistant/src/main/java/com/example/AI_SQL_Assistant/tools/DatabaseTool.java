package com.example.AI_SQL_Assistant.tools;

import com.example.AI_SQL_Assistant.validation.SqlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DatabaseTool {

    private final JdbcTemplate jdbcTemplate;
    private final SqlValidator sqlValidator;
    private final List<String> allowedTables;

    private String sql;
    private List<Map<String, Object>> data;

    private static final Logger log =
            LoggerFactory.getLogger(DatabaseTool.class);

    public DatabaseTool(
            JdbcTemplate jdbcTemplate,
            SqlValidator sqlValidator,
            @Value("${ai.sql.allowed-tables}") String tableNames) {

        this.jdbcTemplate = jdbcTemplate;
        this.sqlValidator = sqlValidator;
        this.allowedTables = Arrays.stream(tableNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());
    }

    @Tool(description = "Returns the columns and data types of the permitted database tables. " +
                        "Use this before generating SQL when the schema is unknown.")
    public List<Map<String, Object>> getSchema() {

        if (allowedTables.isEmpty()) {
            return List.of(Map.of("Error", "No tables are permitted/allowed"));
        }

        String placeholders = String.join(
                ",", Collections.nCopies( allowedTables.size(), "?"));

        log.info("Discovering database schema");

        String schemaSql =
                "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE " +
                        "FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_SCHEMA = DATABASE() " +
                        "AND TABLE_NAME IN (" + placeholders + ") " +
                        "ORDER BY TABLE_NAME, ORDINAL_POSITION";

        List<Map<String, Object>> schema = jdbcTemplate.queryForList(
                schemaSql, allowedTables.toArray());

        log.info("Schema discovery completed. \n {}", schema);

        return schema;
    }

    @Tool(description = "Validates and executes a read-only SELECT query against permitted tables. " +
                        "Use only after discovering the schema. Never submit write operations.")
    public List<Map<String, Object>> executeSql(
            @ToolParam(description = "A single SQL SELECT query") String sql) {

        log.info("SQL execution requested");

        this.sql = sql;
        sqlValidator.validate(sql);

        log.info("SQL validation passed");
        log.debug("Generated SQL: {}", sql);

        data = jdbcTemplate.queryForList(sql);

        log.info("SQL execution completed. Rows returned: {}",
                data.size());

        return data;

    }

    public void reset() {
        this.sql = null;
        this.data = null;
    }

    public String getSql() {
        return sql;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

}
