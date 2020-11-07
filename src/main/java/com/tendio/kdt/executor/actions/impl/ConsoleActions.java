package com.tendio.kdt.executor.actions.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.tendio.kdt.executor.actions.annotation.ActionClass;
import com.tendio.kdt.executor.actions.annotation.ActionDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

@ActionClass
public class ConsoleActions extends CommonActions {
    private static final int JSCH_EXCEPTION_EXIT_CODE = 666666;
    private static final int DEFAULT_PORT = 22;

    @ActionDefinition("Execute remote command \"Command\" on host \"Host\" under user \"User\" with password \"Password\"")
    public ConsoleActionOutput executeRemoteCommand(String command, String host, String user, String password) throws IOException {

        JSch jsch = new JSch();
        try {
            Session session = jsch.getSession(user, host, DEFAULT_PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            InputStream in = channelExec.getInputStream();
            channelExec.setCommand(command);
            channelExec.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
            String executionOutput = getOutputString(reader);

            int exitCode = channelExec.getExitStatus();
            channelExec.disconnect();
            session.disconnect();

            return new ConsoleActionOutput(executionOutput, exitCode);
        } catch (JSchException e) {
            return new ConsoleActionOutput(e.toString(), JSCH_EXCEPTION_EXIT_CODE);
        }
    }

    @ActionDefinition("Execute batch command \"Command\" locally")
    public ConsoleActionOutput executeLocalCommand(String[] commandLine) {
        final String filePrefix = "file=";
/*        if (commandLine[0].startsWith(filePrefix)) {
            File file = new File(commandLine[0].replace(filePrefix, ""));
            commandLine[0] = FileUtils.readFileToString(file, Charset.defaultCharset());
        }*/

        try {
            Process process = Runtime.getRuntime().exec(commandLine);
            int exitCode = process.waitFor();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.defaultCharset()));
            String executionOutput = getOutputString(reader);
            process.destroy();

            return new ConsoleActionOutput(executionOutput, exitCode);
        } catch (InterruptedException | IOException e) {
            return new ConsoleActionOutput(e.toString(), JSCH_EXCEPTION_EXIT_CODE);
        }
    }

    private String getOutputString(BufferedReader reader) throws IOException {
        StringBuilder executionOutputBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            //adding line separator starting from 2nd line
            if (executionOutputBuilder.length() != 0) {
                executionOutputBuilder.append(System.lineSeparator());
            }
            executionOutputBuilder.append(line);
        }
        reader.close();
        return executionOutputBuilder.toString();
    }

    public static class ConsoleActionOutput {
        private final String executionOutput;
        private final int exitCode;

        private ConsoleActionOutput(String executionOutput, int exitCode) {
            this.executionOutput = executionOutput;
            this.exitCode = exitCode;
        }

        public String getExecutionOutput() {
            return executionOutput;
        }

        public int getExitCode() {
            return exitCode;
        }

        public boolean isExecutedSuccessfully() {
            return exitCode == 0;
        }

        public boolean isJschExceptionThrown() {
            return exitCode == JSCH_EXCEPTION_EXIT_CODE;
        }
    }
}
