package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.ControlledSurface;

import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.ProxySubject;

public class SelectCommand
	implements Command
{
	private final List<ProxySubject> mSelected;
	private final ControlledSurface mSurface;
	
	public SelectCommand(ControlledSurface surface,
						 List<? extends ProxySubject> selected)
	{
		mSelected = new ArrayList(selected);
		mSurface = surface;
	}
	
	public SelectCommand(ControlledSurface surface,
						 ProxySubject selected)
	{
		this(surface, Collections.singletonList(selected));
	}
	
	public void execute()
	{
		for (ProxySubject s: mSelected)
		{
			mSurface.select(s);
		}
		mSurface.getEditorInterface().setDisplayed();
	}
	
	public void undo()
	{
		for (ProxySubject s: mSelected)
		{
			mSurface.unselect(s);
		}
		mSurface.getEditorInterface().setDisplayed();
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
