package com.tendio.kdt.configurator.model;

import java.time.Duration;

public interface ExecutableTestEntity extends Runnable {
    void execute();

    default void run() {
        execute();
    }

    Duration calculateDuration();

}
