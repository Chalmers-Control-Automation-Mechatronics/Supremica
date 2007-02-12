package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.waters.gui.ControlledSurface;

import net.sourceforge.waters.subject.base.ProxySubject;

public class UnSelectCommand
	implements Command
{
	private final Collection<ProxySubject> mUnSelected;
	private final ControlledSurface mSurface;
	
	public UnSelectCommand
	  (final ControlledSurface surface,
	   final Collection<? extends ProxySubject> unselected)
	{
	  mUnSelected = new ArrayList<ProxySubject>(unselected);
	  mSurface = surface;
	}
	
	public UnSelectCommand(final ControlledSurface surface,
			       final ProxySubject unselected)
	{
	  this(surface, Collections.singletonList(unselected));
	}
	
	public void execute()
	{
	  for (final ProxySubject s : mUnSelected) {
	    mSurface.unselect(s);			
	  }
	  mSurface.getEditorInterface().setDisplayed();
	}
	
	public void undo()
	{
		for (ProxySubject s : mUnSelected)
		{
			mSurface.select(s);
		}
		//mCommands.undo();
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
