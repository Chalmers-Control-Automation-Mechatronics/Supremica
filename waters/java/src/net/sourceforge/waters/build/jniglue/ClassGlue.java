//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ClassGlue
//###########################################################################
//# $Id: ClassGlue.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


abstract class ClassGlue implements Comparable, FileWritableGlue {

  //#########################################################################
  //# Constructors
  ClassGlue(final String packname,
	    final String classname,
	    final ClassGlue baseclass,
	    final ErrorReporter reporter)
  {
    this(packname, classname, baseclass, null, reporter);
  }

  ClassGlue(final String packname,
	    final String classname,
	    final ClassGlue baseclass,
	    final ClassModifier mod,
	    final ErrorReporter reporter)
  {
    mPackName = packname;
    mClassName = classname;
    mBaseClass = baseclass;
    if (mBaseClass != null) {
      mBaseClass.mNumSubclasses++;
    }
    mMethods = new TreeSet();
    mFields = new TreeSet();
    mModifier = mod;
    mNeedsGlue = false;
    mCppClassName = null;
    mMethodList = null;
    mFieldList = null;
    mNumSubclasses = 0;
    mNumConstructors = -1;
    mJavaClass = loadClass(reporter);
  }


  //#########################################################################
  //# equals() and hashCode()
  public boolean equals(final Object partner)
  {
    if (partner != null && getClass() == partner.getClass()) {
      final ClassGlue glue = (ClassGlue) partner;
      return mClassName.equals(glue.mClassName);
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mClassName.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    final ClassGlue glue = (ClassGlue) partner;
    return mClassName.compareTo(glue.mClassName);
  }


  //#########################################################################
  //# Simple Access
  String getClassName()
  {
    return mClassName;
  }

  String getCppClassName()
  {
    if (mCppClassName == null) {
      if (mClassName.endsWith("Proxy")) {
	final int len = mClassName.length();
	mCppClassName = mClassName.substring(0, len - 5) + "Glue";
      } else {
	mCppClassName = mClassName + "Glue";
      }
    }
    return mCppClassName;
  }

  String getFullName()
  {
    return mPackName +"/" + mClassName;
  }

  String getJavaClassName()
  {
    final String fullname = getFullName();
    return fullname.replace('/', '.');
  }

  ClassGlue getBaseClass()
  {
    return mBaseClass;
  }

  boolean addMethod(final MethodGlue method, final ErrorReporter reporter)
  {
    final boolean added = mMethods.add(method);
    if (added && mJavaClass != null) {
      method.verify(mJavaClass, reporter);
    }
    return added;
  }

  boolean addField(final FieldGlue field, final ErrorReporter reporter)
  {
    final boolean added = mFields.add(field);
    if (added && mJavaClass != null) {
      field.verify(mJavaClass, reporter);
    }
    return added;
  }

  ClassModifier getModifier()
  {
    return mModifier;
  }

  boolean includesFullImplementation()
  {
    return mModifier.includesFullImplementation();
  }

  boolean checkGlueHeaders(final boolean needsglue)
  {
    if (!needsglue) {
      return true;
    } else if (mModifier == null) {
      mNeedsGlue = true;
      return true;
    } else {
      return mModifier.includesFullImplementation();
    }
  }

  void setModifier(final ClassModifier mod)
  {
    mModifier = mod;
  }

  boolean getNeedsGlue()
  {
    return mNeedsGlue;
  }

  Class getJavaClass()
  {
    return mJavaClass;
  }


  //#########################################################################
  //# Provided by Subclasses
  abstract boolean isEnum();


  //#########################################################################
  //# Calculating Type Signatures
  void collectSignatures(final Set names, final Map signatures)
  {
    final Iterator methoditer = mMethods.iterator();
    while (methoditer.hasNext()) {
      final MethodGlue method = (MethodGlue) methoditer.next();
      method.collectSignatures(names, signatures);
    }
    final Iterator fielditer = mFields.iterator();
    while (fielditer.hasNext()) {
      final FieldGlue field = (FieldGlue) fielditer.next();
      field.collectSignatures(names, signatures);
    }
  }


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.FileWritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    computeMethodIndexes();

    final ProcessorVariable nameproc =
      new DefaultProcessorVariable(mClassName);
    context.registerProcessorVariable("CLASSNAME", nameproc);
    final ProcessorVariable cppnameproc =
      new DefaultProcessorVariable(getCppClassName());
    context.registerProcessorVariable("CPPCLASSNAME", cppnameproc);
    if (mBaseClass != null) {
      final ProcessorVariable basenameproc =
	new DefaultProcessorVariable(mBaseClass.getClassName());
      context.registerProcessorVariable("BASECLASSNAME", basenameproc);
      final ProcessorVariable cppbasenameproc =
	new DefaultProcessorVariable(mBaseClass.getCppClassName());
      context.registerProcessorVariable("CPPBASECLASSNAME", cppbasenameproc);
    }
    final ProcessorVariable spcproc =
      new SpaceProcessor(getCppClassName());
    context.registerProcessorVariable("CSPC", spcproc);
    final ProcessorVariable fullnameproc =
      new DefaultProcessorVariable(getFullName());
    context.registerProcessorVariable("FULLCLASSNAME", fullnameproc);
    final ProcessorVariable nummethodsproc =
      new DefaultProcessorVariable(getNumberOfMethods());
    context.registerProcessorVariable("NUMMETHODS", nummethodsproc);
    final ProcessorVariable numfieldsproc =
      new DefaultProcessorVariable(mFields.size());
    context.registerProcessorVariable("NUMFIELDS", numfieldsproc);
    final ProcessorConditional ifrefproc =
      new DefaultProcessorConditional(mModifier.includesGlueHeaders());
    context.registerProcessorConditional("REF", ifrefproc);
    final ProcessorConditional ifglueproc =
      new DefaultProcessorConditional(mModifier.includesFullImplementation());
    context.registerProcessorConditional("GLUE", ifglueproc);
    final ProcessorConditional ifenumproc =
      new DefaultProcessorConditional(isEnum());
    context.registerProcessorConditional("ENUM", ifenumproc);
    final ProcessorConditional ifhasbaseproc =
      new DefaultProcessorConditional(mBaseClass != null);
    context.registerProcessorConditional("HASBASECLASS", ifhasbaseproc);
    final ProcessorConditional ifhassubproc =
      new DefaultProcessorConditional(mNumSubclasses != 0);
    context.registerProcessorConditional("HASSUBCLASSES", ifhassubproc);
    final ProcessorConditional ifhasmethodsproc =
      new DefaultProcessorConditional(!mMethods.isEmpty());
    context.registerProcessorConditional("HASMETHODS", ifhasmethodsproc);
    final ProcessorConditional ifhasfieldsproc =
      new DefaultProcessorConditional(!mFields.isEmpty());
    context.registerProcessorConditional("HASFIELDS", ifhasfieldsproc);
    final ProcessorConditional ifhasmembersproc =
      new DefaultProcessorConditional
            (!mMethods.isEmpty() || !mFields.isEmpty());
    context.registerProcessorConditional("HASMEMBERS", ifhasmembersproc);
    final ProcessorConditional ifhasconstrproc =
      new DefaultProcessorConditional(mNumConstructors > 0);
    context.registerProcessorConditional("HASCONSTRUCTORS", ifhasconstrproc);
    final ProcessorConditional ifhasplainproc =
      new DefaultProcessorConditional(mNumConstructors < mMethodList.size());
    context.registerProcessorConditional("HASPLAINMETHODS", ifhasplainproc);
    final ProcessorConditional ifhasglueresproc =
      new DefaultProcessorConditional(!mResultingGlue.isEmpty());
    context.registerProcessorConditional("HASGLUERESULTS", ifhasglueresproc);
    final ProcessorForeach foreachmethodproc =
      new DefaultProcessorForeach(mMethods);
    context.registerProcessorForeach("METHOD", foreachmethodproc);
    final ProcessorForeach foreachfieldproc =
      new DefaultProcessorForeach(mFields);
    context.registerProcessorForeach("FIELD", foreachfieldproc);
    final ProcessorForeach foreachconstrproc =
      new ProcessorForeachCONSTRUCTOR();
    context.registerProcessorForeach("CONSTRUCTOR", foreachconstrproc);
    final ProcessorForeach foreachplainproc =
      new ProcessorForeachPLAINMETHOD();
    context.registerProcessorForeach("PLAINMETHOD", foreachplainproc);
    final ProcessorForeach foreachincproc =
      new DefaultProcessorForeach(mUsedGlue);
    context.registerProcessorForeach("INCLUDEDGLUE", foreachincproc);
    final ProcessorForeach foreachresproc =
      new DefaultProcessorForeach(mResultingGlue);
    context.registerProcessorForeach("RESULTINGGLUE", foreachresproc);
  }

