//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
