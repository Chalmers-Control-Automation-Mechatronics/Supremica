//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   PlainMethodGlue
//###########################################################################
//# $Id: PlainMethodGlue.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;


class PlainMethodGlue extends MethodGlue {

  //#########################################################################
  //# Constructors
  PlainMethodGlue(final TypeGlue returntype,
		  final String name)
  {
    mReturnType = returntype;
    mMethodName = name;
  }

  PlainMethodGlue(final TypeGlue returntype,
		  final String name,
		  final List parameters)
  {
    super(parameters);
    mReturnType = returntype;
    mMethodName = name;
  }


  //#########################################################################
  //# equals() and hashCode()
  public boolean equals(final Object partner)
  {
    if (super.equals(partner)) {
      final PlainMethodGlue method = (PlainMethodGlue) partner;
      return mMethodName.equals(method.mMethodName);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mMethodName.hashCode() + 5 * super.hashCode();
  }


  //#########################################################################
  //# Simple Access
  int getKindComparisonIndex()
  {
    return 1;
  }

  TypeGlue getReturnType()
  {
    return mReturnType;
  }

  String getMethodName()
  {
    return mMethodName;
  }

  Class getJavaType()
  {
    return mReturnType.getJavaClass();
  }


  //#########################################################################
  //# Type Verification
  void verify(final Class javaclass, final ErrorReporter reporter)
  {
    try {
      final Class[] paramtypes = getParameterClasses();
      final Method method = javaclass.getMethod(mMethodName, paramtypes);
      final Class methodtype = method.getReturnType();
      final Class gluetype = getJavaType();
      if (gluetype != null && gluetype != methodtype) {
	reporter.reportError
	  ("Method " + mMethodName + "() in class " + javaclass.getName() +
	   " does not have return type " + gluetype.getName() + "!");
      }
    } catch (final NoSuchMethodException exception) {
      reporter.reportError
	("Can't find method " + mMethodName + "() in class " +
	 javaclass.getName() + "!");
    }
  }


  //#########################################################################
  //# Calculating Type Signatures
  void collectSignatures(final Set names, final Map signatures)
  {
    super.collectSignatures(names, signatures);
    names.add(mMethodName);
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    super.registerProcessors(context);
    final ProcessorConditional ifconstructorproc =
      new DefaultProcessorConditional(false);
    context.registerProcessorConditional("ISCONSTRUCTOR", ifconstructorproc);
    final ProcessorVariable spcproc = new SpaceProcessor(mMethodName);
    context.registerProcessorVariable("MSPC", spcproc);
  }


  //#########################################################################
  //# Data Members
  private final TypeGlue mReturnType;
  private final String mMethodName;

}
