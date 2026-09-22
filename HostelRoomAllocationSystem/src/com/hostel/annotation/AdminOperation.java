package com.hostel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom annotation that marks a method as an operation only an
 * administrator is allowed to perform (add / update / delete / allocate).
 *
 * An annotation is METADATA: extra information attached to code.
 * It does not change what the method does. It describes the method.
 *
 * @Target(ElementType.METHOD) -> this annotation can only be put on methods.
 * @Retention(RetentionPolicy.RUNTIME) -> the information survives into the
 *         running program, so a tool could read it later. (COMPILE-time
 *         retention would throw it away after compiling.)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOperation {

    /** Short description of what the admin operation does. */
    String value() default "Admin only operation";

    /** Whether the operation changes data in the database. */
    boolean modifiesData() default true;
}
