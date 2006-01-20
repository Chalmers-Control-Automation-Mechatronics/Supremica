package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.EditorLabelGroup;
import net.sourceforge.waters.subject.module.IdentifierSubject;

public class UnSelectLabelCommand
	implements Command
{
	private final List<IdentifierSubject> mUnSelected;
	private final EditorLabelGroup mGroup;
	
	public UnSelectLabelCommand(EditorLabelGroup group,
						 		List<? extends IdentifierSubject> unSelected)
	{
		mUnSelected = new ArrayList(unSelected.size());
		mUnSelected.addAll(unSelected);
		mGroup = group;
	}
	
	public UnSelectLabelCommand(EditorLabelGroup group,
						 		IdentifierSubject unSelected)
	{
		this(group, Collections.singletonList(unSelected));
	}
	
	public void execute()
	{
		for (IdentifierSubject i : mUnSelected)
		{
			mGroup.unSelectLabel(i);
		}		
	}
	
	public void undo()
	{
		for (IdentifierSubject i : mUnSelected)
		{
			mGroup.selectLabel(i);
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
