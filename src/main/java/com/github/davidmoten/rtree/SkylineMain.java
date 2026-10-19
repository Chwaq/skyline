package com.github.davidmoten.rtree;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

public final class SkylineMain {

    private SkylineMain() {
    }

    public static void main(String[] args) throws IOException {

        Path dataset = Paths.get("src/main/java/com/github/davidmoten/rtree/Dataset_for_PA1");;
        RTree<Integer, Point> tree = load(dataset);
        List<Entry<Integer, Point>> skyline = Skyline.compute(tree);

        System.out.println("points=" + tree.size());
        System.out.println("skyline=" + skyline.size());
        for (Entry<Integer, Point> entry : skyline) {
            Point point = entry.geometry();
            System.out.println(entry.value() + " " + point.x() + " " + point.y());
        }
    }

    static RTree<Integer, Point> load(Path path) throws IOException {
        RTree<Integer, Point> tree = RTree.create();
        int id = 0;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.split("\\s+");
                if (fields.length != 2)
                    throw new IllegalArgumentException("Expected two coordinates");

                try{
                    double x = Double.parseDouble(fields[0]);
                    double y = Double.parseDouble(fields[1]);
                    tree = tree.add(id++, Geometries.point(x, y));
                }catch (NumberFormatException e){
                    throw new IllegalArgumentException("Invalid coordinates");
                }
            }
        }
        return tree;
    }
}
