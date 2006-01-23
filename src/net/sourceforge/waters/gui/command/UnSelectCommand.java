package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorObject;
import net.sourceforge.waters.gui.EditorLabelGroup;

public class UnSelectCommand
	implements Command
{
	private final List<EditorObject> mUnSelected;
	private final ControlledSurface mSurface;
	private final CompoundCommand mCommands;
	
	public UnSelectCommand(ControlledSurface surface,
						 List<? extends EditorObject> unselected)
	{
		mUnSelected = new ArrayList(unselected);
		mCommands = new CompoundCommand();
		for (EditorObject o : unselected)
		{
			if (o instanceof EditorLabelGroup)
			{
				EditorLabelGroup l = (EditorLabelGroup)o;
				Command c = new UnSelectLabelCommand(l, l.getSelected());
				mCommands.addCommand(c);
			}
		}
		mCommands.end();
		mSurface = surface;
	}
	
	public UnSelectCommand(ControlledSurface surface,
						 EditorObject unselected)
	{
		this(surface, Collections.singletonList(unselected));
	}
	
	public void execute()
	{
		mCommands.execute();
		for (EditorObject o : mUnSelected)
		{
			mSurface.unselect(o);			
		}
		mSurface.getEditorInterface().setDisplayed();
	}
	
	public void undo()
	{
		for (EditorObject o : mUnSelected)
		{
			mSurface.select(o);
		}
		mCommands.undo();
		mSurface.getEditorInterface().setDisplayed();
	}
	
	public boolean isSignificant()
	{
		return false;
	}
	
	public String getName()
	{
		return "UnSelect";
	}
}
