package org.mybatis.generator.internal;

import org.mybatis.generator.api.ProgressCallback;

public class NullProgressCallback implements ProgressCallback {
    public NullProgressCallback() {
    }

    @Override
    public void generationStarted(int totalTasks) {
    }

    @Override
    public void introspectionStarted(int totalTasks) {
    }

    @Override
    public void saveStarted(int totalTasks) {
    }

    @Override
    public void startTask(String taskName) {
    }

    @Override
    public void checkCancel() throws InterruptedException {
    }

    @Override
    public void done() {
    }
}