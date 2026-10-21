package com.github.davidmoten.rtree;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;

public final class DynamicSkylineDemo {

    private DynamicSkylineDemo() {
    }

    public static void main(String[] args) throws IOException {
        Path dataset = Paths.get("src/main/java/com/github/davidmoten/rtree/Dataset_for_PA1");;
        RTree<Integer, Point> tree = SkylineMain.load(dataset);

        DynamicSkyline<Integer> index = new DynamicSkyline<>(tree);
        List<Entry<Integer, Point>> original = index.skyline();
        printSkyline("1. Original dataset", index.tree().size(), original);

        // Smaller than every dataset point 
        // dominates all original skyline members. 
        Rectangle bounds = tree.root().get().geometry().mbr();
        Point added = Geometries.point(bounds.x1() - 1.0, bounds.y1() - 1.0);
        int addedId = -1;
        System.out.println("\nInsert demonstration point: " + addedId + " " + added.x() + " " + added.y());
        index.add(addedId, added);
        printSkyline("2. After inserting one point", index.tree().size(), index.skyline());
        System.out.println("\nDelete demonstration point: " + addedId);
        index.delete(addedId, added);
        List<Entry<Integer, Point>> restored = index.skyline();
        printSkyline("3. After deleting that same point", index.tree().size(), restored);
        boolean matches = restored.size() == original.size() && new HashSet<>(restored).equals(new HashSet<>(original));
        System.out.println("Original skyline restored: " + matches);
        if (!matches) {
            throw new IllegalStateException("The original skyline was not restored");
        }
    }

    private static void printSkyline(String stage, int pointCount, List<Entry<Integer, Point>> skyline) {
        System.out.println("\n" + stage);
        System.out.println("points=" + pointCount);
        System.out.println("skyline=" + skyline.size());
        System.out.println("id x y");
        List<Entry<Integer, Point>> sorted = new ArrayList<>(skyline);
        sorted.sort(Comparator.comparingInt(entry -> entry.value()));
        for (Entry<Integer, Point> entry : sorted) {
            Point point = entry.geometry();
            System.out.println(entry.value() + " " + point.x() + " " + point.y());
        }
    }
}
