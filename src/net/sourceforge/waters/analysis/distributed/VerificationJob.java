package net.sourceforge.waters.analysis.distributed;

import java.io.Serializable;
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.model.des.ProductDESProxy;

public class VerificationJob extends Job
{
  public VerificationJob()
  {
    super();
  }

  public VerificationJob(Job other)
  {
    super(other);
  }

  public ProductDESProxy getModel()
  {
    return (ProductDESProxy)get(MODEL_ATTR);
  }

  public void setModel(ProductDESProxy model)
  {
    //It is assumed that all Waters ProductDESProxy models are
    //serializable, even though the interface itself doesn't
    //specify serializable (it is specified in the implementations)
    set(MODEL_ATTR, (Serializable)model);
  }

  public static String MODEL_ATTR = "waters-model";
}