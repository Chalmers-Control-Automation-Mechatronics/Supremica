package net.sourceforge.waters.despot;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;

public class SICPropertyVVerifier implements ModelVerifier
{

  public VerificationResult getAnalysisResult()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public TraceProxy getCounterExample()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isSatisfied()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void clearAnalysisResult()
  {
    // TODO Auto-generated method stub

  }

  public ProductDESProxyFactory getFactory()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public ProductDESProxy getModel()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public int getNodeLimit()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getTransitionLimit()
  {
    // TODO Auto-generated method stub
    return 0;
  }

  public boolean isAborting()
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void requestAbort()
  {
    // TODO Auto-generated method stub

  }

  public boolean run() throws AnalysisException
  {
    // TODO Auto-generated method stub
    return false;
  }

  public void setModel(ProductDESProxy model)
  {
    // TODO Auto-generated method stub

  }

  public void setModel(AutomatonProxy aut)
  {
    // TODO Auto-generated method stub

  }

  public void setNodeLimit(int limit)
  {
    // TODO Auto-generated method stub

  }

  public void setTransitionLimit(int limit)
  {
    // TODO Auto-generated method stub

  }

}
