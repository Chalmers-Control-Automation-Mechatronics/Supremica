//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ParameterGlue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.Set;


class ParameterGlue implements WritableGlue {

  //#########################################################################
  //# Constructors
  ParameterGlue(final String name, final TypeGlue type)
  {
    mName = name;
    mType = type;
  }


  //#########################################################################
  //# equals() and hashCode()
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final ParameterGlue param = (ParameterGlue) partner;
      return mType.equals(param.mType);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mType.hashCode();
  }


  //#########################################################################
  //# Simple Access
  public String getName()
  {
    return mName;
  }

  public TypeGlue getType()
  {
    return mType;
  }

  public void appendTypeSignature(final StringBuffer buffer)
  {
    mType.appendTypeSignature(buffer);
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    final ProcessorVariable nameproc =
      new DefaultProcessorVariable(mName);
    context.registerProcessorVariable("ARGNAME", nameproc);
    mType.registerProcessors(context);
  }


  //#########################################################################
  //# Calculating Dependencies
  void collectUsedGlue(final Set<ClassGlue> used)
  {
    mType.collectUsedGlue(used);
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final TypeGlue mType;

}
