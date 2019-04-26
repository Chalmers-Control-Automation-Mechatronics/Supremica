//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.analysis.distributed;

import java.io.Serializable;
import net.sourceforge.waters.analysis.distributed.application.Job;
import net.sourceforge.waters.model.analysis.SerializableKindTranslator;
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
