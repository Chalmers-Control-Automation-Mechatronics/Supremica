//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelAnalyser
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import java.nio.ByteBuffer;

import net.sourceforge.waters.model.analysis.AbstractModelAnalyser;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The abstract base class of all native model analysers.</P>
 *
 * @author Robi Malik
 */

public abstract class NativeModelAnalyser
  extends AbstractModelAnalyser
{

  //#########################################################################
  //# Static Initialisation
  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Constructors
  public NativeModelAnalyser(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeModelAnalyser(final ProductDESProxy model,
                             final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mNativeModelAnalyzer = null;
  }


  //#########################################################################
  //# Native Methods
  public native void requestAbort();


  //#########################################################################
  //# Auxiliary Methods for Native Code
  public ByteBuffer getNativeModelAnalyser()
  {
    return mNativeModelAnalyzer;
  }

  public void setNativeModelAnalyser(final ByteBuffer buffer)
  {
    mNativeModelAnalyzer = buffer;
  }


  //#########################################################################
  //# Data Members
  private ByteBuffer mNativeModelAnalyzer;

}
