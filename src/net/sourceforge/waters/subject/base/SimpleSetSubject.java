//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   SimpleSetSubject
//###########################################################################
//# $Id: SimpleSetSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.Set;


/**
 * <P>A marker interface identifying implementations of the {@link
 * Set} interface that are also subjects ({@link Subject}). This is
 * simple kind of set that may accept any types of elements---not
 * just subjects.</P>
 *
 * @author Robi Malik
 */

public interface SimpleSetSubject<E>
  extends Set<E>, SimpleCollectionSubject<E>
{

  //#########################################################################
  //# Cloning
  public SimpleSetSubject<E> clone();

}
