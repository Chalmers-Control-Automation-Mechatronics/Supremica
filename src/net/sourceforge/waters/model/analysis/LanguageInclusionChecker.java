//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   LanguageInclusionChecker
//###########################################################################
//# $Id: LanguageInclusionChecker.java,v 1.1 2006-11-03 01:00:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * A model verifier that checks its input for controllability.
 * This model verifier checks whether the behaviour of all plant and spec
 * components in its input model is contained in the behavior of all
 * properties in the input model.
 * 
 * @author Robi Malik
 */

public interface LanguageInclusionChecker extends SafetyVerifier
{

}
