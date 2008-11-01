//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   IndexedSet
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Set;


/**
 * <P>A set that uses the names of Waters elements as an index.</P>
 *
 * @author Robi Malik
 */

public interface IndexedSet<P extends NamedProxy>
  extends IndexedCollection<P>, Set<P>
{
}
