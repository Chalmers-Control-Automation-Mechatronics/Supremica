package org.supremica.automata.algorithms.scheduling;

import java.util.*;

public class VisibilityChecker {

    private ArrayList<int[]> intersectionEdges; 
    private int[] start, goal;

    public VisibilityChecker(ArrayList<int[]> intersectionEdges, int[] goal) {
	this(intersectionEdges, new int[]{0, 0}, goal);
    }

    public VisibilityChecker(ArrayList<int[]> intersectionEdges, int[] start, int[] goal) {
	this.start = start;
	this.goal = goal;
	
	this.intersectionEdges = new ArrayList(intersectionEdges);
    }

    /**
     * This method returns true if the points (a, b, c) are ordered clockwise.
     * This is useful in intersection detection.  
     */
    private boolean clockwisePts(int[] a, int[] b, int[] c) {
	int leftSide = (c[1]-a[1])*(b[0]-a[0]);
	int rightSide = (b[1]-a[1])*(c[0]-a[0]);

	if (leftSide < rightSide)
	    return true;

	return false;
    }

    /**
     * Loops through all edges and checks if any of them intersects the start-goal-line.
     */
    public boolean isVisible() {
	for (int i=0; i<intersectionEdges.size(); i++) {
	    if (!pointOnTheEdge(intersectionEdges.get(i))) {
		int[] firstVertice = new int[]{intersectionEdges.get(i)[0], intersectionEdges.get(i)[1]};
		int[] secondVertice = new int[]{intersectionEdges.get(i)[2], intersectionEdges.get(i)[3]};
	
		if (intersect(start, goal, firstVertice, secondVertice))
		    return false;
	    }
	}
	return true;
    }

    /**
     * Returns true if the two edges ab and cd intersect. 
     */
    private boolean intersect(int[] a, int[] b, int[] c, int[] d) {
	if (clockwisePts(a, c, d) == clockwisePts(b, c, d))
	    return false;
	else if (clockwisePts(a, b, c) == clockwisePts(a, b, d))
	    return false;
	
	return true;
    }

    /**
     * Returns true if the start point lies somewhere on the supplied edge (including the vertices). 
     */
    private boolean pointOnTheEdge(int[] edge) {
	int[] a = new int[]{edge[0], edge[1]};
	int[] b = new int[]{edge[2], edge[3]};

	if (!clockwisePts(start, a, b) && !clockwisePts(start, b, a)) {
	    if (start[0] < Math.min(edge[0], edge[2])) 
		return false;
	    else if (start[0] > Math.max(edge[0], edge[2])) 
		return false;
	    else if (start[1] < Math.min(edge[1], edge[3])) 
		return false;
	    else if (start[1] > Math.max(edge[1], edge[3]))
		return false;

	    return true;
	}

	return false;
    }

    public void setStart(int[] start) {
	for (int i=0; i<start.length; i++)
	    this.start[i] = start[i]; 
    }

    public void setGoal(int[] goal) {
	for (int i=0; i<goal.length; i++)
	    this.goal[i] = goal[i]; 
    }

    public double getDistanceToDiag(int[] point) {
	double s1 = 0; 
	double s2 = 0;

	for (int i=0; i<point.length; i++) {
	    s1 += Math.pow(start[i]-point[i], 2);
	    s2 += Math.pow(goal[i]-point[i], 2);
	}
	
	return Math.sqrt(s1) + Math.sqrt(s2);
    }
}