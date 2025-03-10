package org.supremica.automata.algorithms.scheduling;

import java.util.ArrayList;

public class VisibilityChecker {

    private final ArrayList<double[]> intersectionEdges;
    private final double[] start, goal;

	public VisibilityChecker(final ArrayList<double[]> intersectionEdges)
	{
		this(intersectionEdges, new double[]{0, 0});
	}

    public VisibilityChecker(final ArrayList<double[]> intersectionEdges, final double[] goal) {
	this(intersectionEdges, new double[]{0, 0}, goal);
    }

    public VisibilityChecker(final ArrayList<double[]> intersectionEdges, final double[] start, final double[] goal) {
	this.start = start;
	this.goal = goal;

	this.intersectionEdges = new ArrayList<double[]>(intersectionEdges);
    }

    /**
     * This method returns true if the points (a, b, c) are ordered clockwise.
     * This is useful in intersection detection.
     */
    private boolean clockwisePts(final double[] a, final double[] b, final double[] c)
	{
		final double leftSide = (c[1]-a[1])*(b[0]-a[0]);
		final double rightSide = (b[1]-a[1])*(c[0]-a[0]);

		if (leftSide < rightSide)
			return true;

		return false;
    }

    /**
     * Loops through all edges and checks if any of them intersects the start-goal-line.
     */
    public boolean isVisible(final boolean backwardTimeAllowed)
	{
		if (!backwardTimeAllowed)
		{
			if (goal[0] < start[0] || goal[1] < start[1])
			{
				return false;
			}
		}

		if (goal[0] == start[0] && goal[1] == start[1])
		{
			return false;
		}

		for (int i=0; i<intersectionEdges.size(); i++)
		{
			if (pointOnTheEdge(start, intersectionEdges.get(i)))
			{
				if (pointOnTheEdge(goal, intersectionEdges.get(i)))
				{
					return true;
				}
			}
			else
 			{
				final double[] firstVertice = new double[]{intersectionEdges.get(i)[0], intersectionEdges.get(i)[1]};
				final double[] secondVertice = new double[]{intersectionEdges.get(i)[2], intersectionEdges.get(i)[3]};

				if (intersect(start, goal, firstVertice, secondVertice))
				{
					return false;
				}
 			}
		}

		return true;
    }

	/**
	 * Be default, movement backwards in time is not allowed
	 */
	public boolean isVisible()
	{
		return isVisible(false);
	}

	public ArrayList<Integer> getVisibleIndices(final double[] start, final ArrayList<double[]> vertices)
	{
		final ArrayList<Integer> visibleIndices = new ArrayList<Integer>();

		setStart(start);

		for (int i=0; i<vertices.size(); i++)
		{
			setGoal(vertices.get(i));

			if (isVisible())
			{
				visibleIndices.add(i);
			}
		}

		return visibleIndices;
	}

    /**
     * Returns true if the two edges ab and cd intersect.
     */
    private boolean intersect(final double[] a, final double[] b, final double[] c, final double[] d)
	{
		if (clockwisePts(a, c, d) == clockwisePts(b, c, d))
		{
			return false;
		}
		else if (clockwisePts(a, b, c) == clockwisePts(a, b, d))
		{
			return false;
		}

		return true;
    }

    /**
     * Returns true if the start point lies somewhere on the supplied edge (including the vertices).
     */
    private boolean pointOnTheEdge(final double[] start, final double[] edge)
	{
		final double[] a = new double[]{edge[0], edge[1]};
		final double[] b = new double[]{edge[2], edge[3]};

		if (!clockwisePts(start, a, b) && !clockwisePts(start, b, a))
		{
			if (start[0] < Math.min(edge[0], edge[2]))
			{
				return false;
			}
			else if (start[0] > Math.max(edge[0], edge[2]))
			{
				return false;
			}
			else if (start[1] < Math.min(edge[1], edge[3]))
			{
				return false;
			}
			else if (start[1] > Math.max(edge[1], edge[3]))
			{
				return false;
			}

			return true;
		}

		return false;
    }

    public void setStart(final double[] start)
	{
		for (int i=0; i<this.start.length; i++)
		{
			this.start[i] = start[i];
		}
    }

    public void setGoal(final double[] goal)
	{
		for (int i=0; i<this.goal.length; i++)
		{
			this.goal[i] = goal[i];
		}
    }

    public double getDistanceToDiag(final double[] point)
	{
		double s1 = 0;
		double s2 = 0;

		for (int i=0; i<point.length; i++)
		{
			s1 += Math.pow(start[i]-point[i], 2);
			s2 += Math.pow(goal[i]-point[i], 2);
		}

		return Math.sqrt(s1) + Math.sqrt(s2);
    }
}