package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for objects that contain some fields that must be xml-serialized.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target(ElementType.TYPE)
public @interface XMLComplexObject {
    /**
     * If true, object's single field is written directly skipping this object's serialization.
     */
    boolean wrapper() default false;
}
