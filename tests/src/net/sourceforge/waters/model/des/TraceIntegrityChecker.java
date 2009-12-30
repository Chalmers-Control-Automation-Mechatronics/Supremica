//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.des
//# CLASS:   TraceIntegrityChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.des;

import net.sourceforge.waters.model.marshaller.DocumentIntegrityChecker;


public class TraceIntegrityChecker
  extends DocumentIntegrityChecker<TraceProxy>
{

  //#########################################################################
  //# Singleton Pattern
  public static TraceIntegrityChecker getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final TraceIntegrityChecker INSTANCE =
      new TraceIntegrityChecker();
  }


  //#########################################################################
  //# Constructor
  protected TraceIntegrityChecker()
  {
  }

}
