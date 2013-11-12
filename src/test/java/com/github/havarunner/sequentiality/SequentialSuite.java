package com.github.havarunner.sequentiality;

import com.github.havarunner.HavaRunnerSuite;
import com.github.havarunner.annotation.RunSequentially;

@RunSequentially(because = "tests in this suite do not thrive in the concurrent world")
class SequentialSuite implements HavaRunnerSuite<String> {
    public String suiteObject() {
        return "hello";
    }

    public void afterSuite() {
    }
}
