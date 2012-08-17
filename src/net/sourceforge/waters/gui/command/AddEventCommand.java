//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateNodeCommand
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sourceforge.waters.gui.NamedComparator;
import net.sourceforge.waters.subject.base.AbstractSubject;
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
  public AddEventCommand(final EventListExpressionSubject list,
                         final IdentifierSubject identifier,
                         final int position)
  {
    this(list, Collections.singletonList(identifier), position);
  }

  public AddEventCommand(final EventListExpressionSubject list,
                         final List<? extends IdentifierSubject> identifiers,
                         final int position)
  {
    mList = list;
    final List<IdentifierSubject> modIdentifiers =
      new ArrayList<IdentifierSubject>(identifiers.size());
    final Set<IdentifierSubject> contents =
      new TreeSet<IdentifierSubject>(NamedComparator.getInstance());
    for (final AbstractSubject a : mList.getEventIdentifierListModifiable()) {
      contents.add((IdentifierSubject)a);
    }
    for (final IdentifierSubject n: identifiers) {
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
    mList.getEventIdentifierListModifiable().addAll(mPosition, mIdentifiers);
  }

  public void undo()
  {
    mList.getEventIdentifierListModifiable().removeAll(mIdentifiers);
  }

  public void setUpdatesSelection(final boolean update)
  {
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
