package havarunner;

import havarunner.scenario.FrameworkMethodAndScenario;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static havarunner.Helper.*;
import static havarunner.scenario.ScenarioHelper.addScenarioInterceptorAndRunTest;

public class HavaRunner extends ParentRunner<FrameworkMethodAndScenario> {
    final ExecutorService executor = Executors.newCachedThreadPool();

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
            frameworkMethodAndScenario.getFrameworkMethod().getName() +
                " (when " + frameworkMethodAndScenario.getScenario().toString() + ")"
        );
    }

    @Override
    protected void runChild(final FrameworkMethodAndScenario frameworkMethodAndScenario, final RunNotifier notifier) {
        final Description description = describeChild(frameworkMethodAndScenario);
        if (frameworkMethodAndScenario.getFrameworkMethod().getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            executor.submit(new Runnable() {
                public void run() {
                    runLeaf(
                        toStatement(frameworkMethodAndScenario, newTestClassInstance(getTestClass())),
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
                if (isScenarioClass(testClassInstance.getClass())) {
                    addScenarioInterceptorAndRunTest(frameworkMethodAndScenario, testClassInstance);
                } else {
                    frameworkMethodAndScenario.getFrameworkMethod().invokeExplosively(testClassInstance);
                }
            }
        };
    }

}
