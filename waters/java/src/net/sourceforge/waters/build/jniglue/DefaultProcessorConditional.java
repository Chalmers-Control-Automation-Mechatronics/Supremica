//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   DefaultProcessorConditional
//###########################################################################
//# $Id: DefaultProcessorConditional.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class DefaultProcessorConditional implements ProcessorConditional {

  //#########################################################################
  //# Constructors
  DefaultProcessorConditional(final boolean cond)
  {
    mIsConditionSatisfied = cond;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.build.jniglue.ProcessorConditional
  public boolean isConditionSatisfied()
  {
    return mIsConditionSatisfied;
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsConditionSatisfied;

}