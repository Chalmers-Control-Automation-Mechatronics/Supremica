//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   Geometry
//###########################################################################
//# $Id: Geometry.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * <P>A set of handy methods for comparing geometries.</P>
 *
 * @author Robi Malik
 */

public class Geometry {

  public static boolean equalGeometry(final GeometryProxy geo1,
                                      final GeometryProxy geo2)
  {
    if (geo1 == null) {
      return geo2 == null;
    } else {
      return geo1.equals(geo2);
    }
  }

  public static boolean equalList(final List<? extends Proxy> list1,
                                  final List<? extends Proxy> list2)
  {
    if (list1.size() == list2.size()) {
      final Iterator<? extends Proxy> iter1 = list1.iterator();
      final Iterator<? extends Proxy> iter2 = list2.iterator();
      while (iter1.hasNext() && iter2.hasNext()) {
        final Proxy proxy1 = iter1.next();
        final Proxy proxy2 = iter2.next();
        if (!proxy1.equalsWithGeometry(proxy2)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public static boolean equalSet(final Set<? extends Proxy> set1,
                                 final Set<? extends Proxy> set2)
  {
    if (set1.equals(set2)) {
      for (final Proxy proxy1 : set1) {
        boolean found = false;
        for (final Proxy proxy2 : set2) {
          if (proxy1.equalsWithGeometry(proxy2)) {
            found = true;
            break;
          }
        }
        if (!found) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

}
