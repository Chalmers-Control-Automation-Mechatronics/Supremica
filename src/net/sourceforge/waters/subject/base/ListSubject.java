//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ListSubject
//###########################################################################
//# $Id: ListSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.List;


/**
 * <P>A marker interface identifying implementations of the {@link
 * List} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface ListSubject<P extends ProxySubject>
  extends List<P>, CollectionSubject<P>
{

  //#########################################################################
  //# Cloning
  public ListSubject<P> clone();

}
