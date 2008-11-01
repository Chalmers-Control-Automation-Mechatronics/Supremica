//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.transfer
//# CLASS:   ListInsertPosition
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.transfer;

import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;


/**
 * A position record for subjects inserted or deleted in a list.
 * This is an example of the information typically stored as <I>insert
 * position</I> in a {@link InsertInfo} object.
 *
 * @author Robi Malik
 */

public class ListInsertPosition {

  //#########################################################################
  //# Constructor
  public ListInsertPosition(final ListSubject<? extends ProxySubject> list,
			    final int pos)
  {
    mList = list;
    mPosition = pos;
  }


  //#######################################################################
  //# Simple Access
  public ListSubject<? extends ProxySubject> getList()
  {
    return mList;
  }

  public int getPosition()
  {
    return mPosition;
  }


  //#######################################################################
  //# Data Members
  private final ListSubject<? extends ProxySubject> mList;
  private final int mPosition;

}
