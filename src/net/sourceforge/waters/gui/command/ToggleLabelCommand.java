package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.EditorLabelGroup;
import net.sourceforge.waters.subject.module.IdentifierSubject;

public class ToggleLabelCommand
	implements Command
{
	private final List<IdentifierSubject> mToggled;
	private final EditorLabelGroup mGroup;
	
	public ToggleLabelCommand(EditorLabelGroup group,
						 	  List<? extends IdentifierSubject> toggled)
	{
		mToggled = new ArrayList(toggled.size());
		mToggled.addAll(toggled);
		mGroup = group;
	}
	
	public ToggleLabelCommand(EditorLabelGroup group,
						 	  IdentifierSubject toggled)
	{
		this(group, Collections.singletonList(toggled));
	}
	
	public void execute()
	{
		for (IdentifierSubject i : mToggled)
		{
			mGroup.toggleLabel(i);
		}		
	}
	
	public void undo()
	{
		for (IdentifierSubject i : mToggled)
		{
			mGroup.toggleLabel(i);
		}		
	}
	
	public boolean isSignificant()
	{
		return false;
	}
	
	public String getName()
	{
		return "Unselect";
	}
}
