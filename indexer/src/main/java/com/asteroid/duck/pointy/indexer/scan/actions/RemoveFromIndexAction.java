package com.asteroid.duck.pointy.indexer.scan.actions;

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
    protected void process(IndexContext ctx) throws IOException {
        ctx.getWriter().deleteDocuments(getDocumentID());
        // delete the folder of images too
        Path slideFolder = ctx.getConfig().getSlideFolder(checksum);
        FileUtils.deleteDirectory(slideFolder.toFile());
    }
}
