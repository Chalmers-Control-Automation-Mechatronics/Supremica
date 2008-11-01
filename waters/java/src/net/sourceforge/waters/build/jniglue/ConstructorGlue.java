//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ConstructorGlue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.lang.reflect.Constructor;
import java.util.List;


class ConstructorGlue extends MethodGlue {

  //#########################################################################
  //# Constructors
  ConstructorGlue()
  {
  }

  ConstructorGlue(final List<ParameterGlue> parameters)
  {
    super(parameters);
  }


  //#########################################################################
  //# Simple Access
  int getKindComparisonIndex()
  {
    return 0;
  }

  boolean isStatic()
  {
    return false;
  }

  TypeGlue getReturnType()
  {
    return SimpleTypeGlue.TYPE_VOID;
  }

  String getMethodName()
  {
    return "<init>";
  }

  String getCppMethodName()
  {
    return "_init";
  }


  //#########################################################################
  //# Type Verification
  void verify(final Class<?> javaclass, final ErrorReporter reporter)
  {
    try {
      final Class[] paramtypes = getParameterClasses();
      final Constructor constructor = javaclass.getConstructor(paramtypes);
    } catch (final NoSuchMethodException exception) {
      reporter.reportError
        ("Can't find this constructor in class " + javaclass.getName() + "!");
    }
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    super.registerProcessors(context);
    final ProcessorConditional ifconstructorproc =
      new DefaultProcessorConditional(true);
    context.registerProcessorConditional("ISCONSTRUCTOR", ifconstructorproc);
  }

}
