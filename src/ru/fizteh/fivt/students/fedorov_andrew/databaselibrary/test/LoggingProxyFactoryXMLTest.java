package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.junit.Test;
import ru.fizteh.fivt.proxy.LoggingProxyFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.LoggingProxyFactoryXML;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class LoggingProxyFactoryXMLTest extends LoggingProxyFactoryTestBase {
    private static final String NEW_LINE = System.lineSeparator();

    @Override
    protected LoggingProxyFactory obtainFreshFactory() {
        return new LoggingProxyFactoryXML();
    }

    @Test
    public void testDoNothingJSON() throws ParseException, XMLStreamException {
        wrapped.doNothing();
        XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(
                new StringReader(getOutput()));

        reader.nextTag();

        assertThat(
                getOutput(), allOf(
                        startsWith("<invoke timestamp=\""), endsWith(
                                "\" class=\"TestFaceImpl\" name=\"doNothing\"><arguments/></invoke>"
                                + NEW_LINE)));
    }

    @Test
    public void testBoolMethodJSON() throws ParseException {
        wrapped.boolMethod(1, null);
        assertThat(
                getOutput(), allOf(
                        startsWith("<invoke timestamp=\""), endsWith(
                                "\" class=\"TestFaceImpl\" "
                                + "name=\"boolMethod\"><arguments><argument>1</argument><argument><null"
                                + "/></argument></arguments><return>true</return></invoke>"
                                + NEW_LINE)));
    }

    @Test
    public void testThrowingMethodJSON() throws ParseException {
        List<List<String>> iterable = new LinkedList<>();
        iterable.add(Arrays.asList("1_1", "1_2", "1_3"));
        iterable.add(Arrays.asList("2_1", "2_2"));
        iterable.add(null);

        try {
            wrapped.throwingMethod(iterable);
        } catch (Exception exc) {
            // Ignore it.
        }
        assertThat(
                getOutput(), allOf(
                        startsWith(
                                "<invoke timestamp=\""), endsWith(
                                "\" class=\"TestFaceImpl\" "
                                + "name=\"throwingMethod\"><arguments><argument><list><value><list><value"
                                + ">1_1</value><value>1_2</value><value>1_3</value></list></value><value"
                                + "><list><value>2_1</value><value>2_2</value></list></value><value><null"
                                + "/></value></list></argument></arguments><thrown>java.lang"
                                + ".Exception</thrown></invoke>"
                                + NEW_LINE)));
    }

    @Test
    public void testEqualsNotProxiedJSON() {
        wrapped.equals(null);
        assertEquals("", getOutput());
    }
}
