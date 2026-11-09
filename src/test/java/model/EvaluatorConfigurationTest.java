package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import model.exceptions.InvalidExtensionException;
import model.exceptions.InvalidNameException;

public class EvaluatorConfigurationTest {

    private static final String VALID_LAYOUT_FILE = "src/test/resources/azertyLayout.json";
    private static final String INVALID_LAYOUT_FILE = "src/test/resources/azertyLayout.txt";
    private static final String NON_EXISTING_LAYOUT_FILE = "src/test/resources/abc.json";
    private static final String VALID_CONSTANTS_FILE = "src/test/resources/constants.json";
    private static final String INVALID_CONSTANTS_FILE = "src/test/resources/constants.txt";
    private static final String NON_EXISTING_CONSTANTS_FILE = "src/test/resources/abc.json";

    @Test
    public void jsonToHashMapConverter_validFile_shouldReturnHashMap() throws Exception {
        EvaluatorConfiguration config = new EvaluatorConfiguration();
        HashMap<String, Key> result = config.jsonToHashMapConverter(VALID_LAYOUT_FILE);
        assertNotNull(result);
        assertTrue(result.containsKey("a"));
    }

    @Test(expected = InvalidExtensionException.class)
    public void jsonToHashMapConverter_invalidExtension_shouldThrowInvalidExtensionException() throws Exception {
        EvaluatorConfiguration config = new EvaluatorConfiguration();
        config.jsonToHashMapConverter(INVALID_LAYOUT_FILE);
    }

    @Test(expected = InvalidNameException.class)
    public void jsonToHashMapConverter_nonExistingFile_shouldThrowInvalidNameException() throws Exception {
        EvaluatorConfiguration config = new EvaluatorConfiguration();
        config.jsonToHashMapConverter(NON_EXISTING_LAYOUT_FILE);
    }

    @Test
    public void jsonToListConverter_validFile_shouldReturnList() throws Exception {
        EvaluatorConfiguration config = new EvaluatorConfiguration();
        List<Double> result = config.jsonToListConverter(VALID_CONSTANTS_FILE);
        assertNotNull(result);
        assertEquals(15, result.size());
        assertEquals(1.0, result.get(0), 0.001);
    }

    @Test(expected = InvalidExtensionException.class)
    public void jsonToListConverter_invalidExtension_shouldThrowInvalidExtensionException() throws Exception {
        EvaluatorConfiguration config = new EvaluatorConfiguration();
        config.jsonToListConverter(INVALID_CONSTANTS_FILE);
    }

    @Test(expected = InvalidNameException.class)
    public void jsonToListConverter_nonExistingFile_shouldThrowInvalidNameException() throws Exception {
        EvaluatorConfiguration config = new EvaluatorConfiguration();
        config.jsonToListConverter(NON_EXISTING_CONSTANTS_FILE);
    }
}