//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.util.Set;


class ClassTypeGlue extends TypeGlue {

  //#########################################################################
  //# Constructors
  ClassTypeGlue(final ClassGlue clazz)
  {
    this(clazz, false);
  }

  ClassTypeGlue(final ClassGlue clazz, final boolean useglue)
  {
    mClass = clazz;
    mUseGlue = useglue;
  }


  //#########################################################################
  //# equals() and hashCode()
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final ClassTypeGlue type = (ClassTypeGlue) partner;
      return mClass == type.mClass && mUseGlue == type.mUseGlue;
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    int result = mClass.hashCode();
    if (mUseGlue) {
      result++;
    }
    return result;
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final TypeGlue partner)
  {
    if (partner instanceof ClassTypeGlue) {
      final ClassTypeGlue classtype = (ClassTypeGlue) partner;
      final int classcmp = mClass.compareTo(classtype.mClass);
      if (classcmp != 0) {
	return classcmp;
      } else if (mUseGlue) {
	return classtype.mUseGlue ? 0 : 1;
      } else {
	return classtype.mUseGlue ? -1 : 0;
      }
    } else {
      return 1;
    }
  }


  //#########################################################################
  //# Simple Access
  String getCppTypeName()
  {
    return "jobject";
  }

  String getJNITypeName()
  {
    return getCppTypeName();
  }

  String getGlueTypeName()
  {
    if (mUseGlue) {
      return mClass.getCppClassName();
    } else {
      return getCppTypeName();
    }
  }

  void appendTypeSignature(final StringBuilder buffer)
  {
    buffer.append('L');
    buffer.append(mClass.getFullName());
    buffer.append(';');
  }

  String getJNICallPart()
  {
    return "Object";
  }

  Class<?> getJavaClass()
  {
    return mClass.getJavaClass();
  }


  //#########################################################################
  //# Type Flags
  boolean isVoid()
  {
    return false;
  }

  boolean isBoolean()
  {
    return false;
  }

  boolean isString()
  {
    return false;
  }

  boolean isGlue()
  {
    return mUseGlue;
  }

  boolean isEnum()
  {
    return mClass instanceof EnumClassGlue;
  }


  //#########################################################################
  //# Calculating Dependencies
  void collectUsedGlue(final Set<ClassGlue> used)
  {
    if (mUseGlue) {
      used.add(mClass);
    }
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    super.registerProcessors(context);
    final String name = mClass.getClassName();
    final ProcessorVariable jtypeproc = new DefaultProcessorVariable(name);
    context.registerProcessorVariable("JAVATYPENAME", jtypeproc);
    final ProcessorVariable jspcproc = new SpaceProcessor(name);
    context.registerProcessorVariable("JSPC", jspcproc);
  }


  //#########################################################################
  //# Data Members
  private final ClassGlue mClass;
  private final boolean mUseGlue;

}
