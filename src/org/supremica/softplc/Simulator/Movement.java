package org.supremica.softplc.Simulator;

public class Movement
{
	public int[] startPos, endPos = new int[2];
	public float[] move = new float[2];
	public int parts;

	public Movement(int[] startPos, int[] endPos, int parts)
	{
		this.startPos = startPos;
		this.endPos = endPos;
		this.parts = parts;
		this.move = calcMove(startPos, endPos, parts);
	}

	private float[] calcMove(int[] startPos, int[] endPos, int parts)
	{
		float[] vect = new float[2];

		vect[0] = (float) (endPos[0] - startPos[0]) / parts;
		vect[1] = (float) (endPos[1] - startPos[1]) / parts;

		return vect;
	}
}
