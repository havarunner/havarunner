package havarunner;

import com.google.common.base.Optional;
import havarunner.exception.CamelCasedException;
import havarunner.exception.CodingConventionException;
import havarunner.exception.MemberIsNotPackagePrivateException;
import havarunner.exception.UnsupportedAnnotationException;
import havarunner.scenario.FrameworkMethodAndScenario;
import org.junit.*;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class Ensure {

    static Optional<CodingConventionException> violatesCodingConventions(
        FrameworkMethodAndScenario frameworkMethodAndScenario,
        TestClass testClass
    ) {
        try {
            ensuringSnakeCased(frameworkMethodAndScenario.getFrameworkMethod());
            ensuringPackagePrivate(frameworkMethodAndScenario.getFrameworkMethod());
            ensuringValidTestClass(testClass);
            return Optional.absent();
        } catch (CodingConventionException e) {
            return Optional.of(e);
        }
    }

    private static FrameworkMethod ensuringSnakeCased(FrameworkMethod frameworkMethod) {
        if (hasInvalidMethodName(frameworkMethod)) {
            throw new CamelCasedException(String.format(
                "Example %s is camed-cased. Please use_snake_cased_example_names.",
                frameworkMethod.getName()
            ));
        }
        return frameworkMethod;
    }

    private static FrameworkMethod ensuringPackagePrivate(FrameworkMethod frameworkMethod) {
        if (isNotPackagePrivate(frameworkMethod.getMethod())) {
            throw new MemberIsNotPackagePrivateException(frameworkMethod.getMethod());
        }
        return frameworkMethod;
    }

    private static TestClass ensuringValidTestClass(TestClass testClass) {
        return ensureDoesNotHaveUnsupportedJUnitAnnotations(testClass);
    }

    private static TestClass ensureDoesNotHaveUnsupportedJUnitAnnotations(TestClass testClass) {
        List<Class<? extends Annotation>> unsupportedJUnitAnnotations = new ArrayList<Class<? extends Annotation>>() {{
            add(BeforeClass.class);
            add(After.class);
            add(AfterClass.class);
            add(Rule.class);
            add(ClassRule.class);
            add(FixMethodOrder.class);
        }};
        for (Class<? extends Annotation> unsupportedJUnitAnnotation : unsupportedJUnitAnnotations) {
            for (Annotation classAnnotation : testClass.getAnnotations()) { // Check class annotations
                if (unsupportedJUnitAnnotation.equals(classAnnotation.getClass())) {
                    throw new UnsupportedAnnotationException(classAnnotation, testClass);
                }
            }
            if (!testClass.getAnnotatedMethods(unsupportedJUnitAnnotation).isEmpty()) {  // Check method annotations
                throw new UnsupportedAnnotationException(unsupportedJUnitAnnotation, testClass);
            }
        }
        return testClass;
    }

    private static boolean hasInvalidMethodName(FrameworkMethod frameworkMethod) {
        String methodName = frameworkMethod.getMethod().getName();
        return methodName.matches(".*[a-z][A-Z].*") && !methodName.contains("_");
    }

    private static boolean isNotPackagePrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers()) ||
            Modifier.isPublic(member.getModifiers()) ||
            Modifier.isProtected(member.getModifiers());

    }
}
