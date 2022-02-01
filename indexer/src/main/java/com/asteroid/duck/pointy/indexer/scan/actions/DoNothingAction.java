package com.asteroid.duck.pointy.indexer.scan.actions;

import io.github.duckasteroid.progress.ProgressMonitor;

import java.io.IOException;

/**
 * An action that means we have nothing to do
 */
public class DoNothingAction extends IndexAction {

    public DoNothingAction(String checksum) {
        super(checksum);
    }

    @Override
    protected void process(IndexActionContext ctx, ProgressMonitor monitor) throws IOException {
        // does nothing
        monitor.done();
    }

    @Override
    public String getTaskName() {
        return "no-op";
    }
}
