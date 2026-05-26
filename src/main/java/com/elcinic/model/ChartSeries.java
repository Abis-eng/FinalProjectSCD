package com.elcinic.model;

import java.util.ArrayList;
import java.util.List;

public class ChartSeries {
    private final List<String> labels = new ArrayList<>();
    private final List<Number> values = new ArrayList<>();

    public List<String> getLabels() {
        return labels;
    }

    public List<Number> getValues() {
        return values;
    }

    public void add(String label, Number value) {
        labels.add(label);
        values.add(value);
    }
}