  public boolean isUpToDate(final File rootdir, final long outfiletime)
  {
    final String pathname = getFullName() + ".java";
    final File srcfilename = new File(rootdir, pathname);
    return outfiletime > srcfilename.lastModified();
  }


  //#########################################################################
  //# Type Verification
  private Class loadClass(final ErrorReporter reporter)
  {
    final String javaname = getJavaClassName();
    final ClassLoader loader = getClass().getClassLoader();
    try {
      return loader.loadClass(javaname);
    } catch (final ClassNotFoundException exception) {
      reporter.reportError("Can't find class " + javaname + "!");
      return null;
    }
  }


  //#########################################################################
  //# Method Indexing
  private int getNumberOfMethods()
  {
    int result = mMethods.size();
    if (mBaseClass != null) {
      result += mBaseClass.getNumberOfMethods();
    }
    return result;
  }

  private void computeMethodIndexes()
  {
    if (mMethodList == null) {
      mMethodList = new ArrayList(mMethods);
      mFieldList = new ArrayList(mFields);
      mNumConstructors = 0;
      mResultingGlue = new TreeSet();
      mUsedGlue = new TreeSet();
      final int nummethods = mMethodList.size();
      int nextcode = 0;
      if (mBaseClass != null) {
	nextcode += mBaseClass.getNumberOfMethods();
      }
      MethodGlue prev = null;
      for (int i = 0; i < nummethods; i++) {
	final MethodGlue method = (MethodGlue) mMethodList.get(i);
	method.setMethodNumber(nextcode++);
	if (i > 0 &&
	    prev.getCppMethodName().equals(method.getCppMethodName())) {
	  final int prevcode = prev.getMethodCodeSuffix();
	  if (prev.getMethodCodeSuffix() < 0) {
	    prev.setMethodCodeSuffix(0);
	    method.setMethodCodeSuffix(1);
	  } else {
	    method.setMethodCodeSuffix(prevcode + 1);
	  }
	}
	prev = method;
	if (method instanceof ConstructorGlue) {
	  mNumConstructors++;
	}
	method.collectUsedGlue(mResultingGlue, mUsedGlue);
      }
    }
  }


  //#########################################################################
  //# Local Class ProcessorForeachCONSTRUCTOR
  private class ProcessorForeachCONSTRUCTOR
    implements ProcessorForeach
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorForeach
    public Iterator getIterator()
    {
      final List list = mMethodList.subList(0, mNumConstructors);
      return list.iterator();
    }

  }


  //#########################################################################
  //# Local Class ProcessorForeachPLAINMETHOD
  private class ProcessorForeachPLAINMETHOD
    implements ProcessorForeach
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorForeach
    public Iterator getIterator()
    {
      final List list =
	mMethodList.subList(mNumConstructors, mMethodList.size());
      return list.iterator();
    }

  }


  //#########################################################################
  //# Data Members
  private final String mPackName;
  private final String mClassName;
  private final ClassGlue mBaseClass;
  private final Set mMethods;
  private final Set mFields;
  private final Class mJavaClass;
  private ClassModifier mModifier;
  private boolean mNeedsGlue;
  private String mCppClassName;
  private List mMethodList;
  private List mFieldList;
  private int mNumSubclasses;
  private int mNumConstructors;
  private Set mResultingGlue;
  private Set mUsedGlue;
   
}
