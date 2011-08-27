//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ListedEnumFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/**
 * An extensible implementation of the {@link EnumFactory} interface.
 * This enumeration factory simply maintains a list of registered values.
 *
 * @author Robi Malik
 */

public class ListedEnumFactory<E> extends EnumFactory<E>
{

  //#########################################################################
  //# Constructors
  protected ListedEnumFactory()
  {
    mRegisteredElements = new LinkedList<E>();
  }


  //#########################################################################
  //# Initialisation
  protected void register(final E item)
  {
    mRegisteredElements.add(item);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.EnumFactory
  public List<E> getEnumConstants()
  {
    return Collections.unmodifiableList(mRegisteredElements);
  }


  //#########################################################################
  //# Data Members
  private final List<E> mRegisteredElements;

}
