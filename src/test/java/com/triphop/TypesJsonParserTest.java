package com.triphop;

import com.fasterxml.jackson.core.JsonFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class TypesJsonParserTest {

    private JsonFactory factory;
    private ClassLoader classLoader;
    private TypesJsonParser typesJsonParser;

    @Before
    public void setUp() throws Exception {
        //JsonFactory is heavy weight object has to be reused.
        factory = new JsonFactory();
        classLoader = ClassLoader.getSystemClassLoader();
        typesJsonParser = TypesJsonParser.of(factory);
    }

    @Test
    public void givenSampleJsonWhenParseThenMapShouldContain7Types() throws Exception {
        //Given
        final File sampleFile = getFile("sample.json");
        Map<String, String> allTypesAndCurrentValues = new HashMap<>();

        //When
        typesJsonParser.parse(sampleFile, allTypesAndCurrentValues);

        //Then
        verify7TypesOfSample(allTypesAndCurrentValues);
    }

    @Test
    public void givenReorderedSampleJsonWhenParseThenMapShouldContain7Types() throws Exception {
        //Given
        final File reorderedSampleFile = getFile("reordered-sample.json");
        Map<String, String> allTypesAndCurrentValues = new HashMap<>();

        //When
        typesJsonParser.parse(reorderedSampleFile, allTypesAndCurrentValues);

        //Then
        verify7TypesOfSample(allTypesAndCurrentValues);
    }

    @Test
    public void givenEmptyJsonWhenParseThenMapBeEmpty() throws Exception {
        //Given
        final File test1File = getFile("test1.json");
        Map<String, String> allTypesAndCurrentValues = new HashMap<>();

        //When
        typesJsonParser.parse(test1File, allTypesAndCurrentValues);

        //Then
        assertEquals(0, allTypesAndCurrentValues.size());
    }

    @Test
    public void givenNestedArraysAndObjectsJsonWhenParseThenMapShouldContain() throws Exception {
        //Given
        final File test2File = getFile("test2.json");
        Map<String, String> allTypesAndCurrentValues = new HashMap<>();

        //When
        typesJsonParser.parse(test2File, allTypesAndCurrentValues);

        //Then
        assertEquals("5", allTypesAndCurrentValues.get("array[0].type"));
        assertEquals("Type F", allTypesAndCurrentValues.get("array[1].type"));
        assertEquals("Type A", allTypesAndCurrentValues.get("array[1].object.array2[2].null[0].type"));
        assertEquals("Type B", allTypesAndCurrentValues.get("array[1].object.array2[2].null[1].type"));
        assertEquals("Type C", allTypesAndCurrentValues.get("array[1].object.array2[3].null[0].type"));
        assertEquals("Type D", allTypesAndCurrentValues.get("array[1].object.array2[3].null[1].type"));
    }

    private void verify7TypesOfSample(Map<String, String> allTypesAndCurrentValues) {
        assertEquals("Type A", allTypesAndCurrentValues.get("type"));
        assertEquals("Type B", allTypesAndCurrentValues.get("array[0].type"));
        assertEquals("Type F", allTypesAndCurrentValues.get("array[1].type"));
        assertEquals("Type C", allTypesAndCurrentValues.get("obj.type"));
        assertEquals("Type D", allTypesAndCurrentValues.get("obj.childObj.type"));
        assertEquals("Hello World", allTypesAndCurrentValues.get("obj.childObj.childArr[0].type"));
        assertEquals("End of chain", allTypesAndCurrentValues.get("obj.childObj.childArr[2].type"));
    }

    private File getFile(String s) throws URISyntaxException {
        final URL url = Objects.requireNonNull(classLoader.getResource(s));
        return new File(url.toURI());
    }
}
