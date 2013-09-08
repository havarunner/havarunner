package havarunner;

import com.google.common.base.Optional;
import havarunner.exception.CodingConventionException;
import havarunner.scenario.FrameworkMethodAndScenario;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.*;

import static havarunner.Ensure.violatesCodingConventions;
import static havarunner.Helper.*;
import static havarunner.scenario.ScenarioHelper.addScenarioInterceptor;

public class HavaRunner extends ParentRunner<FrameworkMethodAndScenario> {
    final ExecutorService executor = new ThreadPoolExecutor(
        0, Runtime.getRuntime().availableProcessors() * 3,
        60L, TimeUnit.SECONDS,
        new SynchronousQueue<Runnable>(),
        new ThreadPoolExecutor.CallerRunsPolicy()
    );

    public HavaRunner(Class testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<FrameworkMethodAndScenario> getChildren() {
        return toFrameworkMethods(getTestClass());
    }

    @Override
    protected Description describeChild(FrameworkMethodAndScenario frameworkMethodAndScenario) {
        return Description.createTestDescription(
            getTestClass().getJavaClass(),
            frameworkMethodAndScenario.getFrameworkMethod().getName() + frameworkMethodAndScenario.scenarioToString()
        );
    }

    @Override
    protected void runChild(final FrameworkMethodAndScenario frameworkMethodAndScenario, final RunNotifier notifier) {
        final Description description = describeChild(frameworkMethodAndScenario);
        Optional<CodingConventionException> codingConventionException = violatesCodingConventions(
            frameworkMethodAndScenario,
            getTestClass()
        );
        if (codingConventionException.isPresent()) {
            notifier.fireTestAssumptionFailed(new Failure(description, codingConventionException.get()));
        } else {
            runValidTest(frameworkMethodAndScenario, notifier, description);
        }
    }

    private void runValidTest(final FrameworkMethodAndScenario frameworkMethodAndScenario, final RunNotifier notifier, final Description description) {
        if (frameworkMethodAndScenario.getFrameworkMethod().getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            executor.submit(new Runnable() {
                public void run() {
                    runLeaf(
                        toStatement(
                            frameworkMethodAndScenario,
                            newTestClassInstance(getTestClass())
                        ),
                        description,
                        notifier
                    );
                }
            });
        }
    }


    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
        try {
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Statement toStatement(final FrameworkMethodAndScenario frameworkMethodAndScenario, final Object testClassInstance) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                withExpectedExceptionTolerance(
                    createTestInvokingStatement()
                ).evaluate();
            }

            private Statement createTestInvokingStatement() throws Throwable {
                if (isScenarioClass(testClassInstance.getClass())) {
                    return addScenarioInterceptor(frameworkMethodAndScenario, testClassInstance);
                } else {
                    return new Statement() {
                        @Override
                        public void evaluate() throws Throwable {
                            frameworkMethodAndScenario.getFrameworkMethod().invokeExplosively(testClassInstance);
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
                            Test annotation = frameworkMethodAndScenario.getFrameworkMethod().getAnnotation(Test.class);
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
