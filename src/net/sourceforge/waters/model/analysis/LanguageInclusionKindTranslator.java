//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   LanguageInclusionKindTranslator
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.Serializable;


/**
 * <P>A kind translator used for language inclusion checking.
 * This translator remaps all event to be uncontrollable,
 * all specs and supervisors are considered as plants, and all properties are
 * considered as specs. Such a remapping makes it possible to
 * implement language inclusion checking using a controllability
 * checker.</P>
 *
 * @author Robi Malik
 */

public class LanguageInclusionKindTranslator
  extends AbstractLanguageInclusionKindTranslator
  implements Serializable
{

  //#########################################################################
  //# Singleton Implementation
  public static LanguageInclusionKindTranslator getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final LanguageInclusionKindTranslator theInstance =
      new LanguageInclusionKindTranslator();
  }

  private LanguageInclusionKindTranslator()
  {
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
