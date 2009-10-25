//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.cpp.analysis
//# CLASS:   NativeModelVerifier
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.cpp.analysis;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;


/**
 * @author Robi Malik
 */

public abstract class NativeModelVerifier
  extends NativeModelAnalyser
  implements ModelVerifier
{

  //#########################################################################
  //# Constructors
  public NativeModelVerifier(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public NativeModelVerifier(final ProductDESProxy model,
			     final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mExplorerMode = ExplorerMode.BEST_GUESS;
    mResult = null;
  }


  //#########################################################################
  //# Configuration
  public void setExplorerMode(final ExplorerMode mode)
  {
    mExplorerMode = mode;
  }

  public ExplorerMode getExplorerMode()
  {
    return mExplorerMode;
  }


  //#########################################################################
  //# Invocation
  public boolean run()
    throws AnalysisException
  {
    if (getModel() == null) {
      throw new NullPointerException("No model given!");
    } else {
      mResult = null;
      mResult = runNativeAlgorithm();
      return mResult.isSatisfied();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifier
  public boolean isSatisfied()
  {
    if (mResult != null) {
      return mResult.isSatisfied();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  public TraceProxy getCounterExample()
  {
    if (isSatisfied()) {
      throw new IllegalStateException("No trace for satisfied property!");
    } else {
      return mResult.getCounterExample();
    }
  }

  public VerificationResult getAnalysisResult()
  {
    return mResult;
  }

  public void clearAnalysisResult()
  {
    mResult = null;
  }


  //#########################################################################
  //# Native Methods
  abstract VerificationResult runNativeAlgorithm() throws AnalysisException;

  public abstract String getTraceName();


  //#########################################################################
  //# Auxiliary Methods
  /*
  private void dumpModel()
  {
    try {
      final ProductDESProxy model = getModel();
      // should also replace event and component kinds
      // according to KindTranslator ...
      final ProductDESProxyFactory factory = getFactory();
      final net.sourceforge.waters.model.marshaller.
        ProxyMarshaller<ProductDESProxy> marshaller =
        new net.sourceforge.waters.model.marshaller.
        JAXBProductDESMarshaller(factory);
      final int code = model.hashCodeByContents();
      final java.io.File file = new java.io.File("failed" + code + ".wdes");
      marshaller.marshal(model, file);
    } catch (final java.io.IOException exception) {
      // ignore
    } catch (final javax.xml.bind.JAXBException exception) {
      // ignore
    } catch (final org.xml.sax.SAXException exception) {
      // ignore
    } catch (final
             net.sourceforge.waters.model.marshaller.WatersMarshalException
             exception) {
      // ignore
    }
  }
  */


  //#########################################################################
  //# Data Members
  private ExplorerMode mExplorerMode;
  private VerificationResult mResult;

}
