package com.on.eye.api.location.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.lang.NonNull;

public class Locations implements Iterable<Location> {
    private final List<Location> items = new ArrayList<>();

    public void add(Location location) {
        items.add(location);
    }

    @Override
    @NonNull
    public Iterator<Location> iterator() {
        return items.iterator();
    }
}
