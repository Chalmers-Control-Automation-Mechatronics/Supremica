//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.base
//# CLASS:   NamedProxy
//###########################################################################
//# $Id: NamedProxy.java,v 1.2 2005-11-03 01:24:15 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.base;

/**
 * The common functionality for all Waters elements that are identified
 * by a name. 
 * Several elements in Waters have got some kind of name that is used to
 * find them within some list or set. This interface defines the required
 * functionality in a uniform way.
 *
 * @author Robi Malik
 */

public interface NamedProxy
  extends ComparableProxy<NamedProxy>
{

  //#########################################################################
  //# Cloning
  public NamedProxy clone();


  //#########################################################################
  //# Accessing the Name
  /**
   * Returns the name of this element.
   */
  public String getName();

  /**
   * Checks whether two elements have the same name.
   * This method considers two elements as equal if they have the same name.
   */
  public boolean refequals(NamedProxy partner);

}
