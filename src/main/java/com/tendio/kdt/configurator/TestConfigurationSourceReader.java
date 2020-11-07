package com.tendio.kdt.configurator;

import com.tendio.kdt.configurator.model.TestSuite;

public interface TestConfigurationSourceReader {
    TestSuite readTestSuite() throws Exception;
}
