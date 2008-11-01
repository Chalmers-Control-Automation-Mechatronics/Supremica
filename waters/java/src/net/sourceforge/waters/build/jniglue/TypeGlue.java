//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TypeGlue
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.Set;


abstract class TypeGlue implements Comparable<TypeGlue>, WritableGlue {

  //#########################################################################
  //# Simple Access
  abstract String getCppTypeName();

  abstract String getJNITypeName();

  abstract String getGlueTypeName();

  abstract void appendTypeSignature(StringBuffer buffer);

  abstract String getJNICallPart();

  abstract Class getJavaClass();


  //#########################################################################
  //# Type Flags
  abstract boolean isVoid();

  abstract boolean isBoolean();

  abstract boolean isString();

  abstract boolean isGlue();

  abstract boolean isEnum();


  //#########################################################################
  //# Calculating Dependencies
  abstract void collectUsedGlue(final Set<ClassGlue> used);


  //#########################################################################
  //# interface net.sourceforge.waters.build.jniglue.WritableGlue
  public void registerProcessors(final TemplateContext context)
  {
    final ProcessorVariable cpptypeproc =
      new DefaultProcessorVariable(getCppTypeName());
    context.registerProcessorVariable("CPPTYPENAME", cpptypeproc);
    final ProcessorVariable jnitypeproc =
      new DefaultProcessorVariable(getJNITypeName());
    context.registerProcessorVariable("JNITYPENAME", jnitypeproc);
    final ProcessorVariable tspcproc =
      new SpaceProcessor(getCppTypeName());
    context.registerProcessorVariable("TSPC", tspcproc);
    final ProcessorVariable gspcproc =
      new SpaceProcessor(getGlueTypeName());
    context.registerProcessorVariable("GSPC", gspcproc);
    final ProcessorVariable gluetypeproc =
      new DefaultProcessorVariable(getGlueTypeName());
    context.registerProcessorVariable("GLUETYPENAME", gluetypeproc);
    final ProcessorConditional ifnonvoidproc =
      new DefaultProcessorConditional(!isVoid());
    context.registerProcessorConditional("NONVOID", ifnonvoidproc);
    final ProcessorConditional ifstringproc =
      new DefaultProcessorConditional(isString());
    context.registerProcessorConditional("STRING", ifstringproc);
    final ProcessorConditional ifbooleanproc =
      new DefaultProcessorConditional(isBoolean());
    context.registerProcessorConditional("BOOLEAN", ifbooleanproc);
    final ProcessorConditional ifglueproc =
      new DefaultProcessorConditional(isGlue());
    context.registerProcessorConditional("GLUE", ifglueproc);
    final ProcessorConditional ifenumproc =
      new DefaultProcessorConditional(isEnum());
    context.registerProcessorConditional("ENUM", ifenumproc);
  }

}
