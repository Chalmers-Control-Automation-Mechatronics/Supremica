package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.subject.module.IdentifierSubject;
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;
import net.sourceforge.waters.gui.NamedComparator;
import java.util.TreeSet;
import com.jclark.xsl.util.Comparator;
import net.sourceforge.waters.subject.base.NamedSubject;
import java.util.Collection;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;

public class AddEventCommand
	implements Command
{
	private final EventListExpressionSubject mList;
	private final Collection<IdentifierSubject> mIdentifiers;
	private final int mPosition;
	private final String mDescription = "Add Event";
	
  public AddEventCommand(EventListExpressionSubject list,
                         IdentifierSubject identifier,
                         int position)
  {
    this(list, Collections.singleton(identifier), position);
  }
  
	public AddEventCommand(EventListExpressionSubject list,
                         Collection<? extends IdentifierSubject> identifiers,
                         int position)
	{
		mList = list;
		mIdentifiers = new ArrayList<IdentifierSubject>(identifiers.size());
    Set<IdentifierSubject> contents = new TreeSet<IdentifierSubject>(NamedComparator.getInstance());
    for (AbstractSubject a : mList.getEventListModifiable()) {
      contents.add((IdentifierSubject)a);
    }
    for (IdentifierSubject n: identifiers) {
      if (contents.add(n)) {
        mIdentifiers.add(n.clone());
      }
    }
		mPosition = position;
	}
	
	public void execute()
	{
		mList.getEventListModifiable().addAll(mPosition, mIdentifiers);
	}
	
    /** 
     * Undoes the Command
     *
     */    
    public void undo()
    {
      mList.getEventListModifiable().removeAll(mIdentifiers);
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
