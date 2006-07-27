package net.sourceforge.waters.gui.renderer;

public interface Handle
	extends RendererShape
{
	public HandleType getType();
	
	public enum HandleType
	{
		SOURCE, TARGET,
		NW, N, NE, W, E, SW, S, SE,
		INITIAL;
	}
}
