package havarunner.exception;

import java.lang.annotation.Annotation;

public class UnsupportedAnnotationException extends RuntimeException {
    public UnsupportedAnnotationException(Class<? extends Annotation> annotation, Object annotationUser) {
        super(String.format(
            "%s uses the unsupported annotation %s",
            annotationUser,
            annotation
        ));
    }

    public UnsupportedAnnotationException(Annotation annotation, Object annotationUser) {
        this(annotation.getClass(), annotationUser);
    }
}
