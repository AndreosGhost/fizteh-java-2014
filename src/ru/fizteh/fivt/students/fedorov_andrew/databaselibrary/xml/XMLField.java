package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for fields that must be xml-serialized.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface XMLField {
    String DEFAULT_NAME = "";

    int NULLPOLICY_EMPTY_IF_NULL = 0;
    int NULLPOLICY_IGNORE_IF_NULL = 1;
    int NULLPOLICY_FULL_IF_NULL = 2;

    /**
     * Used for iterables, arrays to specify element tag alternative to 'value'.<br/>
     * If empty, default tag is applied.
     */
    String childName() default DEFAULT_NAME;

    /**
     * Specifies behaviour if element that is going to be serialized is null.<br/>
     * @see #NULLPOLICY_EMPTY_IF_NULL
     * @see #NULLPOLICY_FULL_IF_NULL
     * @see #NULLPOLICY_IGNORE_IF_NULL
     */
    int nullPolicy() default NULLPOLICY_FULL_IF_NULL;

    /**
     * Name of tag/attribute of this element (replaces declared field's name, if not empty).
     */
    String name() default DEFAULT_NAME;

    /**
     * If true, the value is written as attribute inside host tag.<br/>
     * Otherwise the value is written inside inner node.<br/>
     * If this element is set true for an object that cannot be inlined, exception is raised.
     */
    boolean inline() default false;
}
