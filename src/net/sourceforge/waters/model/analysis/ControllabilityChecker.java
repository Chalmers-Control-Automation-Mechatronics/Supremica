//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;


/**
 * A model verifier that checks its input for controllability.
 * This model verifier checks whether the spec components in its input
 * model is controllable with respect to the plant components in the input
 * model.
 * 
 * @author Robi Malik
 */

public interface ControllabilityChecker extends SafetyVerifier
{

}
