//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   ModelObserver
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

/**
 * @author Robi Malik
 */

public interface ModelObserver {

  public void modelChanged(ModelChangeEvent event);

}
