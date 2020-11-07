package com.tendio.kdt.configurator.excel;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tendio.kdt.TestProperties;
import com.tendio.kdt.configurator.TestConfigurationSourceReader;
import com.tendio.kdt.configurator.model.Step;
import com.tendio.kdt.configurator.model.TestCase;
import com.tendio.kdt.configurator.model.TestSuite;
import com.tendio.kdt.configurator.model.TestSuiteParameters;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExcelReader implements TestConfigurationSourceReader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String EXECUTION_FLAG = "Y";
    private static final String NO_EXECUTION_FLAG = "N";
    private static final int EMPTY_TEST_CASE_IDENTIFIER = 2;
    private File file;
    private Helper helper = new Helper();
    private Workbook excelWorkbook;

    public TestSuite readTestSuite() throws IOException, InvalidFormatException {
        final String filepathFromProperties = TestProperties.getProperty("test.suite.filepath");
        this.excelWorkbook = openExcelFile(filepathFromProperties);

        TestSuiteParameters parameters = new TestSuiteParameters(readParameters());
        ArrayList<TestCase> executableTestCases = readTestCases();

        this.excelWorkbook.close();
        String testSuiteId = FilenameUtils.removeExtension(file.getName());
        TestSuite testSuite = new TestSuite(testSuiteId, executableTestCases, parameters);
        LOGGER.debug("Successfully read test suite to be executed: {}",
                testSuite.getTestCases().stream().map(TestCase::getId).toArray());
        return testSuite;
    }

    private ArrayList<TestCase> readTestCases() {
        List<String> testCasesForExecution = getTestCasesListForExecution();
        ArrayList<TestCase> executableTestCases = Lists.newArrayList();
        for (String testCaseId : testCasesForExecution) {
            if (!helper.isSheetEmpty(testCaseId)) {
                TestCase executableTestCase = readTestCase(testCaseId);
                executableTestCases.add(executableTestCase);
            } else {
                LOGGER.debug("Test Case with ID={} is empty so won`t be read", testCaseId);
            }
        }

        return executableTestCases;
    }


    private Workbook openExcelFile(String fileName) throws IOException, InvalidFormatException {
        this.file = new File(fileName);
        LOGGER.debug("Opening excel file {} ...", file.getAbsolutePath());
        this.excelWorkbook = WorkbookFactory.create(file);
        LOGGER.debug("Successfully opened excel file {} ", file.getAbsolutePath());
        return excelWorkbook;
    }

    private TestCase readTestCase(String testCaseId) {
        LOGGER.debug("Reading test case with ID={} ...", testCaseId);
        int stepDescriptionCellIndex = 0;
        int stepDefinitionCellIndex = 1;
        int stepExecutionCellIndex = 2;
        Sheet testCaseSheet = helper.getSheet(testCaseId);
        int rowsCount = testCaseSheet.getPhysicalNumberOfRows();
        LinkedList<Step> testCaseSteps = Lists.newLinkedList();

        //starting from the second row where TC definitions begin
        for (int i = 1; i < rowsCount; i++) {
            Row row = helper.getRow(testCaseSheet, i);
            String isExecutedFlag = helper.getStringCellValue(row, stepExecutionCellIndex);
            if (!NO_EXECUTION_FLAG.equals(isExecutedFlag)) {
                String description = helper.getStringCellValue(row, stepDescriptionCellIndex);
                String stepDefinition = helper.getStringCellValue(row, stepDefinitionCellIndex);
                Step step = new Step(description, stepDefinition);
                testCaseSteps.add(step);
            }
        }

        TestCase testCase = new TestCase(testCaseSteps, testCaseId);
        LOGGER.debug("Successfully read test case with ID={}: {}", testCaseId, testCase);
        return testCase;
    }

    private List<String> getTestCasesListForExecution() {
        final String configSheet = ExcelConfigurationProperties.TestSuite.CONFIGURATION_SHEET_NAME;
        LOGGER.debug("Opening excel sheet {} and reading test cases marked for execution", configSheet);
        Sheet configurationSheet = helper.getSheet(configSheet);
        LinkedList<String> testCases = Lists.newLinkedList();

        int testCaseCellIndex = 0;
        int executionFlagCellIndex = 1;
        int rowsCount = configurationSheet.getPhysicalNumberOfRows();

        //starting from the second row where TC definitions begin
        for (int i = 1; i < rowsCount; i++) {
            Row row = helper.getRow(configurationSheet, i);
            String testCaseId = helper.getStringCellValue(row, testCaseCellIndex);
            if (EXECUTION_FLAG.equals(helper.getStringCellValue(row, executionFlagCellIndex))) {
                if (!Strings.isNullOrEmpty(testCaseId)) {
                    LOGGER.debug("Test Case with ID={} is marked for execution", testCaseId);
                    testCases.add(testCaseId);
                }
            } else {
                LOGGER.debug("Test Case with ID={} will NOT be executed", testCaseId);
            }
        }

        LOGGER.debug("Successfully read excel sheet {}", configSheet);
        return testCases;
    }

    private Map<String, String> readParameters() {
        final String paramsSheet = ExcelConfigurationProperties.TestSuite.PARAMETERS_SHEET_NAME;
        LOGGER.debug("Opening excel sheet {} and reading test suite parameters", paramsSheet);
        Sheet parametersSheet = helper.getSheet(paramsSheet);
        Map<String, String> params = Maps.newHashMap();

        int keyCellIndex = 0;
        int valueCellIndex = 1;
        int rowsCount = parametersSheet.getPhysicalNumberOfRows();

        //starting from the second row where parameters mapping begins
        for (int i = 1; i < rowsCount; i++) {
            Row row = helper.getRow(parametersSheet, i);
            String key = helper.getStringCellValue(row, keyCellIndex);
            String value = helper.getStringCellValue(row, valueCellIndex);

            if (!Strings.isNullOrEmpty(key) && (value != null)) {
                LOGGER.debug("Adding parameter: {}={}", key, value);
                params.put(key, value);
            } else {
                LOGGER.warn("Incorrect parameter: {}={}", key, value);
            }
        }

        LOGGER.debug("Successfully read excel sheet {}", paramsSheet);
        return !params.isEmpty() ? params : Collections.EMPTY_MAP;
    }

