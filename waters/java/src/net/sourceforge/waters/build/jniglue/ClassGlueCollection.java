//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ClassGlue
//###########################################################################
//# $Id$
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
