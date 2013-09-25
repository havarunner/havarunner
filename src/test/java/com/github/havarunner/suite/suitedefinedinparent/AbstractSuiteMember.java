package com.github.havarunner.suite.suitedefinedinparent;

import com.github.havarunner.annotation.PartOf;
import org.junit.Test;

@PartOf(Suite.class)
abstract class AbstractSuiteMember {

    @Test
    void hello() {

    }
}
