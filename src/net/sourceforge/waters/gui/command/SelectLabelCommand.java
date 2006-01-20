package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.gui.EditorLabelGroup;
import net.sourceforge.waters.subject.module.IdentifierSubject;

public class SelectLabelCommand
	implements Command
{
	private final List<IdentifierSubject> mSelected;
	private final EditorLabelGroup mGroup;
	
	public SelectLabelCommand(EditorLabelGroup group,
						 	  List<? extends IdentifierSubject> selected)
	{
		mSelected = new ArrayList(selected.size());
		mSelected.addAll(selected);
		mGroup = group;
	}
	
	public SelectLabelCommand(EditorLabelGroup group,
		 					  IdentifierSubject selected)
	{
		this(group, Collections.singletonList(selected));
	}
	
	public void execute()
	{
		for (IdentifierSubject i : mSelected)
		{
			mGroup.selectLabel(i);
		}		
	}
	
	public void undo()
	{
		for (IdentifierSubject i : mSelected)
		{
			mGroup.unSelectLabel(i);
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
