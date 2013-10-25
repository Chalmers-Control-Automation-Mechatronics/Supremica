//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.compositional
//# CLASS:   AbstractSelectionHeuristic
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.compositional;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;


/**
 * @author Robi Malik
 */

public abstract class AbstractSelectionHeuristic<T>
  implements Comparator<T>
{

  //#########################################################################
  //# Invocation
  public T select(final Collection<? extends T> candidates)
  {
    final Iterator<? extends T> iter = candidates.iterator();
    if (iter.hasNext()) {
      T result = iter.next();
      while (iter.hasNext()) {
        final T next = iter.next();
        if (compare(result, next) > 0) {
          result = next;
        }
      }
      reset();
      return result;
    } else {
      return null;
    }
  }

  public void reset()
  {
  }

}
