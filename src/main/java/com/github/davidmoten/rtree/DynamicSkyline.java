package com.github.davidmoten.rtree;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.rtree.geometry.Point;


public final class DynamicSkyline<T> {

    private RTree<T, Point> tree;
    private List<Entry<T, Point>> skyline;

    public DynamicSkyline() {
        this(RTree.<T, Point>create());
    }

    public DynamicSkyline(RTree<T, Point> tree) {
        if (tree == null) {
            throw new NullPointerException("tree");
        }
        this.tree = tree;
        this.skyline = Skyline.compute(tree);
    }

    public void add(T value, Point point) {
        add(tree.context().factory().createEntry(value, point));
    }

    @SuppressWarnings("unchecked")
    public void add(Entry<? extends T, ? extends Point> entry) {
        tree = tree.add(entry);
        Point point = entry.geometry();
        for (Entry<T, Point> member : skyline) {
            if (Skyline.dominates(member.geometry(), point.x(), point.y()))
                return;
        }
        skyline.removeIf(member -> Skyline.dominates(point,
                member.geometry().x(), member.geometry().y()));
        skyline.add((Entry<T, Point>) entry);
    }

    public void delete(T value, Point point) {
        delete(tree.context().factory().createEntry(value, point));
    }

    public void delete(Entry<? extends T, ? extends Point> entry) {
        RTree<T, Point> updated = tree.delete(entry);
        if (updated.size() == tree.size())
            return;
        tree = updated;

        int index = skyline.indexOf(entry);
        if (index < 0)
            return; 

        Point deleted = skyline.remove(index).geometry();
        for (Entry<T, Point> member : skyline) {
            if (member.geometry().x() == deleted.x() && member.geometry().y() == deleted.y())
                return; 
        }
        skyline = Skyline.recover(tree, deleted, skyline);
    }

    public List<Entry<T, Point>> skyline() {
        return new ArrayList<>(skyline);
    }

    public RTree<T, Point> tree() {
        return tree;
    }
}
