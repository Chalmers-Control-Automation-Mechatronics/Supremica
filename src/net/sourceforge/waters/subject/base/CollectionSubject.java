//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   CollectionSubject
//###########################################################################
//# $Id: CollectionSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.util.Collection;


/**
 * <P>A marker interface identifying implementations of the {@link
 * Collection} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface CollectionSubject<P extends ProxySubject>
  extends Collection<P>, Subject
{
}
