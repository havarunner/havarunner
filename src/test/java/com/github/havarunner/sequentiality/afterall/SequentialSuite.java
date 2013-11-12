package com.github.havarunner.sequentiality.afterall;

import com.github.havarunner.HavaRunnerSuite;
import com.github.havarunner.annotation.RunSequentially;

@RunSequentially
class SequentialSuite implements HavaRunnerSuite<String> {

    public String suiteObject() {
        return "hi";
    }

    public void afterSuite() {

    }
}
