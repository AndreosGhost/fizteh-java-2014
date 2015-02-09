package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml.XMLComplexObject;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml.XMLField;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.xml.XMLMaker;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class XMLTest {
    @Test
    public void testCyclicLinks() {
        MyObject5 objA = new MyObject5();
        MyObject6 objB = new MyObject6();
        objB.a = objA;
        objA.b = objB;

        assertEquals("<obj5><b><a>cyclic</a></b></obj5>", XMLMaker.makeXML(objA, "obj5"));
    }

    @Test
    public void testSimpleTag() {
        assertEquals("<obj><field1>1323</field1></obj>", XMLMaker.makeXML(new MyObject1(), "obj"));
    }

    @Test
    public void testRenamedField() {
        assertEquals("<obj><value>123</value></obj>", XMLMaker.makeXML(new MyObject2(), "obj"));
    }

    @Test
    public void testInlineAttribute() {
        assertEquals("<obj tag=\"true\"></obj>", XMLMaker.makeXML(new MyObject3(), "obj"));
    }

    @Test
    public void testArray() {
        assertEquals(
                "<obj><objects><value>true</value><value>false</value><value>String</value></objects></obj>",
                XMLMaker.makeXML(new MyObject4(), "obj"));
    }

    @XMLComplexObject
    class MyObject1 {
        @XMLField
        private String field1 = "1323";
    }

    @XMLComplexObject
    class MyObject2 {
        @XMLField(name = "value")
        private int field2 = 123;
    }

    @XMLComplexObject
    class MyObject3 {
        @XMLField(inline = true)
        private boolean tag = true;
    }

    @XMLComplexObject
    class MyObject4 {
        @XMLField
        private Object[] objects = new Object[] {"true", "false", "String"};
    }

    @XMLComplexObject
    class MyObject5 {
        @XMLField
        MyObject6 b;
    }

    @XMLComplexObject
    class MyObject6 {
        @XMLField
        MyObject5 a;
    }
}
