//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   SimpleListSubject
//###########################################################################
//# $Id: SimpleListSubject.java,v 1.3 2007-02-26 21:41:18 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.List;


/**
 * <P>A marker interface identifying implementations of the {@link
 * List} interface that are also subjects ({@link Subject}). This is
 * a simple kind of list that may accept any types of elements---not
 * just subjects.</P>
 *
 * @author Robi Malik
 */

public interface SimpleListSubject<E>
  extends List<E>, SimpleCollectionSubject<E>
{

  //#########################################################################
  //# Cloning
  public SimpleListSubject<E> clone();

}
