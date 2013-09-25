package com.github.havarunner.suite.suitedefinedinparent;

import com.github.havarunner.HavaRunnerSuite;

class Suite implements HavaRunnerSuite<String> {
    public String suiteObject() {
        return "hello suite";
    }

    public void afterSuite() {
    }
}
