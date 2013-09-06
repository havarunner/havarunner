package havarunner;

import havarunner.exception.CamelCasedException;
import havarunner.exception.MemberIsNotPackagePrivateException;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

class Ensure {
    static FrameworkMethod ensuringSnakeCased(FrameworkMethod frameworkMethod) {
        if (hasInvalidMethodName(frameworkMethod)) {
            throw new CamelCasedException(String.format(
                "Example %s is camed-cased. Please use_snake_cased_example_names.",
                frameworkMethod.getName()
            ));
        }
        return frameworkMethod;
    }

    static FrameworkMethod ensuringPackagePrivate(FrameworkMethod frameworkMethod) {
        if (isNotPackagePrivate(frameworkMethod.getMethod())) {
            throw new MemberIsNotPackagePrivateException(frameworkMethod.getMethod());
        }
        return frameworkMethod;
    }

    static Constructor ensuringPackagePrivate(Constructor constructor) {
        if (isNotPackagePrivate(constructor)) {
            throw new MemberIsNotPackagePrivateException(constructor);
        }
        return constructor;
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
