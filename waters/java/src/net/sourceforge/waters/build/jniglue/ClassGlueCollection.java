//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ClassGlue
//###########################################################################
//# $Id: ClassGlueCollection.java,v 1.1 2005-02-18 01:30:10 robi Exp $
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
  ClassGlueCollection(final Collection classes)
  {
    final Collection copy = new ArrayList(classes);
    mClasses = Collections.unmodifiableCollection(copy);
  }


  //#########################################################################
  //# Simple Access
  Collection getClasses()
  {
    return mClasses;
  }


  //#########################################################################
  //# Calculating Type Signatures
  void collectSignatures()
  {
    if (mNames == null) {
      mNames = new TreeSet();
      final Map signatures = new TreeMap();
      final Iterator iter1 = mClasses.iterator();
      while (iter1.hasNext()) {
	final ClassGlue glue = (ClassGlue) iter1.next();
	glue.collectSignatures(mNames, signatures);
      }
      mSignatures = signatures.values();
      int sigcode = 0;
      final Iterator iter2 = mSignatures.iterator();
      while (iter2.hasNext()) {
	final TypeSignature sig = (TypeSignature) iter2.next();
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
    Collection getStringCollection()
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
    public Iterator getIterator()
    {
      collectSignatures();
      return mSignatures.iterator();
    }

  }


  //#########################################################################
  //# Data Members
  private final Collection mClasses;
  private Set mNames;
  private Collection mSignatures;

}
