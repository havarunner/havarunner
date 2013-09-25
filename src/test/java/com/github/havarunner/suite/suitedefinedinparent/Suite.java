package com.github.havarunner.suite.suitedefinedinparent;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.HavaRunnerSuite;
import org.junit.runner.RunWith;

class Suite implements HavaRunnerSuite<String> {
    public String suiteObject() {
        return "hello suite";
    }

    public void afterSuite() {
    }
}
