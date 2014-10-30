package ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.test.support;

import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.db.DBTableProviderFactory;
import ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Utility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * This class provides some utility methods for testing.<br/> Note that some methods are linking to
 * {@link ru.fizteh.fivt.students.fedorov_andrew.databaselibrary.support.Utility} class. The
 * purpose
 * of this is that test classes should not contact with something that is not under test package.
 */
public class TestUtils {
    private static final char[] ALPHABET = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static Random random = new Random();

    // Not for constructing
    private TestUtils() {
    }

    public static int randInt(int a, int b) {
        return random.nextInt(b - a + 1) + a;
    }

    public static int randInt(int n) {
        return random.nextInt(n);
    }

    public static String randString(int length) {
        char[] data = new char[length];
        for (int i = 0; i < length; i++) {
            data[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return String.valueOf(data);
    }

    public static TableProviderFactory obtainFactory() {
        return new DBTableProviderFactory();
    }

    public static void removeFileSubtree(Path removePath) throws IOException {
        Utility.rm(removePath, "JUnit Test");
    }

    public static <T> T randElement(Collection<T> set) {
        int keysCount = set.size();
        if (keysCount == 0) {
            return null;
        }
        int keyID = TestUtils.randInt(keysCount);

        Iterator<T> iterator = set.iterator();
        while (keyID > 0) {
            iterator.next();
            keyID--;
        }

        T element = iterator.next();
        return element;
    }

    public static <K, V> int countDifferences(Map<K, V> source, Map<K, V> target) {
        return Utility.countDifferences(source, target);
    }
}