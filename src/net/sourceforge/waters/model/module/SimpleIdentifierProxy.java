//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   SimpleIdentifierProxy
//###########################################################################
//# $Id: SimpleIdentifierProxy.java,v 1.3 2007-11-19 02:16:52 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.module;


/**
 * A simple identifier.
 *
 * A simple identifier is an identifier that only consists of a single
 * name. It has no structure or indexes.
 *
 * @author Robi Malik
 */

public interface SimpleIdentifierProxy extends IdentifierProxy {

  public SimpleIdentifierProxy clone();

}
