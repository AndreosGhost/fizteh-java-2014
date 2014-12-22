package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml;

import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Utility;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class XMLMaker {
    private static final String LIST_TAG = "list";
    private static final String LIST_ELEMENT = "value";
    private static final String CYCLIC_LINK = "cyclic";
    private static final String NULL_ELEMENT = "null";

    /**
     * Writes some xml.
     * @param writer
     *         where to write.
     * @param object
     *         what to serialize. Can be null.
     * @param tagName
     *         name of the tag. Can be null.
     * @param inline
     *         if this is an attribute or not.
     * @param childName
     *         name of each child node (only for iterables/arrays). Can be null or empty.
     * @param identityMap
     *         map to determine cyclic links and handle them in proper way.
     * @throws javax.xml.stream.XMLStreamException
     * @throws IllegalAccessException
     */
    private static void writeXML(XMLStreamWriter writer,
                                 Object object,
                                 String tagName,
                                 boolean inline,
                                 String childName,
                                 IdentityHashMap<Object, Boolean> identityMap)
            throws XMLStreamException, IllegalAccessException {
        ConditionVerifier inlineUseVerifier = () -> {
            if (inline) {
                throw new IllegalArgumentException("This object cannot be inlined: " + object);
            }
        };

        if (tagName != null && !inline) {
            writer.writeStartElement(tagName);
        }

        if (object == null) {
            writer.writeEmptyElement(NULL_ELEMENT);
        } else {
            Class<?> objClass = object.getClass();

            if (identityMap.containsKey(object)) {
                writer.writeCharacters(CYCLIC_LINK);
            } else {
                identityMap.put(object, Boolean.TRUE);

                if (objClass.getAnnotation(XMLComplexObject.class) != null) {
                    inlineUseVerifier.verify();

                    // Convenience trick.
                    FieldXMLAppender xmlAppender = (field, overrideName) -> {
                        XMLField fieldAnno = field.getAnnotation(XMLField.class);

                        String fieldName = fieldAnno.name();
                        boolean accessible = field.isAccessible();
                        field.setAccessible(true);

                        if (overrideName != null && overrideName.isEmpty()) {
                            if (fieldName.isEmpty()) {
                                overrideName = field.getName();
                            } else {
                                overrideName = fieldName;
                            }
                        }

                        Object fieldValue = field.get(object);
                        if (fieldValue != null
                            || fieldAnno.nullPolicy() == XMLField.NULLPOLICY_FULL_IF_NULL) {
                            writeXML(
                                    writer,
                                    field.get(object),
                                    overrideName,
                                    fieldAnno.inline(),
                                    fieldAnno.childName(),
                                    identityMap);
                        } else if (fieldAnno.nullPolicy() == XMLField.NULLPOLICY_EMPTY_IF_NULL) {
                            writer.writeEmptyElement(overrideName);
                        }
                        field.setAccessible(accessible);
                    };

                    List<Field> annotatedFields = Utility.getAllAnnotatedFields(objClass, XMLField.class);

                    if (annotatedFields.isEmpty()) {
                        throw new IllegalArgumentException(
                                "Class "
                                + objClass.getSimpleName()
                                + " does not have any XML fields and cannot be serialized");
                    } else if (objClass.getAnnotation(XMLComplexObject.class).wrapper()) {
                        if (annotatedFields.size() != 1) {
                            throw new IllegalArgumentException(
                                    "Class "
                                    + objClass.getSimpleName()
                                    + " has more then one XML fields, thus it cannot be annotated as "
                                    + "'wrapper'");
                        }
                        xmlAppender.appendInfo(annotatedFields.get(0), tagName);
                    } else {
                        // Attributes first.
                        for (Field field : annotatedFields) {
                            XMLField fieldAnno = field.getAnnotation(XMLField.class);
                            if (fieldAnno.inline()) {
                                xmlAppender.appendInfo(field);
                            }
                        }

                        // Tags then.
                        for (Field field : annotatedFields) {
                            XMLField fieldAnno = field.getAnnotation(XMLField.class);
                            if (!fieldAnno.inline()) {
                                xmlAppender.appendInfo(field);
                            }
                        }
                    }
                } else if (object instanceof Iterable || objClass.isArray()) {
                    inlineUseVerifier.verify();

                    Iterator<Object> iter = (object instanceof Iterable
                                             ? ((Iterable) object).iterator()
                                             : new Iterator<Object>() {
                                                 final int size = Array.getLength(object);
                                                 int index = 0;

                                                 @Override
                                                 public boolean hasNext() {
                                                     return index < size;
                                                 }

                                                 @Override
                                                 public Object next() {
                                                     if (!hasNext()) {
                                                         throw new NoSuchElementException(
                                                                 "No more elements in the array");
                                                     }
                                                     return Array.get(object, index++);
                                                 }
                                             });

                    if (tagName == null) {
                        writer.writeStartElement(LIST_TAG);
                    }
                    while (iter.hasNext()) {
                        Object next = iter.next();
                        writer.writeStartElement(
                                childName == null || childName.isEmpty() ? LIST_ELEMENT : childName);
                        writeXML(writer, next, null, false, null, identityMap);
                        writer.writeEndElement();
                    }
                    if (tagName == null) {
                        writer.writeEndElement();
                    }
                } else {
                    String value = object.toString();
                    if (inline) {
                        writer.writeAttribute(tagName, value);
                    } else {
                        writer.writeCharacters(value);
                    }
                }

                identityMap.remove(object);
            }
        }

        if (tagName != null && !inline) {
            writer.writeEndElement();
        }
    }

    public static String makeXML(Object object, String tagName) {
        try {
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            StringWriter stringWriter = new StringWriter();
            XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(stringWriter);

            writeXML(xmlWriter, object, tagName, false, null, new IdentityHashMap<>());

            return stringWriter.toString();
        } catch (IllegalAccessException | XMLStreamException exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * Convenience structure used in method {@link #writeXML(javax.xml.stream.XMLStreamWriter, Object,
     * String,
     * boolean, String, java.util.IdentityHashMap)}.
     */
    @FunctionalInterface
    private interface FieldXMLAppender {
        void appendInfo(Field field, String overrideName) throws IllegalAccessException, XMLStreamException;

        default void appendInfo(Field field) throws IllegalAccessException, XMLStreamException {
            appendInfo(field, "");
        }
    }

    @FunctionalInterface
    interface ConditionVerifier {
        void verify() throws RuntimeException;
    }
}
