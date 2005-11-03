//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   IndexedSet
//###########################################################################
//# $Id: IndexedSet.java,v 1.2 2005-11-03 01:24:15 robi Exp $
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
