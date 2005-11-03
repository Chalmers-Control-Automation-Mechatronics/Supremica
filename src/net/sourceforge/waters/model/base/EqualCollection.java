//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   EqualCollection
//###########################################################################
//# $Id: EqualCollection.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * An utility class to compare collections as sets.
 * 
 * @author Robi Malik
 */

public class EqualCollection
{

  //#########################################################################
  //# Equality
  public static boolean equalSet(final Collection<?> coll1,
                                 final Collection<?> coll2)
  {
    if (coll1.size() <= coll2.size()) {
      final Map<Object,Integer> map =
        new HashMap<Object,Integer>(coll1.size());
      for (final Object item : coll1) {
        map.put(item, 0);
      }
      for (final Object item : coll2) {
        final Integer value = map.get(item);
        if (value == null) {
          return false;
        } else if (value == 0) {
          map.put(item, 1);
        }
      }
      for (final int value : map.values()) {
        if (value == 0) {
          return false;
        }
      }
      return true;
    } else {
      return equalSet(coll2, coll1);
    }
  }

}
