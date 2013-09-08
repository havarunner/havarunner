package havarunner;

import com.google.common.base.Optional;
import havarunner.exception.CodingConventionException;
import havarunner.scenario.TestParameters;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static havarunner.Ensure.violatesCodingConventions;
import static havarunner.Helper.*;
import static havarunner.scenario.ScenarioHelper.addScenarioInterceptor;

public class HavaRunner extends Runner {
    final ExecutorService executor = new ThreadPoolExecutor(
        0, Runtime.getRuntime().availableProcessors() * 3,
        60L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final Collection<Class> classesToTest;
    private final Class parentClass;

    public HavaRunner(Class parentClass) throws InitializationError {
        this.parentClass = parentClass;
        classesToTest = new ArrayList<>();
        Collections.addAll(classesToTest, parentClass.getDeclaredClasses());
        classesToTest.add(parentClass);
    }

    protected List<TestParameters> getChildren() {
        return toTestParameters(classesToTest);
    }

    protected Description describeChild(TestParameters testParameters) {
        return Description.createTestDescription(
            testParameters.getTestClass().getJavaClass(),
            testParameters.getFrameworkMethod().getName() + testParameters.scenarioToString()
        );
    }

    protected void runChild(final TestParameters testParameters, final RunNotifier notifier) {
        final Description description = describeChild(testParameters);
        Optional<CodingConventionException> codingConventionException = violatesCodingConventions(
            testParameters,
            testParameters.getTestClass()
        );
        if (codingConventionException.isPresent()) {
            notifier.fireTestAssumptionFailed(new Failure(description, codingConventionException.get()));
        } else {
            runValidTest(testParameters, notifier, description);
        }
    }

    private void runValidTest(final TestParameters testParameters, final RunNotifier notifier, final Description description) {
        if (testParameters.getFrameworkMethod().getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            executor.submit(new Runnable() {
                public void run() {
                    runLeaf(
                        toStatement(
                            testParameters,
                            newTestClassInstance(testParameters.getTestClass())
                        ),
                        description,
                        notifier
                    );
                }
            });
        }
    }

    private void runLeaf(Statement statement, Description description, RunNotifier notifier) {
        EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
        eachNotifier.fireTestStarted();
        try {
            statement.evaluate();
        } catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        } catch (Throwable e) {
            eachNotifier.addFailure(e);
        } finally {
            eachNotifier.fireTestFinished();
        }
    }


    @Override
    public Description getDescription() {
        Description description = Description.createSuiteDescription(parentClass);
        for (TestParameters child : getChildren()) {
            description.addChild(describeChild(child));
        }
        return description;
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            for (TestParameters testParameters : getChildren()) {
                runChild(testParameters, notifier);
            }
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Statement toStatement(final TestParameters testParameters, final Object testClassInstance) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                withExpectedExceptionTolerance(
                    createTestInvokingStatement()
                ).evaluate();
            }

            private Statement createTestInvokingStatement() throws Throwable {
                if (isScenarioClass(testClassInstance.getClass())) {
                    return addScenarioInterceptor(testParameters, testClassInstance);
                } else {
                    return new Statement() {
                        @Override
                        public void evaluate() throws Throwable {
                            for (Method before : testParameters.getBefores()) {
                                before.setAccessible(true);
                                before.invoke(testClassInstance);
                            }
                            testParameters.getFrameworkMethod().invokeExplosively(testClassInstance);
                        }
                    };
                }
            }

            private Statement withExpectedExceptionTolerance(final Statement testInvokingStatement) {
                return new Statement() {
                    @Override
                    public void evaluate() throws Throwable {
                        try {
                            testInvokingStatement.evaluate();
                        } catch (Throwable exceptionWhileRunningTest) {
                            Test annotation = testParameters.getFrameworkMethod().getAnnotation(Test.class);
                            Class<? extends Throwable> expectedException = annotation.expected();
                            if (!expectedException.isAssignableFrom(exceptionWhileRunningTest.getClass())) {
                                throw exceptionWhileRunningTest;
                            }
                        }
                    }
                };
            }
        };
    }
}
