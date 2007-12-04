//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateNodeCommand
//###########################################################################
//# $Id: AddEventCommand.java,v 1.12 2007-12-04 03:22:54 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.gui.NamedComparator;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;


/**
 * The Command for adding events labels to a label block.
 *
 * @author Simon Ware
 */

public class AddEventCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  public AddEventCommand(EventListExpressionSubject list,
                         IdentifierSubject identifier,
                         int position)
  {
    this(list, Collections.singletonList(identifier), position);
  }
  
  public AddEventCommand(EventListExpressionSubject list,
                         List<? extends IdentifierSubject> identifiers,
                         int position)
  {
    mList = list;
    final List<IdentifierSubject> modIdentifiers =
      new ArrayList<IdentifierSubject>(identifiers.size());
    Set<IdentifierSubject> contents =
      new TreeSet<IdentifierSubject>(NamedComparator.getInstance());
    for (AbstractSubject a : mList.getEventListModifiable()) {
      contents.add((IdentifierSubject)a);
    }
    for (IdentifierSubject n: identifiers) {
      if (contents.add(n)) {
        modIdentifiers.add(n.clone());
      }
    }
    mIdentifiers = Collections.unmodifiableList(modIdentifiers);
    mPosition = position;
  }
	

  //#########################################################################
  //# Simple Access
  /**
   * Gets the list of identifiers to be inserted by this command.
   */
  public List<IdentifierSubject> getAddedIdentifiers()
  {
    return mIdentifiers;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command
  public void execute()
  {
    mList.getEventListModifiable().addAll(mPosition, mIdentifiers);
  }

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


  //#########################################################################
  //# Data Members
  private final EventListExpressionSubject mList;
  private final List<IdentifierSubject> mIdentifiers;
  private final int mPosition;
  private final String mDescription = "Add Event";

}
