//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.module
//# CLASS:   GeometryElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.base;

import net.sourceforge.waters.model.base.GeometryProxy;


/**
 * An immutable implementation of the {@link GeometryProxy} Interface.
 *
 * @author Robi Malik
 */

public abstract class GeometryElement
  extends Element
  implements GeometryProxy
{

  //#########################################################################
  //# Constructor
  protected GeometryElement()
  {
  }

}
