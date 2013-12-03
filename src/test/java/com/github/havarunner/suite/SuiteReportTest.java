package com.github.havarunner.suite;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.RunnerHelper;
import org.junit.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;

public class SuiteReportTest {

    @Test
    public void HavaRunner_should_report_suite_memberships_to_stdout() {
        List<String> reports = newArrayList(RunnerHelper.reportIfSuite(new HavaRunner(ExampleSuite.class).tests()));
        assertTrue(reports.contains("[HavaRunner] Running SuiteMember1#test as a part of ExampleSuite"));
    }
}
