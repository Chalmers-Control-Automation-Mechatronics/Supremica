//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   MethodGlue
//###########################################################################
//# $Id: MethodGlue.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


abstract class MethodGlue implements Comparable, WritableGlue {

  //#########################################################################
  //# Constructors
  MethodGlue()
  {
    this(new ArrayList(0));
  }

  MethodGlue(final List parameters)
  {
    final List parcopy = new ArrayList(parameters);
    mParameterList = Collections.unmodifiableList(parcopy);
    mTypeSignature = null;
    mMethodNumber = -1;
    mMethodCodeSuffix = -1;
  }


  //#########################################################################
  //# equals() and hashCode()
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final MethodGlue method = (MethodGlue) partner;
      return mParameterList.equals(method.mParameterList);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mParameterList.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    final MethodGlue method = (MethodGlue) partner;
    final int result1 =
      getKindComparisonIndex() - method.getKindComparisonIndex();
    if (result1 != 0) {
      return result1;
    }
    final int result2 = getMethodName().compareTo(method.getMethodName());
    if (result2 != 0) {
      return result2;
    }
    final Iterator iter1 = mParameterList.iterator();
    final Iterator iter2 = method.mParameterList.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      final ParameterGlue param1 = (ParameterGlue) iter1.next();
      final ParameterGlue param2 = (ParameterGlue) iter2.next();
      final TypeGlue type1 = param1.getType();
      final TypeGlue type2 = param2.getType();
      final int result3 = type1.compareTo(type2);
      if (result3 != 0) {
	return result3;
      }
    }
    return (iter1.hasNext() ? 1 : 0) - (iter2.hasNext() ? 1 : 0);
  }


  //#########################################################################
  //# Simple Access
  abstract int getKindComparisonIndex();

  abstract TypeGlue getReturnType();

  abstract String getMethodName();


  //#########################################################################
  //# Type Verification
  abstract void verify(final Class javaclass, final ErrorReporter reporter);

  Class[] getParameterClasses()
  {
    if (mParameterList.isEmpty()) {
      return null;
    } else {
      final int arity = mParameterList.size();
      final Class[] result = new Class[arity];
      for (int i = 0; i < arity; i++) {
	final ParameterGlue param = (ParameterGlue) mParameterList.get(i);
	final TypeGlue type = param.getType();
	result[i] = type.getJavaClass();
      }
      return result;
    }
  }


  //#########################################################################
  //# Calculating Names
  int getMethodNumber()
  {
    return mMethodNumber;
  }

  void setMethodNumber(final int code)
  {
    if (mMethodNumber < 0) {
      mMethodNumber = code;
    } else {
      throw new IllegalStateException("Redefining method number!");
    }
  }

  int getMethodCodeSuffix()
  {
    return mMethodCodeSuffix;
  }

  void setMethodCodeSuffix(final int code)
  {
    if (mMethodCodeSuffix < 0) {
      mMethodCodeSuffix = code;
    } else {
      throw new IllegalStateException("Redefining method code suffix!");
    }
  }

  String getCppMethodName()
  {
    return getMethodName();
  }

  String getFullMethodName()
  {
    final StringBuffer buffer = new StringBuffer(getCppMethodName());
    if (mMethodCodeSuffix >= 0) {
      buffer.append('_');
      buffer.append(mMethodCodeSuffix);
    }
    return buffer.toString();
  }


  //#########################################################################
  //# Calculating Type Signatures
  void collectSignatures(final Set names, final Map signatures)
  {
    if (mTypeSignature != null) {
      throw new IllegalStateException("Second call to collectSignatures()!");
    }

    final StringBuffer buffer = new StringBuffer();
    final Iterator iter = mParameterList.iterator();
    buffer.append('(');
    while (iter.hasNext()) {
      final ParameterGlue param = (ParameterGlue) iter.next();
      param.appendTypeSignature(buffer);
    }
    buffer.append(')');
    getReturnType().appendTypeSignature(buffer);

    final String signame = buffer.toString();
    final TypeSignature foundsig = (TypeSignature) signatures.get(signame);
    if (foundsig == null) {
      mTypeSignature = new TypeSignature(signame);
      signatures.put(signame, mTypeSignature);
    } else {
      mTypeSignature = foundsig;
    }
  }


  //#########################################################################
  //# Calculating Dependencies
  void collectUsedGlue(final Set results, final Set used)
  {
    final TypeGlue returntype = getReturnType();
    returntype.collectUsedGlue(results);
    returntype.collectUsedGlue(used);
    final Iterator iter = mParameterList.iterator();
    while (iter.hasNext()) {
      final ParameterGlue param = (ParameterGlue) iter.next();
      param.collectUsedGlue(used);
    }
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    final ProcessorVariable nameproc =
      new DefaultProcessorVariable(getMethodName());
    context.registerProcessorVariable("METHODNAME", nameproc);
    final ProcessorVariable numproc =
      new DefaultProcessorVariable(getMethodNumber());
    context.registerProcessorVariable("METHODNO", numproc);
    final ProcessorVariable codenameproc =
      new DefaultProcessorVariable(getFullMethodName());
    context.registerProcessorVariable("METHODCODENAME", codenameproc);
    final ProcessorForeach foreachargproc =
      new DefaultProcessorForeach(mParameterList);
    context.registerProcessorForeach("ARG", foreachargproc);
    if (mTypeSignature != null) {
      mTypeSignature.registerProcessors(context);
    }
    final TypeGlue returntype = getReturnType();
    returntype.registerProcessors(context);
  }


  //#########################################################################
  //# Data Members
  private final List mParameterList;
  private TypeSignature mTypeSignature;
  private int mMethodNumber;
  private int mMethodCodeSuffix;

}
