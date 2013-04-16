package net.sourceforge.waters.analysis.distributed;

import java.io.Serializable;
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.model.analysis.des.SerializableKindTranslator;
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

  public SerializableKindTranslator getKindTranslator()
  {
    return (SerializableKindTranslator)get(KIND_XLATOR_ATTR);
  }

  public void setKindTranslator(SerializableKindTranslator translator)
  {
    set(KIND_XLATOR_ATTR, translator);
  }

  public Integer getWalltimeLimit()
  {
    return (Integer)get(WALLTIME_LIMIT_ATTR);
  }

  public void setWalltimeLimit(Integer limit)
  {
    set(WALLTIME_LIMIT_ATTR, limit);
  }


  //#########################################################################
  //# Class Constants
  public static final String MODEL_ATTR = "waters-model";
  public static final String KIND_XLATOR_ATTR = "waters-kindtranslator";
  public static final String WALLTIME_LIMIT_ATTR = "job-walltime-limit";

  private static final long serialVersionUID = 1L;

}