//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ModelObserver
//###########################################################################
//# $Id: ModelObserver.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

/**
 * @author Robi Malik
 */

public interface ModelObserver {

  public void modelChanged(ModelChangeEvent event);

}
