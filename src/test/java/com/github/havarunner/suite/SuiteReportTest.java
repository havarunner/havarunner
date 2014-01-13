package com.github.havarunner.suite;

import com.github.havarunner.HavaRunner;
import com.github.havarunner.RunnerHelper;
import com.github.havarunner.enclosed.nonstatic.SuiteWithMemberThatHasScenarios;
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

    @Test
    public void HavaRunner_should_report_non_static_inner_classes_as_suite_members() {
        List<String> reports = newArrayList(RunnerHelper.reportIfSuite(new HavaRunner(SuiteWithMemberThatHasScenarios.class).tests()));
        assertTrue(reports.contains("[HavaRunner] Running SuiteMemberWithScenarios#outerTest (when 1) as a part of SuiteWithMemberThatHasScenarios"));
        assertTrue(reports.contains("[HavaRunner] Running SuiteMemberWithScenarios#outerTest (when 2) as a part of SuiteWithMemberThatHasScenarios"));
        assertTrue(reports.contains("[HavaRunner] Running InnerSuiteClass#innerTest (when 1) as a part of SuiteWithMemberThatHasScenarios"));
        assertTrue(reports.contains("[HavaRunner] Running InnerSuiteClass#innerTest (when 2) as a part of SuiteWithMemberThatHasScenarios"));
    }
}
