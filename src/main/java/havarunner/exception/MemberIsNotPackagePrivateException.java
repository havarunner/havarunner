package havarunner.exception;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public class MemberIsNotPackagePrivateException extends CodingConventionException {
    public MemberIsNotPackagePrivateException(Member member) {
        super(String.format(
            "%s is %s. Please make it package private.",
            member.getName(),
            modifierToString(member)
        ));
    }

    private static String modifierToString(Member member) {
        return Modifier.isPrivate(member.getModifiers()) ? "private" :
            (Modifier.isPublic(member.getModifiers()) ? "public" : "protected");
    }
}
