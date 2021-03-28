package com.asteroid.duck.pointy.indexer.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PixelIteratorTest {
    private final PixelIterator subject = new PixelIterator(TestData.basn2c16png);

    @Test
    void iterating() {
        assertTrue(subject.hasNext());

        for (int i = 0; i < 1024; i++) {
            assertTrue(subject.hasNext(), subject.toString());
            int v = subject.next();
        }

        assertFalse(subject.hasNext());
        assertThrows(NoSuchElementException.class, subject::next);
    }

    @Test
    void size() {
        assertEquals(1024, subject.size());
    }
}