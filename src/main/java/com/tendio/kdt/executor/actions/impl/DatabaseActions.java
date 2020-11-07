package com.tendio.kdt.executor.actions.impl;

import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;
import com.tendio.kdt.executor.actions.model.ActionFactory;
import com.tendio.kdt.executor.connectors.db.OracleDatabase;
import com.tendio.kdt.reporting.Report;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@ActionClass
public class DatabaseActions extends CommonActions {
    private static final String FILE_IDENTIFIER = "file=";

    private static String resolveSqlQuery(String inputParam) {
        if (inputParam.startsWith(FILE_IDENTIFIER)) {
            File file = new File(inputParam.replace(FILE_IDENTIFIER, ""));
            try {
                return FileUtils.readFileToString(file, Charset.defaultCharset());
            } catch (IOException ignored) {
            }
        }
        return inputParam;
    }

    @ActionDefinition("Execute SQL DML query: \"Query\"")
    public void executeUpdateQuery(String query) {
        Instant start = Instant.now();
        String parameterizedQuery = ActionFactory.resolveTestSuiteParameters(resolveSqlQuery(query));
        Instant end = Instant.now();
        int rowsAffected = OracleDatabase.executeUpdateQuery(parameterizedQuery);
        Duration duration = Duration.between(start, end);

        String message = String.format("Successfully executed query: %s. Execution time: %s. Rows affected: %s",
                parameterizedQuery, duration, rowsAffected);
        //Report.getReport().info(message, null);
    }

    public String getSingleValueQueryResult(String query) throws SQLException {
        String resolvedQuery = ActionFactory.resolveTestSuiteParameters(resolveSqlQuery(query));
        ResultSet rs = OracleDatabase.executeQuery(resolvedQuery);
        if (rs != null && rs.next()) {
            return rs.getString(1);
        }
        return null;
    }

        @ActionDefinition("Verify query \"Query\" returns values: \"Column=Value,Col=Val,ID=123\"")
    public void verifyQueryRows(String query, String keyValueString) throws SQLException {
        String resolvedQuery = ActionFactory.resolveTestSuiteParameters(resolveSqlQuery(query));
        ResultSet rs = OracleDatabase.executeQuery(resolvedQuery);
        Map<String, String> keyValueMap = parseKeyValueString(keyValueString);

        //looking only through the first row!!!
        if (rs != null) {
            rs.next();
            keyValueMap.forEach((column, expectedValue) -> {
                {
                    String actualValue = null;
                    try {
                        actualValue = rs.getString(column);
                    } catch (SQLException e) {
                        LOGGER.error("Could not retrieve value of column {} and row {} for query:", column, 1, query);
                    }

                    if (expectedValue.equals(actualValue)) {
                        Report.getReport().info("Actual value=Expected value=" + expectedValue);
                    } else {
                        String message = String.format("AR!=ER... Actual value: %s. Expected value: %s", actualValue, expectedValue);
                        //Report.getReport().error(message);
                    }
                }
            });
        } else {
            //Report.getReport().warn("Result set was null for query: " + query);
        }
    }

    @ActionDefinition("Execute SQL call: \"Query\"")
    public void executeCall(String query) {
        String resolvedQuery = ActionFactory.resolveTestSuiteParameters(resolveSqlQuery(query));
        Instant start = Instant.now();
        OracleDatabase.executeCall(resolvedQuery);
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);

        String message = String.format("Successfully executed SQL call: %s. Execution time: %s", resolvedQuery, duration);
        //Report.getReport().info(message, null);
    }


}

