//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   IdenticalKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.Serializable;


/**
 * <P>A kind translator that does not change component and event
 * attributes. This simple kind translator implementation is used
 * for simple controllability checks.</P>
 *
 * @author Robi Malik
 */

public class IdenticalKindTranslator
  extends AbstractKindTranslator
  implements Serializable
{

  //#########################################################################
  //# Singleton Implementation
  public static IdenticalKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final IdenticalKindTranslator theInstance =
      new IdenticalKindTranslator();
  }

  private IdenticalKindTranslator()
  {
  }


  //#########################################################################
  //# Singleton Implementation
  private static final long serialVersionUID = 1L;

}
