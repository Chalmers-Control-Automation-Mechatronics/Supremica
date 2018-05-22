//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

import java.util.HashMap;
import java.util.Map;


class TemplateContext {

  //#########################################################################
  //# Constructors
  TemplateContext()
  {
    this(null);
  }

  TemplateContext(final TemplateContext parent)
  {
    mParent = parent;
    mProcessors = new HashMap<TemplateProcessorKey,Object>();
  }


  //#########################################################################
  //# Simple Access
  TemplateContext getParent()
  {
    return mParent;
  }


  //#########################################################################
  //# Retrieving Processors
  ProcessorVariable getProcessorVariable(final String name)
  {
    final TemplateProcessorKey key =
      new TemplateProcessorKey(TemplateProcessorKey.K_VARIABLE, name);
    return (ProcessorVariable) getProcessor(key);
  }

  ProcessorForeach getProcessorForeach(final String name)
  {
    final TemplateProcessorKey key =
      new TemplateProcessorKey(TemplateProcessorKey.K_FOREACH, name);
    return (ProcessorForeach) getProcessor(key);
  }

  ProcessorConditional getProcessorConditional(final String name)
  {
    final TemplateProcessorKey key =
      new TemplateProcessorKey(TemplateProcessorKey.K_CONDITIONAL, name);
    return (ProcessorConditional) getProcessor(key);
  }

  private Object getProcessor(final TemplateProcessorKey key)
  {
    final Object processor = mProcessors.get(key);
    if (processor != null) {
      return processor;
    } else if (mParent != null) {
      return mParent.getProcessor(key);
    } else {
      return null;
    }
  }


  //#########################################################################
  //# Registering Processors
  void registerProcessorVariable(final String name,
                                 final ProcessorVariable processor)
  {
    final TemplateProcessorKey key =
      new TemplateProcessorKey(TemplateProcessorKey.K_VARIABLE, name);
    mProcessors.put(key, processor);
  }

  void registerProcessorForeach(final String name,
                                final ProcessorForeach processor)
  {
    final TemplateProcessorKey key =
      new TemplateProcessorKey(TemplateProcessorKey.K_FOREACH, name);
    mProcessors.put(key, processor);
  }

  void registerProcessorConditional(final String name,
                                    final ProcessorConditional processor)
  {
    final TemplateProcessorKey key =
      new TemplateProcessorKey(TemplateProcessorKey.K_CONDITIONAL, name);
    mProcessors.put(key, processor);
  }


  //#########################################################################
  //# Data Members
  private final TemplateContext mParent;
  private final Map<TemplateProcessorKey,Object> mProcessors;

}
