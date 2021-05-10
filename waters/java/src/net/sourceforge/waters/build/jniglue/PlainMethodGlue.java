//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.build.jniglue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;


class PlainMethodGlue extends MethodGlue {

  //#########################################################################
  //# Constructors
  PlainMethodGlue(final TypeGlue returntype,
                  final String name)
  {
    mIsStatic = false;
    mReturnType = returntype;
    mMethodName = name;
  }

  PlainMethodGlue(final TypeGlue returntype,
                  final String name,
                  final List<ParameterGlue> parameters)
  {
    this(false, returntype, name, parameters);
  }

  PlainMethodGlue(final boolean isstatic,
                  final TypeGlue returntype,
                  final String name,
                  final List<ParameterGlue> parameters)
  {
    super(parameters);
    mIsStatic = isstatic;
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
    return mIsStatic ? 2 : 1;
  }

  boolean isStatic()
  {
    return mIsStatic;
  }

  TypeGlue getReturnType()
  {
    return mReturnType;
  }

  String getMethodName()
  {
    return mMethodName;
  }

  Class<?> getJavaType()
  {
    return mReturnType.getJavaClass();
  }

  String getJNICallName()
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("Call");
    if (isStatic()) {
      buffer.append("Static");
    }
    final TypeGlue type = getReturnType();
    final String part = type.getJNICallPart();
    buffer.append(part);
    buffer.append("Method");
    return buffer.toString();
  }


  //#########################################################################
  //# Type Verification
  void verify(final Class<?> javaclass, final ErrorReporter reporter)
  {
    try {
      final Class<?>[] paramtypes = getParameterClasses();
      final Method method = javaclass.getMethod(mMethodName, paramtypes);
      final Class<?> methodtype = method.getReturnType();
      final Class<?> gluetype = getJavaType();
      if (gluetype != null && gluetype != methodtype) {
        reporter.reportError
          ("Method " + mMethodName + "() in class " + javaclass.getName() +
           " does not have return type " + gluetype.getName() + "!");
      }
      final boolean isstatic = (method.getModifiers() & Modifier.STATIC) != 0;
      if (mIsStatic != isstatic) {
        if (mIsStatic) {
          reporter.reportError
            ("Method " + mMethodName + "() in class " + javaclass.getName() +
             " is not static as expected!");
        } else {
          reporter.reportError
            ("Method " + mMethodName + "() in class " + javaclass.getName() +
             " is static but should not be!");
        }
      }
    } catch (final NoSuchMethodException exception) {
      reporter.reportError
        ("Can't find method " + mMethodName + "() in class " +
         javaclass.getName() + "!");
    }
  }


  //#########################################################################
  //# Calculating Type Signatures
  void collectSignatures(final Set<String> names,
			 final Map<String,TypeSignature> signatures)
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
    final ProcessorConditional ifstaticproc =
      new DefaultProcessorConditional(mIsStatic);
    context.registerProcessorConditional("ISSTATIC", ifstaticproc);
    final ProcessorVariable spcproc = new SpaceProcessor(mMethodName);
    context.registerProcessorVariable("MSPC", spcproc);
    final ProcessorVariable jnicallproc =
      new DefaultProcessorVariable(getJNICallName());
    context.registerProcessorVariable("JNICALLNAME", jnicallproc);
  }


  //#########################################################################
  //# Data Members
  private final boolean mIsStatic;
  private final TypeGlue mReturnType;
  private final String mMethodName;

}
