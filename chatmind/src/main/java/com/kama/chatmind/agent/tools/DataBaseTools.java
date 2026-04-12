package com.kama.chatmind.agent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
@Slf4j
public class DataBaseTools implements Tool {

    private static final int MAX_ROWS = 50;

    /**
     * Pattern to detect forbidden DML/DDL keywords anywhere in the query.
     * Word boundaries (\b) prevent false positives on column/table names.
     */
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
            "\\b(INSERT|UPDATE|DELETE|DROP|ALTER|TRUNCATE|CREATE|REPLACE|MERGE|GRANT|REVOKE|EXEC|EXECUTE|CALL)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final JdbcTemplate jdbcTemplate;

    public DataBaseTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String getName() {
        return "dataBaseTool";
    }

    @Override
    public String getDescription() {
        return "Query the PostgreSQL database. Only SELECT queries are allowed. "
                + "Use this to answer questions about agents, knowledge bases, documents, and other business data.";
    }

    @Override
    public ToolType getType() {
        return ToolType.OPTIONAL;
    }

    /**
     * Execute a read-only SQL query against the PostgreSQL database.
     *
     * @param sql SQL query (only SELECT / WITH ... SELECT are allowed)
     * @return Formatted query results as a text table
     */
    @org.springframework.ai.tool.annotation.Tool(
            name = "databaseQuery",
            description = "Query the PostgreSQL database. Only SELECT queries are allowed. "
                    + "Use this to answer questions about agents, knowledge bases, documents, and other business data. "
                    + "The parameter sql is a SQL SELECT statement."
    )
    public String query(String sql) {
        if (sql == null || sql.isBlank()) {
            return "Error: SQL query must not be empty.";
        }

        String trimmed = sql.trim();

        // Strip trailing semicolons for validation
        while (trimmed.endsWith(";")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1).trim();
        }

        // Must start with SELECT or WITH (for CTEs)
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH")) {
            log.warn("Rejected non-SELECT query: {}", sql);
            return "Error: Only SELECT queries are allowed. The query must start with SELECT or WITH.";
        }

        // Reject any forbidden DML/DDL keywords
        if (FORBIDDEN_PATTERN.matcher(trimmed).find()) {
            log.warn("Rejected query containing forbidden keywords: {}", sql);
            return "Error: The query contains a forbidden statement. "
                    + "Only read-only SELECT queries are allowed. "
                    + "INSERT, UPDATE, DELETE, DROP, ALTER, TRUNCATE and other write operations are not permitted.";
        }

        try {
            // Apply a row limit if the query does not already have one
            String limitedSql = applyRowLimit(trimmed);

            List<String> rows = jdbcTemplate.query(limitedSql, (ResultSet rs) -> {
                List<String> resultRows = new ArrayList<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnCount == 0) {
                    resultRows.add("Query returned no columns.");
                    return resultRows;
                }

                // Collect column names and initialise widths
                List<String> columnNames = new ArrayList<>();
                List<Integer> columnWidths = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    columnNames.add(columnName);
                    columnWidths.add(columnName.length());
                }

                // Collect data rows and compute max column widths
                List<List<String>> dataRows = new ArrayList<>();
                while (rs.next()) {
                    List<String> rowData = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        String valueStr = value == null ? "NULL" : value.toString();
                        rowData.add(valueStr);
                        int currentWidth = columnWidths.get(i - 1);
                        if (valueStr.length() > currentWidth) {
                            columnWidths.set(i - 1, valueStr.length());
                        }
                    }
                    dataRows.add(rowData);
                }

                // Format header row
                StringBuilder header = new StringBuilder("| ");
                for (int i = 0; i < columnCount; i++) {
                    if (i > 0) header.append(" | ");
                    header.append(String.format("%-" + columnWidths.get(i) + "s", columnNames.get(i)));
                }
                header.append(" |");
                resultRows.add(header.toString());

                // Separator line
                StringBuilder separator = new StringBuilder("|");
                for (int i = 0; i < columnCount; i++) {
                    separator.append("-".repeat(columnWidths.get(i) + 2)).append("|");
                }
                resultRows.add(separator.toString());

                // Data rows
                if (dataRows.isEmpty()) {
                    resultRows.add("(no data)");
                } else {
                    for (List<String> rowData : dataRows) {
                        StringBuilder row = new StringBuilder("| ");
                        for (int i = 0; i < columnCount; i++) {
                            if (i > 0) row.append(" | ");
                            row.append(String.format("%-" + columnWidths.get(i) + "s", rowData.get(i)));
                        }
                        row.append(" |");
                        resultRows.add(row.toString());
                    }
                }

                return resultRows;
            });

            int dataRowCount = Math.max(0, rows.size() - 2); // subtract header + separator
            if (dataRowCount > 0 && rows.get(rows.size() - 1).equals("(no data)")) {
                dataRowCount = 0;
            }

            log.info("SQL query executed successfully, returned {} row(s)", dataRowCount);
            return "Query results (" + dataRowCount + " row" + (dataRowCount == 1 ? "" : "s") + "):\n"
                    + String.join("\n", rows);
        } catch (Exception e) {
            log.error("SQL query failed: {}", e.getMessage());
            return "Error executing query: " + e.getMessage();
        }
    }

    /**
     * Appends a LIMIT clause if the query does not already contain one,
     * to prevent excessively large result sets.
     */
    private String applyRowLimit(String sql) {
        if (sql.toUpperCase(Locale.ROOT).contains("LIMIT")) {
            return sql;
        }
        return sql + " LIMIT " + MAX_ROWS;
    }
}
