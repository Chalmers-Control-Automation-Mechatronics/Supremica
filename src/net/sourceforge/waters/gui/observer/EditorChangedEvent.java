package net.sourceforge.waters.gui.observer;

public interface EditorChangedEvent
{
	public static int NODEMOVED = 1;
	public static int UNDOREDO = 2;
	public static int TOOLBAR = 3;
	
	public int getType();
}
	
