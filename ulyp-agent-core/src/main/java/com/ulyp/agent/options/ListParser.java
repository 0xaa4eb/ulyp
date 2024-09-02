package com.ulyp.agent.options;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ListParser<T> implements Parser<List<T>> {

    private static final String SEPARATORS = ",";

    private final Parser<T> elementParser;

    public ListParser(Parser<T> elementParser) {
        this.elementParser = elementParser;
    }

    @Override
    public List<T> parse(String text) {
        List<T> result = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(text, SEPARATORS, false);
        while (stringTokenizer.hasMoreTokens()) {
            String element = stringTokenizer.nextToken();
            result.add(elementParser.parse(element));
        }
        return result;
    }
}
