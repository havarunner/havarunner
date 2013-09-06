package havarunner;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static havarunner.Helpers.newTestClassInstance;
import static havarunner.Helpers.toFrameworkMethods;

public class HavaRunner extends ParentRunner<FrameworkMethod> {
    final ExecutorService executor = Executors.newCachedThreadPool();

    public HavaRunner(Class testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        return toFrameworkMethods(getTestClass());
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        return Description.createTestDescription(getTestClass().getJavaClass(), method.getName());
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        final Description description = describeChild(method);
        if (method.getAnnotation(Ignore.class) != null) {
            notifier.fireTestIgnored(description);
        } else {
            executor.submit(new Runnable() {
                public void run() {
                    runLeaf(toStatement(method, newTestClassInstance(getTestClass())), description, notifier);
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

    private Statement toStatement(final FrameworkMethod method, final Object testClassInstance) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                method.invokeExplosively(testClassInstance);
            }
        };
    }
}
