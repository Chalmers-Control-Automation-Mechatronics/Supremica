package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorObject;

public class SelectCommand
	implements Command
{
	private final List<EditorObject> mSelected;
	private final ControlledSurface mSurface;
	
	public SelectCommand(ControlledSurface surface,
						 List<? extends EditorObject> selected)
	{
		mSelected = new ArrayList(selected.size());
		mSelected.addAll(selected);
		mSurface = surface;
	}
	
	public SelectCommand(ControlledSurface surface,
						 EditorObject selected)
	{
		this(surface, Collections.singletonList(selected));
	}
	
	public void execute()
	{
		for (EditorObject o : mSelected)
		{
			mSurface.select(o);
		}
	}
	
	public void undo()
	{
		for (EditorObject o : mSelected)
		{
			mSurface.unselect(o);
		}
	}
	
	public boolean isSignificant()
	{
		return false;
	}
	
	public String getName()
	{
		return "Select";
	}
}
