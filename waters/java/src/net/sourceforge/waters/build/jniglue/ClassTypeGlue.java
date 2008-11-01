//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   ClassTypeGlue
//###########################################################################
//# $Id$
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

  void appendTypeSignature(final StringBuffer buffer)
  {
    buffer.append('L');
    buffer.append(mClass.getFullName());
    buffer.append(';');
  }

  String getJNICallPart()
  {
    return "Object";
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
