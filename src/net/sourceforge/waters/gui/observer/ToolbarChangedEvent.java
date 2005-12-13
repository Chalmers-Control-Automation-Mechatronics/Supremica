package net.sourceforge.waters.gui.observer;

public class ToolbarChangedEvent
	implements EditorChangedEvent
{
	public ToolbarChangedEvent()
	{
	}
	
	public int getType()
	{
		return TOOLBAR;
	}
}
