//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ObjectClassGlue
//###########################################################################
//# $Id: ObjectClassGlue.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.Collections;
import java.util.List;


class ObjectClassGlue extends PlainClassGlue {

  //#########################################################################
  //# Constructors
  ObjectClassGlue(final ErrorReporter reporter)
  {
    super("java/lang", "Object", null, ClassModifier.M_GLUE, reporter);
    final TypeGlue type = new ClassTypeGlue(this);
    final ParameterGlue param = new ParameterGlue("partner", type);
    final List<ParameterGlue> parameters = Collections.singletonList(param);
    final MethodGlue equalsmethod =
      new PlainMethodGlue(SimpleTypeGlue.TYPE_BOOLEAN, "equals", parameters);
    addMethod(equalsmethod, reporter);
    final MethodGlue hashmethod =
      new PlainMethodGlue(SimpleTypeGlue.TYPE_INT, "hashCode");
    addMethod(hashmethod, reporter);
  }
   
}
