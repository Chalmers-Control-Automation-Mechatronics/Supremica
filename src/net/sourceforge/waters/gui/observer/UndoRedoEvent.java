package net.sourceforge.waters.gui.observer;

public class UndoRedoEvent
	implements EditorChangedEvent
{
	public int getType()
	{
		return UNDOREDO;
	}
}
