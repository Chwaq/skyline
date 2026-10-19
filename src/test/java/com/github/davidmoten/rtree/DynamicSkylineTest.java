package com.github.davidmoten.rtree;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

public class DynamicSkylineTest {

    @Test
    public void reflectsInsertionsAndDeletions() {
        DynamicSkyline<String> index = new DynamicSkyline<>();
        Point a = Geometries.point(1, 9);
        Point i = Geometries.point(3, 2);
        Point k = Geometries.point(9, 1);

        index.add("a", a);
        index.add("i", i);
        index.add("k", k);
        assertEquals(setOf("a", "i", "k"), values(index.skyline()));

        index.add("better", Geometries.point(0, 0));
        assertEquals(setOf("better"), values(index.skyline()));

        index.delete("better", Geometries.point(0, 0));
        assertEquals(setOf("a", "i", "k"), values(index.skyline()));
    }

    @Test
    public void insertionRemovesOnlyMembersItDominates() {
        DynamicSkyline<String> index = new DynamicSkyline<>();
        index.add("a", Geometries.point(1.0, 9.0));
        index.add("b", Geometries.point(3.0, 2.0));
        index.add("c", Geometries.point(9.0, 1.0));
        index.add("d", Geometries.point(2.0, 1.0));
        assertEquals(setOf("a", "d"), values(index.skyline()));
        index.delete("d", Geometries.point(2.0, 1.0));
        assertEquals(setOf("a", "b", "c"), values(index.skyline()));
    }

    @Test
    public void recoveryPrunesWithSurvivorsAndFindsOnlyNewSkylineMembers() {
        RTree<String, Point> tree = RTree.maxChildren(3).create();
        tree = tree.add("deleted", Geometries.point(1.0, 4.0))
                .add("survivor", Geometries.point(4.0, 1.0))
                .add("restore", Geometries.point(2.0, 5.0))
                .add("stillDominated", Geometries.point(5.0, 5.0))
                .add("dominatedByRestored", Geometries.point(3.0, 6.0))
                .add("outsideRegion", Geometries.point(6.0, 2.0));
        DynamicSkyline<String> index = new DynamicSkyline<>(tree);
        index.delete("deleted", Geometries.point(1.0, 4.0));
        assertEquals(setOf("survivor", "restore"), values(index.skyline()));
        assertEquals(2, index.skyline().size());
    }

    @Test
    public void dominatedAndMissingDeletionsLeaveSkylineUnchanged() {
        DynamicSkyline<String> index = new DynamicSkyline<>();
        index.add("best", Geometries.point(0.0, 0.0));
        index.add("worse", Geometries.point(1.0, 1.0));
        index.delete("worse", Geometries.point(1.0, 1.0));
        index.delete("missing", Geometries.point(0.0, 0.0));
        index.delete("best", Geometries.point(9.0, 9.0));
        assertEquals(1, index.tree().size());
        assertEquals(setOf("best"), values(index.skyline()));
    }

    @Test
    public void duplicateCoordinatesAndIdenticalRecordsAreDeletedOneAtATime() {
        DynamicSkyline<String> index = new DynamicSkyline<>();
        Entry<String, Point> a = Entries.entry("a", Geometries.point(1.0, 1.0));
        Entry<String, Point> b = Entries.entry("b", Geometries.point(1.0, 1.0));
        index.add(a);
        index.add(a);
        index.add(b);
        index.add("hidden", Geometries.point(2.0, 2.0));
        assertEquals(3, index.skyline().size());
        index.delete(a);
        assertEquals(2, index.skyline().size());
        assertEquals(setOf("a", "b"), values(index.skyline()));
        index.delete(b);
        assertEquals(setOf("a"), values(index.skyline()));
        index.delete(a);
        assertEquals(setOf("hidden"), values(index.skyline()));
        assertEquals(1, index.skyline().size());
    }

    @Test
    public void deletingLastEntryAndThenReinsertingWorks() {
        DynamicSkyline<String> index = new DynamicSkyline<>();
        Point point = Geometries.point(-2.0, -3.0);
        index.add("only", point);
        index.delete("only", point);
        assertEquals(0, index.skyline().size());
        assertEquals(0, index.tree().size());
        index.add("again", point);
        assertEquals(setOf("again"), values(index.skyline()));
    }

    @Test
    public void returnedListCannotChangeCachedSkyline() {
        DynamicSkyline<String> index = new DynamicSkyline<>();
        index.add("a", Geometries.point(1.0, 1.0));
        List<Entry<String, Point>> snapshot = index.skyline();
        snapshot.clear();
        assertEquals(setOf("a"), values(index.skyline()));
        index.add("b", Geometries.point(0.0, 0.0));
        assertEquals(0, snapshot.size());
    }

    @Test
    public void randomizedUpdatesMatchIndependentBruteForceIncludingMultiplicity() {
        Random random = new Random(562);
        for (int trial = 0; trial < 20; trial++) {
            RTree<Integer, Point> tree = trial % 2 == 0
                    ? RTree.maxChildren(3).create() : RTree.star().maxChildren(4).create();
            List<Entry<Integer, Point>> records = new ArrayList<>();
            for (int i = 0; i < 40; i++) {
                Entry<Integer, Point> entry = randomEntry(random);
                records.add(entry);
                tree = tree.add(entry);
            }
            DynamicSkyline<Integer> index = new DynamicSkyline<>(tree);
            assertEquals(bruteForce(records), counts(index.skyline()));
            for (int step = 0; step < 150; step++) {
                if (!records.isEmpty() && random.nextBoolean()) {
                    Entry<Integer, Point> entry = records.remove(random.nextInt(records.size()));
                    if (step % 2 == 0)
                        index.delete(entry);
                    else
                        index.delete(entry.value(), entry.geometry());
                } else {
                    Entry<Integer, Point> entry = randomEntry(random);
                    records.add(entry);
                    if (step % 2 == 0)
                        index.add(entry);
                    else
                        index.add(entry.value(), entry.geometry());
                }
                assertEquals(records.size(), index.tree().size());
                assertEquals("trial=" + trial + ", step=" + step,
                        bruteForce(records), counts(index.skyline()));
            }
        }
    }

    private static Entry<Integer, Point> randomEntry(Random random) {
        return Entries.entry(random.nextInt(4), Geometries.point(
                (double) (random.nextInt(11) - 5), (double) (random.nextInt(11) - 5)));
    }

    private static <T> Map<Entry<T, Point>, Integer> bruteForce(List<Entry<T, Point>> records) {
        List<Entry<T, Point>> result = new ArrayList<>();
        for (Entry<T, Point> entry : records) {
            boolean dominated = false;
            Point p = entry.geometry();
            for (Entry<T, Point> other : records) {
                Point q = other.geometry();
                if (q.x() <= p.x() && q.y() <= p.y() && (q.x() < p.x() || q.y() < p.y())) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated)
                result.add(entry);
        }
        return counts(result);
    }

    private static <T> Map<Entry<T, Point>, Integer> counts(List<Entry<T, Point>> records) {
        Map<Entry<T, Point>, Integer> result = new HashMap<>();
        for (Entry<T, Point> entry : records)
            result.put(entry, result.getOrDefault(entry, 0) + 1);
        return result;
    }

    private static <T> Set<T> values(List<Entry<T, Point>> entries) {
        Set<T> result = new HashSet<>();
        for (Entry<T, Point> entry : entries) {
            result.add(entry.value());
        }
        return result;
    }

    @SafeVarargs
    private static <T> Set<T> setOf(T... values) {
        Set<T> result = new HashSet<>();
        for (T value : values) {
            result.add(value);
        }
        return result;
    }
}
