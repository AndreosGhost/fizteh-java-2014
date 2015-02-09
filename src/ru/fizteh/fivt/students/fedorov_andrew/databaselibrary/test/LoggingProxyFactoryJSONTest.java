package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.fizteh.fivt.proxy.LoggingProxyFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.json.JSONMaker;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.json.JSONParsedObject;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.json.JSONParser;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.LoggingProxyFactoryJSON;

import java.text.ParseException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class LoggingProxyFactoryJSONTest extends LoggingProxyFactoryTestBase {

    /**
     * Matcher that always returns true.
     */
    private static final Matcher<Object> EXISTS_MATCHER = new BaseMatcher<Object>() {
        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("exists");
        }
    };
    /**
     * Requires presence of some sections and forbids situation when 'thrown' and 'returnValue" exist both.
     */
    private static final Matcher<JSONParsedObject> MIN_REQUIREMENTS = allOf(
            new LogPieceMatcher(TIMESTAMP, EXISTS_MATCHER),
            new LogPieceMatcher(CLASS, EXISTS_MATCHER),
            new LogPieceMatcher(METHOD, EXISTS_MATCHER),
            new LogPieceMatcher(ARGUMENTS, EXISTS_MATCHER),
            not(
                    allOf(
                            new LogPieceMatcher(THROWN, EXISTS_MATCHER),
                            new LogPieceMatcher(RETURN_VALUE, EXISTS_MATCHER))));

    private static Matcher<JSONParsedObject> makeRequirements(Matcher<JSONParsedObject>... restrictions) {
        return allOf(MIN_REQUIREMENTS, allOf(restrictions));
    }

    @Override
    protected LoggingProxyFactory obtainFreshFactory() {
        return new LoggingProxyFactoryJSON();
    }

    @Test
    public void testDoNothingJSON() throws ParseException {
        wrapped.doNothing();
        JSONParsedObject parsed = JSONParser.parseJSON(getOutput());
        assertThat(parsed, makeRequirements());
    }

    @Test
    public void testBoolMethodJSON() throws ParseException {
        wrapped.boolMethod(1, null);
        JSONParsedObject parsedObject = JSONParser.parseJSON(getOutput());
        assertThat(parsedObject, makeRequirements());
        assertThat(parsedObject.getObject(ARGUMENTS).asArray(), equalTo(new Object[] {1L, null}));
        assertThat(parsedObject, new LogPieceMatcher("returnValue", equalTo(Boolean.TRUE)));
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

        JSONParsedObject parsedObject = JSONParser.parseJSON(getOutput());

        assertThat(parsedObject, makeRequirements());
        assertEquals(
                JSONMaker.makeJSON(new Object[] {iterable}), JSONMaker.makeJSON(parsedObject.get(ARGUMENTS)));
    }

    @Test
    public void testEqualsNotProxiedJSON() {
        wrapped.equals(null);
        assertEquals("", getOutput());
    }

    protected static class LogPieceMatcher extends BaseMatcher<JSONParsedObject> {
        private final String fieldName;
        private final Matcher<Object> valueMatcher;

        public LogPieceMatcher(String fieldName, Matcher<Object> valueMatcher) {
            this.fieldName = fieldName;
            this.valueMatcher = valueMatcher;
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof JSONParsedObject) {
                JSONParsedObject obj = (JSONParsedObject) item;
                if (valueMatcher == null) {
                    return !obj.containsField(fieldName);
                } else {
                    return obj.containsField(fieldName) && valueMatcher.matches(obj.get(fieldName));
                }
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Field " + fieldName);
            if (valueMatcher == null) {
                description.appendText(" must not exist.");
            } else {
                description.appendText(" must match: ");
                description.appendDescriptionOf(valueMatcher);
            }
        }
    }
}
