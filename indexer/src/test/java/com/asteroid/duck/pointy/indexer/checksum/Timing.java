package com.asteroid.duck.pointy.indexer.checksum;

public class Timing {
    private long startTime;
    private long created;
    private long checksumComplete;

    public void start() {
        startTime = System.nanoTime();
    }

    public void created() {
        created = System.nanoTime();
    }

    public void checksumComplete() {
        checksumComplete = System.nanoTime();
    }

    public long getCreation() {
        return created - startTime;
    }

    public long getChecksum() {
        return checksumComplete - created;
    }

    @Override
    public String toString() {
        return getCreation()+","+getChecksum();
    }
}
