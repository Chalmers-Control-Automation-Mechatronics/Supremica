//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   IndexedListSubject
//###########################################################################
//# $Id: IndexedListSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import net.sourceforge.waters.model.base.IndexedList;


/**
 * <P>A marker interface identifying implementations of the {@link
 * IndexedList} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface IndexedListSubject<P extends NamedSubject>
  extends IndexedList<P>, ListSubject<P>
{

  //#########################################################################
  //# Cloning
  public IndexedListSubject<P> clone();

}
