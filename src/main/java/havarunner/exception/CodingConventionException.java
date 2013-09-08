package havarunner.exception;

import havarunner.HavaRunner;

public class CodingConventionException extends RuntimeException {
    CodingConventionException(String messageAboutParticularCodingConventionViolation) {
        super(
            String.format(
                "%s (%s is strict about coding conventions, because its authors believe they help writing better software.)",
                messageAboutParticularCodingConventionViolation, HavaRunner.class.getSimpleName()
            )
        );
    }
}
