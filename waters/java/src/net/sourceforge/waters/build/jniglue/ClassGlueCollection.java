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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;


class ClassGlueCollection implements FileWritableGlue {

  //#########################################################################
  //# Constructors
  ClassGlueCollection(final Collection<ClassGlue> classes)
  {
    final Collection<ClassGlue> copy = new ArrayList<ClassGlue>(classes);
    mClasses = Collections.unmodifiableCollection(copy);
  }


  //#########################################################################
  //# Simple Access
  Collection<ClassGlue> getClasses()
  {
    return mClasses;
  }


  //#########################################################################
  //# Calculating Type Signatures
  void collectSignatures()
  {
    if (mNames == null) {
      mNames = new TreeSet<String>();
      final Map<String,TypeSignature> signatures =
	new TreeMap<String,TypeSignature>();
      for (final ClassGlue glue : mClasses) {
        glue.collectSignatures(mNames, signatures);
      }
      mSignatures = signatures.values();
      int sigcode = 0;
      for (final TypeSignature sig : mSignatures) {
        sig.setCode(sigcode++);
      }      
    }
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.FileWritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    final ProcessorForeach foreachclassproc =
      new DefaultProcessorForeach(mClasses);
    context.registerProcessorForeach("CLASS", foreachclassproc);
    final ProcessorForeach foreachnameproc =
      new ProcessorForeachPLAINMETHODNAME();
    context.registerProcessorForeach("PLAINMETHODNAME", foreachnameproc);
    final ProcessorForeach foreachsigproc =
      new ProcessorForeachTYPESIGNATURE();
    context.registerProcessorForeach("TYPESIGNATURE", foreachsigproc);
  }

  public boolean isUpToDate(final File rootdir, final long outfiletime)
  {
    return true;
  }


  //#########################################################################
  //# Local Class ProcessorForeachPLAINMETHODNAME
  private class ProcessorForeachPLAINMETHODNAME
    extends StringProcessorForeach
  {

    //#######################################################################
    //# Constructors
    private ProcessorForeachPLAINMETHODNAME()
    {
      super("METHODNAME");
    }

    //#######################################################################
    //# Overrides for abstract baseclass
    //# net.sourceforge.waters.build.jniglue.StringProcessorForeach
    Collection<String> getStringCollection()
    {
      collectSignatures();
      return mNames;
    }

  }


  //#########################################################################
  //# Local Class ProcessorForeachTYPESIGNATURE
  private class ProcessorForeachTYPESIGNATURE
    implements ProcessorForeach
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorForeach
    public Iterator<TypeSignature> getIterator()
    {
      collectSignatures();
      return mSignatures.iterator();
    }

  }


  //#########################################################################
  //# Data Members
  private final Collection<ClassGlue> mClasses;
  private Set<String> mNames;
  private Collection<TypeSignature> mSignatures;

}
