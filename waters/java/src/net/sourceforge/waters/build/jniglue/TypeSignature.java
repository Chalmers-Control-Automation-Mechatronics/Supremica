//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TypeSignature
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;


class TypeSignature implements Comparable<Object>, WritableGlue {

  //#########################################################################
  //# Constructors
  TypeSignature(final String name)
  {
    mName = name;
    mCode = -1;
  }


  //#########################################################################
  //# Equals and HashCode
  public boolean equals(final Object partner)
  {
    if (partner != null && partner.getClass() == getClass()) {
      final TypeSignature sig = (TypeSignature) partner;
      return mName.equals(sig.mName);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mName.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    final TypeSignature sig = (TypeSignature) partner;
    return mName.compareTo(sig.mName);
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }

  int getCode()
  {
    return mCode;
  }

  void setCode(final int code)
  {
    if (mCode < 0) {
      mCode = code;
    } else {
      throw new IllegalStateException("Re-assigning signature code!");
    }
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    final ProcessorVariable nameproc = new DefaultProcessorVariable(mName);
    context.registerProcessorVariable("TYPESIGNATURE", nameproc);
    if (mCode >= 0) {
      final ProcessorVariable codeproc = new DefaultProcessorVariable(mCode);
      context.registerProcessorVariable("TYPESIGCODE", codeproc);
    }
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private int mCode;

}
