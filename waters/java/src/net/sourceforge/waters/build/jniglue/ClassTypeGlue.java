//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ClassTypeGlue
//###########################################################################
//# $Id: ClassTypeGlue.java,v 1.1 2005-02-18 01:30:10 robi Exp $
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
      return mClass == type.mClass;
    } else {
      return false;
    }
  }

  public int hashCode()
  {
    return mClass.hashCode();
  }


  //#########################################################################
  //# Interface java.lang.Comparable
  public int compareTo(final Object partner)
  {
    if (partner instanceof ClassTypeGlue) {
      final ClassTypeGlue classtype = (ClassTypeGlue) partner;
      return mClass.compareTo(classtype.mClass);
    } else if (partner instanceof TypeGlue) {
      return 1;
    } else {
      throw new ClassCastException
	("Unknown type to compare to: " + partner.getClass().getName() + "!");
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

  void appendTypeSignature(final StringBuffer buffer)
  {
    buffer.append('L');
    buffer.append(mClass.getFullName());
    buffer.append(';');
  }

  String getJNICall()
  {
    return "CallObjectMethod";
  }

  Class getJavaClass()
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
  void collectUsedGlue(final Set used)
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