/*    private void readCompositeSteps() {
        final String compositeStepsSheet = ExcelConfigurationProperties.TestSuite.COMPOSITE_STEPS_SHEET_NAME;
        LOGGER.debug("Opening excel sheet {} and reading composite steps", compositeStepsSheet);
        Sheet compositesSheet = helper.getSheet(compositeStepsSheet);

        int compositeStepDefinitionCellIndex = 0;
        int singleStepDefinitionCellIndex = 1;
        int rowsCount = compositesSheet.getPhysicalNumberOfRows();

        //starting from the second row to skip header
        for (int i = 1; i < rowsCount; i++) {
            Row row = helper.getRow(compositesSheet, i);
            String compositeStepDefinition = helper.getStringCellValue(row, compositeStepDefinitionCellIndex);



            while (true) {
                int nextRowIndex = i +1;
                Row nextRow = helper.getRow(compositesSheet, nextRowIndex);
                String compositeStepDefinition = helper.getStringCellValue(row, compositeStepDefinitionCellIndex);
                if (!compositeStepDefinition.trim().isEmpty()) {
                    break;
                }
            }
            //find first and last rownum
            //execute ActionFactory.resolveCompositeStep()
            //put in list
        }

        LOGGER.debug("Successfully read excel sheet {}. Composite steps found: {}", compositeStepsSheet, compositeSteps);
    }*/


    private class Helper {
        private Sheet getSheet(@Nonnull String sheetName) {
            final Sheet sheet = excelWorkbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException(String.format("Sheet %s was not found in file: %s ", sheetName, file.getName()));
            }

            return sheet;
        }

        private Row getRow(@Nonnull Sheet sheet, int rowIndex) {
            final Row row = sheet.getRow(rowIndex);
            if (row == null) {
                throw new IllegalArgumentException(String.format("Row with index %s was not found on sheet: %s ", rowIndex, sheet.getSheetName()));
            }

            return row;
        }

        private Cell getCell(@Nonnull Row row, int cellIndex) {
            final Cell cell = row.getCell(cellIndex);
            if (cell == null) {
                String message = String.format("Cell with index %s was not found in row: %s ", cellIndex, row.getRowNum());
                throw new IllegalArgumentException(message);
            }

            return cell;
        }

        private String getStringCellValue(@Nonnull Row row, int cellIndex) {
            Cell cell = row.getCell(cellIndex);
            if (cell == null) {
                /*String message = String.format("Row %s: cell with index %s is empty", row.getRowNum(), cellIndex);
                throw new InvalidConfigurationException(message);*/
                return "";
            }

            //cell.getCellType() - bad API
            // let`s try to retrieve String, then Numeric value or fail with exception while reading
            try {
                return cell.getStringCellValue();
            } catch (IllegalStateException ignored) {
                //do nothing - let`s try to retrieve Numeric value
            }
            return String.valueOf(cell.getNumericCellValue());
        }

        private boolean isSheetEmpty(@Nonnull String name) {
            final Sheet sheet = excelWorkbook.getSheet(name);
            return sheet.getPhysicalNumberOfRows() == EMPTY_TEST_CASE_IDENTIFIER;
        }
    }

}
