//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   IndexedSetSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import net.sourceforge.waters.model.base.IndexedSet;


/**
 * <P>A marker interface identifying implementations of the {@link
 * IndexedSet} interface that are also subjects ({@link Subject}).</P>
 *
 * @author Robi Malik
 */

public interface IndexedSetSubject<P extends NamedSubject>
  extends IndexedSet<P>, SetSubject<P>
{
}
