package com.tendio.kdt;

import com.tendio.kdt.configurator.excel.ExcelReader;
import com.tendio.kdt.configurator.model.TestSuite;
import com.tendio.kdt.executor.PooledTestSuiteExecutor;
import com.tendio.kdt.executor.actions.model.ActionsRegistrator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.IOException;

public class KdtApplication {

    public static void main(String[] args) {
        try {
            ActionsRegistrator.registerActions();
            ExcelReader reader = new ExcelReader();
            TestSuite testSuite = reader.readTestSuite();
            PooledTestSuiteExecutor.execute(testSuite);
        } catch (IOException | ClassNotFoundException | InvalidFormatException e) {
            throw new RuntimeException("Test run has been unexpectedly aborted: " + e.getMessage(), e.getCause());
        }
    }
}
