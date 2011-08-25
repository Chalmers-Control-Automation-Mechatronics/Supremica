//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ExtensibleEnumFactory
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.List;

/**
 * An enumeration interface to support enumerated command line arguments.
 * There are different implementations of this interface to support standard
 * Java enumerations and user-defined extensible enumerations.
 *
 * @author Robi Malik
 */

public interface EnumFactory<E>
{

  /**
   * Gets an immutable list the items in this enumeration.
   */
  public List<? extends E> getEnumConstants();

}
