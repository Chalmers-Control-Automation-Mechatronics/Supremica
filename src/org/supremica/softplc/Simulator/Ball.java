package org.supremica.softplc.Simulator;

import java.awt.*;

/**Class Ball is used to model balls
 *
 * Date 15 Oct 2001
 * @author Anders Röding
 * @author Henrik Staberg
 */
public class Ball
{
	public static final int SMALL_BALL = 5;    // allowed sizes of balls
	public static final int BIG_BALL = 8;
	int radius;    // ball's radius
	float tolerance;    // tolerance at endpos
	float[] lower = new float[2];    // coordinates for lower part of ball
	float[] stopPos = new float[2];    // where the ball should stop moving
	float[] sp = new float[2];    // how the ball moves when calling move()
	boolean atEndPos = false;
	boolean ballVisible = true;
	public boolean allowMove = true;
	public boolean allowMove2 = true;

	/**Constructor Ball initialises a new ball
	 * @param size Small ball (1, default) or Big ball (2)
	 */
	public Ball(int size)
	{
		switch (size)
		{

		case 1 :
		{
			radius = SMALL_BALL;

			break;
		}
		case 2 :
		{
			radius = BIG_BALL;

			break;
		}
		default :
		{
			radius = SMALL_BALL;
		}
		}
	}

	public Ball()
	{
		radius = SMALL_BALL;
	}

	/**moveInit initializes a new subtrack for a ball.
	 * @param fromPos startposition for new subtrack
	 * @param toPos endposition for new subtrack
	 * @param speed speed of ball
	 */
	public void moveInit(int[] fromPos, int[] toPos, float[] speed)
	{
		lower[0] = fromPos[0];
		lower[1] = fromPos[1];
		stopPos[0] = toPos[0];
		stopPos[1] = toPos[1];
		sp = speed;
		atEndPos = false;
		tolerance = (float) Math.sqrt(sp[0] * sp[0] + sp[1] * sp[1]);
	}

	/**move makes the ball move one step as initialized with moveInit()
	 */
	public void move()
	{
		if ((!atEndPos) && allowMove && allowMove2)
		{
			lower[0] += sp[0];
			lower[1] += sp[1];

			if ((Math.abs(lower[0] - stopPos[0]) < tolerance) && (Math.abs(lower[1] - stopPos[1]) < tolerance))
			{
				atEndPos = true;
			}
		}
	}

	/**setCoord is used when ball is travelling in lifts and arm. The
	 * method makes it possible to allow the ball go in different
	 * directions
	 */
	public void setCoord(int x, int y)
	{
		lower[0] = x;
		lower[1] = y;
	}

	/** collisionRisk calculates whether this ball is about to run into
	 *  the ball b
	 *  @param b the ball that we might run into
	 *  @return returns true if we might collide
	 */
	public boolean collisionRisk(Ball b)
	{
		float[] np = { lower[0] + sp[0], lower[1] + sp[1] };
		double d1 = distance(lower, b.getFloatPosition());
		double d2 = distance(np, b.getFloatPosition());

		return ((d1 <= (radius + b.getRadius())) && (d2 < d1));
	}

	/**Distance gives the distance between two points
	 * @param p1 a point/position
	 * @param p2 a point/position
	 * @return the distance between the two points
	 */
	private double distance(float[] p1, float[] p2)
	{
		float pp1 = p1[0] - p2[0];
		float pp2 = p1[1] - p2[1];

		return Math.sqrt(pp1 * pp1 + pp2 * pp2);
	}

	/**getPosition returns the ball's current position
	 * @return ball's position
	 */
	public int[] getPosition()
	{
		int[] r = new int[]{ Math.round(lower[0]), Math.round(lower[1]) };

		return r;
	}

	// Ta inte bort efter tester, kommentera ist.
	public float[] getFloatPosition()
	{
		return lower;
	}

	/**getPosition returns the ball's radius
	 * @return ball's radius in pixels
	 */
	public int getRadius()
	{
		return radius;
	}

	/**getPosition returns whether the ball has finished it's current leg
	 * i.e. if the ball has reached it's end position
	 * @return whether the ball has reached it's end position
	 */
	public boolean finishedLeg()
	{
		return atEndPos;
	}

	/**setVisible sets a flag to say whether it should be painted or not
	 * @param
	 */
	public void setVisible(boolean v)
	{
		ballVisible = v;
	}

	/**allowMove is used to model the physical behaviour of balls, so that
	 * they do not run over each other.
	 */
	public void allowMove(boolean a)
	{
		allowMove = a;
	}

	/**allowMove2 Gives false when a ball gets from leg7 to the arm.
	 * Then when the arm moves back (left) the value again is true.
	 */
	public void allowMove2(boolean a)
	{
		allowMove2 = a;
	}

	/**paint paints the ball
	 * @param g the graphics pen
	 */
	public void paint(Graphics g)
	{
		if (ballVisible)
		{
			g.setColor(Color.black);
			g.fillOval(Math.round(lower[0] - radius), Math.round(lower[1] - radius * 2), radius * 2, radius * 2);
		}
	}
}
