//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;


class FieldGlue implements Comparable<FieldGlue>, WritableGlue {

  //#########################################################################
  //# Constructors
  FieldGlue(final TypeGlue type, final String name)
  {
    mType = type;
    mFieldName = name;
    mTypeSignature = null;
  }


  //#########################################################################
  //# equals() and hashCode()
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final FieldGlue field = (FieldGlue) partner;
      return mFieldName.equals(field.mFieldName);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mFieldName.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final FieldGlue field)
  {
    return mFieldName.compareTo(field.mFieldName);
  }


  //#########################################################################
  //# Simple Access
  TypeGlue getType()
  {
    return mType;
  }

  String getFieldName()
  {
    return mFieldName;
  }

  Class<?> getJavaType()
  {
    return mType.getJavaClass();
  }


  //#########################################################################
  //# Type Verification
  void verify(final Class<?> javaclass, final ErrorReporter reporter)
  {
    try {
      final Field field = javaclass.getField(mFieldName);
      final Class<?> fieldtype = field.getType();
      final int mod = field.getModifiers();
      final Class<?> gluetype = getJavaType();
      if (gluetype != null && gluetype != fieldtype) {
        reporter.reportError
          ("Field " + mFieldName + " in class " + javaclass.getName() +
           " is not of type " + gluetype.getName() + "!");
      } else if (!Modifier.isPublic(mod) ||
                 !Modifier.isStatic(mod) ||
                 !Modifier.isFinal(mod) ||
                 Modifier.isStrict(mod) ||
                 Modifier.isTransient(mod) ||
                 Modifier.isVolatile(mod)) {
        reporter.reportError
          ("Field " + mFieldName + " in class " + javaclass.getName() +
           " is not public static final, is " + Modifier.toString(mod) + "!");
      }
    } catch (final NoSuchFieldException exception) {
      reporter.reportError
        ("Can't find field " + mFieldName + " in class " +
         javaclass.getName() + "!");
    }
  }


  //#########################################################################
  //# Calculating Type Signatures
  void collectSignatures(final Set<String> names,
                         final Map<String,TypeSignature> signatures)
  {
    if (mTypeSignature != null) {
      throw new IllegalStateException("Second call to collectSignatures()!");
    }

    final StringBuilder buffer = new StringBuilder();
    mType.appendTypeSignature(buffer);
    final String signame = buffer.toString();
    final TypeSignature foundsig = (TypeSignature) signatures.get(signame);
    if (foundsig == null) {
      mTypeSignature = new TypeSignature(signame);
      signatures.put(signame, mTypeSignature);
    } else {
      mTypeSignature = foundsig;
    }

    names.add(mFieldName);
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    final ProcessorVariable nameproc =
      new DefaultProcessorVariable(mFieldName);
    context.registerProcessorVariable("FIELDNAME", nameproc);
    mType.registerProcessors(context);
    if (mTypeSignature != null) {
      mTypeSignature.registerProcessors(context);
    }
  }


  //#########################################################################
  //# Data Members
  private final TypeGlue mType;
  private final String mFieldName;
  private TypeSignature mTypeSignature;

}








