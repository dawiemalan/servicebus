package io.dmcs.test;

import io.micronaut.runtime.Micronaut;

;

public class TestApplication {

    static void main(String[] args) {

        Micronaut.build(args)
                .packages("io.dmcs")
                .banner(false)
                .mainClass(TestApplication.class)
                .start();
    }
}
