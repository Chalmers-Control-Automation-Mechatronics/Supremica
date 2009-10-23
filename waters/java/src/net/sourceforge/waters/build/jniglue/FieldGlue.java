//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   FieldGlue
//###########################################################################
//# $Id$
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

    final StringBuffer buffer = new StringBuffer();
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
