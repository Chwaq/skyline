package com.github.davidmoten.rtree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;

public final class Skyline {

     private static final class Candidate<T>{
        final Node<T, Point> node;
        final Entry<T, Point> entry;
        final Rectangle bounds;
        final double score;

        Candidate(Node<T, Point> node){
            this.node = node;
            this.entry = null;
            this.bounds = node.geometry().mbr();
            this.score = bounds.x1() + bounds.y1();
        }

        Candidate(Entry<T, Point> entry){
            this.node = null;
            this.entry = entry;
            this.bounds = entry.geometry().mbr();
            this.score = bounds.x1() + bounds.y1();
        }
    }
    
    private Skyline() {
        // prevent instantiation
    }

    public static <T> List<Entry<T, Point>> compute(RTree<T, Point> tree){
        return compute(tree, null, Collections.emptyList());
    }

    //Keeps the surviving skyline and searches only the region strictly dominated by the deleted point.
    static <T> List<Entry<T, Point>> recover(RTree<T, Point> tree, Point deleted, List<Entry<T, Point>> survivors) {
        return compute(tree, deleted, survivors);
    }

    private static <T> List<Entry<T, Point>> compute(RTree<T, Point> tree, Point deleted, List<Entry<T, Point>> initialSkyline) {
        List<Entry<T, Point>> skyline = new ArrayList<>(initialSkyline);

        if(!tree.root().isPresent())
            return skyline;

        PriorityQueue<Candidate<T>> heap = new PriorityQueue<>((a,b) -> Double.compare(a.score, b.score));
        
        expand(tree.root().get(),skyline, heap, deleted);

        while(!heap.isEmpty()){
            Candidate<T> candidate = heap.poll();

            if(isDominated(candidate.bounds, skyline))
                continue;

            if(candidate.node != null){
                expand(candidate.node, skyline, heap, deleted);
            } else {
                skyline.add(candidate.entry);
            }
        }
        return skyline;
    }

     private static <T> void offerIfUseful(Candidate<T> candidate, List<Entry<T, Point>> skyline, PriorityQueue<Candidate<T>> heap, Point deleted){
        if (deleted != null) {
            if (candidate.node != null) {
                // An MBR completely outside the affected upper-right region
                // cannot contain a point that needs to be restored.
                if (candidate.bounds.x2() < deleted.x() || candidate.bounds.y2() < deleted.y())
                    return;
            } else if (!dominates(deleted, candidate.bounds.x1(), candidate.bounds.y1())) {
                return;
            }
        }
        if(!isDominated(candidate.bounds, skyline)){
            heap.offer(candidate);
        }
    }

    static boolean dominates(Point p, double x, double y){
        return p.x() <= x && p.y() <= y && (p.x() < x || p.y() < y);
    }

    private static<T> void expand(Node<T, Point> node, List<Entry<T, Point>> skyline, PriorityQueue<Candidate<T>> heap, Point deleted){
        if(node instanceof NonLeaf){
            NonLeaf<T, Point> nonLeaf = (NonLeaf<T, Point>) node;

            for(int i = 0;i < nonLeaf.count(); i++){
                Candidate<T> candidate = new Candidate<>(nonLeaf.child(i));
                offerIfUseful(candidate, skyline, heap, deleted);
            }
        }else{
            Leaf<T, Point> leaf = (Leaf<T, Point>) node;
            for(int i = 0;i < leaf.count(); i++){
                Candidate<T> candidate = new Candidate<>(leaf.entry(i));
                offerIfUseful(candidate, skyline, heap, deleted);
            }
        }
    }


    private static<T> boolean isDominated(Rectangle bounds, List<Entry<T, Point>> skyline){
        double x = bounds.x1();
        double y = bounds.y1();

        for(Entry<T, Point> entry : skyline){
            Point p = entry.geometry();

            if(dominates(p, x, y))
                return true;
        }
        return false;
    }

   
}
