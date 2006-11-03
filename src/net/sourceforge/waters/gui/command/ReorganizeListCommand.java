package net.sourceforge.waters.gui.command;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.IdentityHashMap;
import java.util.ArrayList;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;

public class ReorganizeListCommand
	implements Command
{
	private final EventListExpressionSubject mList;
	private final List<AbstractSubject> mIdentifiers;
	private final IdentityHashMap<AbstractSubject, Integer> mIndexs;
	private final int mNewPosition;
	private final String mDescription = "Move Event";
	
	public ReorganizeListCommand(EventListExpressionSubject group,
                               List<? extends AbstractSubject> identifiers,
                               int newPosition)
	{		
		mList = group;
		mIdentifiers = new ArrayList<AbstractSubject>(identifiers.size());
		mIdentifiers.addAll(identifiers);
		Collections.sort(mIdentifiers, new Comparator<AbstractSubject>()
		{
			public int compare(AbstractSubject a1, AbstractSubject a2)
			{
				return (mList.getEventListModifiable().indexOf(a1) -
                mList.getEventListModifiable().indexOf(a2));
			}
			
			public boolean equals(Object o)
			{
				return o == this;
			}
		});
		mIndexs = new IdentityHashMap<AbstractSubject, Integer>();
		for (AbstractSubject a : identifiers)
		{
			int index = mList.getEventList().indexOf(a);
			mIndexs.put(a, new Integer(index));
		}
		mNewPosition = newPosition;
	}
	
	public void execute()
	{		
		final List<AbstractSubject> list = mList.getEventListModifiable();
		list.removeAll(mIdentifiers);
		int i = 0;
		for (AbstractSubject a : mIdentifiers)
		{
			int index = mNewPosition + i;
			if (index > list.size())
			{
				index = list.size();
			}
			list.add(index, a);
			i++;
		}
		// Remove label and add to new position in list				
	}
	
    /** 
     * Undoes the Command
     */    
    public void undo()
    {
		final List<AbstractSubject> list =
						mList.getEventListModifiable();		
		// Remove label and add to new position in list
		list.removeAll(mIdentifiers);
		for (AbstractSubject a : mIdentifiers)
		{
			list.add(mIndexs.get(a).intValue(), a);
		}
    }
	
		
	public boolean isSignificant()
	{
		return true;
	}

    public String getName()
    {
		return mDescription;
    }
}
