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

import java.util.Set;


abstract class TypeGlue implements Comparable<TypeGlue>, WritableGlue {

  //#########################################################################
  //# Simple Access
  abstract String getCppTypeName();

  abstract String getJNITypeName();

  abstract String getGlueTypeName();

  abstract void appendTypeSignature(StringBuilder buffer);

  abstract String getJNICallPart();

  abstract Class<?> getJavaClass();


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
