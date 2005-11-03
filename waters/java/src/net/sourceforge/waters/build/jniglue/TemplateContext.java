//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateContext
//###########################################################################
//# $Id: TemplateContext.java,v 1.2 2005-11-03 01:24:16 robi Exp $
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