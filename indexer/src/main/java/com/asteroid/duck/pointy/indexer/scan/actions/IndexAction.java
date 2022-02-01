package com.asteroid.duck.pointy.indexer.scan.actions;

import io.github.duckasteroid.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Some action that must be performed to bring the index into sync with the filesystem
 */
public abstract class IndexAction {
    private static final Logger LOG = LoggerFactory.getLogger(IndexAction.class);

    protected final String checksum;

    protected IndexAction(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }

    public void safeProcess(IndexActionContext ctx, ProgressMonitor monitor) {
        try {
            process(ctx, monitor);
        }
        catch(Throwable e) {
            LOG.error("Error processing", e);
        }
    }

    protected abstract void process(IndexActionContext ctx, ProgressMonitor monitor) throws IOException;

    public String getName() {
        return checksum + " " + getTaskName();
    }

	protected abstract String getTaskName();
}
