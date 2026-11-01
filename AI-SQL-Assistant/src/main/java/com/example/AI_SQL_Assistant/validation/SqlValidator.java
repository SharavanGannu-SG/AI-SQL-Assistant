package com.example.AI_SQL_Assistant.validation;

import com.example.AI_SQL_Assistant.tools.DatabaseTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SqlValidator {

    private final Set<String> allowedTables;

    private static final Logger log =
            LoggerFactory.getLogger(SqlValidator.class);

    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "(?i)\\b(?:FROM|JOIN)\\s+" +
                    "([a-zA-Z_][a-zA-Z0-9_$]*" +
                    "(?:\\.[a-zA-Z_][a-zA-Z0-9_$]*)?)"
    );

    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
            "(?i)\\b(INSERT|UPDATE|DELETE|DROP|ALTER|CREATE|" +
                    "TRUNCATE|REPLACE|GRANT|REVOKE|CALL|EXEC|EXECUTE|" +
                    "LOAD|OUTFILE|DUMPFILE|LOCK|UNLOCK|SET|USE|HANDLER)\\b"
    );

    public SqlValidator( @Value("${ai.sql.allowed-tables}") String tableNames) {

        this.allowedTables = Arrays.stream(tableNames.split(","))
                .map(String::trim)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    public void validate(String sql) {

        if (sql == null || sql.isBlank()) {
            log.info("SQL query cannot be empty.");
            throw new IllegalArgumentException("SQL query cannot be empty.");
        }

        String query = sql.trim();

        if (!query.matches("(?is)^SELECT\\b.*")) {
            log.info("Only SELECT queries are allowed.");
            throw new IllegalArgumentException(
                    "Only SELECT queries are allowed.");
        }

        if (query.contains(";") ||
                query.contains("--") ||
                query.contains("/*") ||
                query.contains("*/") ||
                query.contains("#") ||
                query.contains("`")) {
            log.info("Multiple statements, comments and quoted identifiers are not allowed.");
            throw new IllegalArgumentException(
                    "Multiple statements, comments and quoted identifiers are not allowed.");
        }

        if (FORBIDDEN_PATTERN.matcher(query).find()) {
            log.info("Forbidden statements are not allowed.");
            throw new IllegalArgumentException(
                    "The SQL contains a forbidden operation.");
        }

        Matcher matcher = TABLE_PATTERN.matcher(query);

        while (matcher.find()) {
            String table = matcher.group(1);

            if (table.contains(".")) {
                log.info("Schema-qualified table references are not allowed.");
                throw new IllegalArgumentException(
                        "Schema-qualified table references are not allowed.");
            }

            if (!allowedTables.contains(
                    table.toLowerCase(Locale.ROOT))) {
                log.info("Access to table {} is not allowed.", table);
                throw new IllegalArgumentException(
                        "Access to table '" + table + "' is not allowed.");
            }
        }
    }
}
