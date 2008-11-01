//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   IdentifiedProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * The common interface for elements that use a complex name.
 *
 * This interface represents all those elements in a module whose name is
 * not stored as a string but as an identifier ({@link
 * IdentifiedProxy}). This includes elements that can have indexed names
 * such as components or aliases.
 *
 * @author Robi Malik
 */
// @short identified object

public interface IdentifiedProxy extends NamedProxy
{

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the identifier defining the name of this object.
   */
  public IdentifierProxy getIdentifier();

}
