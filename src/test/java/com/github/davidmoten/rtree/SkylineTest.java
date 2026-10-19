package com.github.davidmoten.rtree;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

public class SkylineTest {

    @Test
    public void findsExpectedSkyline() {
        RTree<String, Point> tree = RTree.create();

        tree = tree
                .add("a", Geometries.point(1.0, 9.0))
                .add("b", Geometries.point(2.0, 10.0))
                .add("i", Geometries.point(3.0, 2.0))
                .add("h", Geometries.point(4.0, 3.0))
                .add("k", Geometries.point(9.0, 1.0));

        List<Entry<String, Point>> result = Skyline.compute(tree);

        Set<String> actual = new HashSet<>();

        for (Entry<String, Point> entry : result) {
            actual.add(entry.value());
        }

        Set<String> expected = new HashSet<>(Arrays.asList("a", "i", "k"));
        System.out.println("实际 Skyline：" + actual);
        System.out.println("预期 Skyline：" + expected);

        assertEquals(expected, actual);
    }

    @Test
    public void handlesEmptyTree() {
        RTree<String, Point> tree = RTree.create();

        assertEquals(0, Skyline.compute(tree).size());
    }
}