package com.asteroid.duck.pointy.indexer.image;

import org.junit.jupiter.api.Test;

import static com.asteroid.duck.pointy.indexer.image.TestData.TEST_DATA;
import static org.junit.jupiter.api.Assertions.*;

class ChannelTest {

    @Test
    void extract() {
        TEST_DATA.stream().map(Channel.red::extract).forEach(ch -> System.out.println("R: "+ch));
        TEST_DATA.stream().map(Channel.green::extract).forEach(ch -> System.out.println("G: "+ch));
        TEST_DATA.stream().map(Channel.blue::extract).forEach(ch -> System.out.println("B: "+ch));
        TEST_DATA.stream().map(Channel.alpha::extract).forEach(ch -> System.out.println("a: "+ch));
    }
}