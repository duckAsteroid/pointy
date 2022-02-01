package com.asteroid.duck.pointy.indexer.scan.actions;

import io.github.duckasteroid.progress.ProgressMonitor;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Action to remove (purge) a file from the index
 */
public class RemoveFromIndexAction extends IndexAction {

    public RemoveFromIndexAction(String checksum) {
        super(checksum);
    }

    @Override
    protected void process(IndexActionContext ctx, ProgressMonitor monitor) throws IOException {
        monitor.setSize(2);
        ctx.delete(getChecksum());
        monitor.worked(1, "Deleted document from index");
        // delete the folder of images too
        Path slideFolder = ctx.getConfig().getSlideFolder(checksum);
        FileUtils.deleteDirectory(slideFolder.toFile());
        monitor.worked(1, "Removed thumbnail images");
        monitor.done();
    }

    @Override
    public String getTaskName() {
        return "remove";
    }
}
