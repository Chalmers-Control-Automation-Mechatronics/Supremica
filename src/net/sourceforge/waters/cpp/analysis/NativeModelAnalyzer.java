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

import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzer;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The abstract base class of all native model analysers.</P>
 *
 * @author Robi Malik
 */

public abstract class NativeModelAnalyzer
  extends AbstractModelAnalyzer
{

  //#########################################################################
  //# Static Initialisation
  static {
    System.loadLibrary("waters");
  }


  //#########################################################################
  //# Constructors
  public NativeModelAnalyzer(final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  public NativeModelAnalyzer(final ProductDESProxy model,
                             final ProductDESProxyFactory factory,
                             final KindTranslator translator)
  {
    super(model, factory, translator);
    mNativeModelAnalyzer = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  public boolean supportsNondeterminism()
  {
    return true;
  }


  //#########################################################################
  //# Native Methods
  public native void requestAbort();

  public static native long getPeakMemoryUsage();


  //#########################################################################
  //# Auxiliary Methods for Native Code
  public ByteBuffer getNativeModelAnalyzer()
  {
    return mNativeModelAnalyzer;
  }

  public void setNativeModelAnalyzer(final ByteBuffer buffer)
  {
    mNativeModelAnalyzer = buffer;
  }


  //#########################################################################
  //# Data Members
  private ByteBuffer mNativeModelAnalyzer;

}
