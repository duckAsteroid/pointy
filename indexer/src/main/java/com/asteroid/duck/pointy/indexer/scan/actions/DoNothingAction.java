package com.asteroid.duck.pointy.indexer.scan.actions;

import java.io.IOException;

public class DoNothingAction extends IndexAction {

    public DoNothingAction(String checksum) {
        super(checksum);
    }

    @Override
    protected void process(IndexActionContext ctx) throws IOException {
        // does nothing
    }
}
