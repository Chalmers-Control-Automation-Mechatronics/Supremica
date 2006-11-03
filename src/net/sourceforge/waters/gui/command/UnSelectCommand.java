package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.ControlledSurface;

import net.sourceforge.waters.subject.base.ProxySubject;

public class UnSelectCommand
	implements Command
{
	private final List<ProxySubject> mUnSelected;
	private final ControlledSurface mSurface;
	//private final CompoundCommand mCommands;
	
	public UnSelectCommand(ControlledSurface surface,
						 List<? extends ProxySubject> unselected)
	{
		mUnSelected = new ArrayList(unselected);
		//mCommands = new CompoundCommand();
		//at this point I think i'll handle this differently now
		/*for (Subject s : unselected)
		{
			if (o instanceof EditorLabelGroup)
			{
				EditorLabelGroup l = (EditorLabelGroup)o;
				Command c = new UnSelectLabelCommand(l, l.getSelected());
				mCommands.addCommand(c);
			}
		}*/
		//mCommands.end();
		mSurface = surface;
	}
	
	public UnSelectCommand(ControlledSurface surface,
						 	ProxySubject unselected)
	{
		this(surface, Collections.singletonList(unselected));
	}
	
	public void execute()
	{
		//mCommands.execute();
		for (ProxySubject s : mUnSelected)
		{
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
