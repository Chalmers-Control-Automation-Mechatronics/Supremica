package org.supremica.automata.algorithms.scheduling;

public class GlpkBridge
{
	public native void bridge(String modelFile);

	static
	{
		System.loadLibrary("glpk_bridge");
	}

	public static void main(String[] args) 
	{
		if (args.length == 0)
		{
			System.err.println("Supply a model file (*.mod).............");
			System.exit(0);
		}
			
		new GlpkBridge().bridge(args[0]);
	}
}