package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorObject;

public class UnSelectCommand
	implements Command
{
	private final List<EditorObject> mUnSelected;
	private final ControlledSurface mSurface;
	
	public UnSelectCommand(ControlledSurface surface,
						 List<? extends EditorObject> unselected)
	{
		mUnSelected = new ArrayList(unselected.size());
		mUnSelected.addAll(unselected);
		mSurface = surface;
	}
	
	public UnSelectCommand(ControlledSurface surface,
						 EditorObject unselected)
	{
		this(surface, Collections.singletonList(unselected));
	}
	
	public void execute()
	{
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
