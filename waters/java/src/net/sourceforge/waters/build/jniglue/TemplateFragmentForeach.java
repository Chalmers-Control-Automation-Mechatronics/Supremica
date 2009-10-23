//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentForeach
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.Iterator;


class TemplateFragmentForeach extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentForeach(final String name,
			  final TemplateFragment body,
			  final int numskipped,
			  final int lineno)
  {
    super(numskipped, lineno);
    mName = name;
    mBody = body;
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }

  TemplateFragment getBody()
  {
    return mBody;
  }


  //#########################################################################
  //# Code Generation
  void writeCppGlue(final CppGlueWriter writer, TemplateContext context)
  {
    final TemplateContext outer = getRelevantContext(context);
    final ProcessorForeach processor = outer.getProcessorForeach(mName);
    if (processor != null) {
      final Iterator<?> iter = processor.getIterator();
      final int numerrors = writer.getNumErrors();
      final ProcessorINDEX indexproc = new ProcessorINDEX();
      final ProcessorVariable commaproc = new ProcessorCOMMA(iter, ",");
      final ProcessorVariable commaspproc = new ProcessorCOMMA(iter, ", ");
      final ProcessorConditional hasnextproc = new ProcessorHASNEXT(iter);
      while (iter.hasNext() && numerrors == writer.getNumErrors()) {
	final WritableGlue glue = (WritableGlue) iter.next();
	final TemplateContext subcontext = new TemplateContext(context);
	subcontext.registerProcessorVariable("INDEX", indexproc);
	subcontext.registerProcessorVariable("COMMA", commaproc);
	subcontext.registerProcessorVariable("COMMASP", commaspproc);
	subcontext.registerProcessorConditional("HASNEXT", hasnextproc);
	glue.registerProcessors(subcontext);
	mBody.writeCppGlue(writer, subcontext);
      }
      final int count = indexproc.getIndex();
      final ProcessorVariable countproc = new DefaultProcessorVariable(count);
      context.registerProcessorVariable("COUNT", countproc);
    } else {
      writer.reportError
	("Undefined instruction $FOREACH-" + mName, getLineNo());
    }
  }


  //#########################################################################
  //# Local Class ProcessorINDEX
  private class ProcessorINDEX implements ProcessorVariable {

    //######################################################################
    //# Constructors
    private ProcessorINDEX()
    {
      mIndex = 0;
    }

    //######################################################################
    //# Simple Access
    private int getIndex()
    {
      return mIndex;
    }

    //######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorVariable
    public String getText()
    {
      return Integer.toString(mIndex++);
    }

    //######################################################################
    //# Data Members
    private int mIndex;

  }


  //#########################################################################
  //# Local Class ProcessorCOMMA
  private class ProcessorCOMMA implements ProcessorVariable {

    //######################################################################
    //# Constructors
    private ProcessorCOMMA(final Iterator<?> iter, final String text)
    {
      mIterator = iter;
      mText = text;
    }

    //######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorVariable
    public String getText()
    {
      if (mIterator.hasNext()) {
	return mText;
      } else {
	return "";
      }
    }

    //######################################################################
    //# Data Members
    private final Iterator<?> mIterator;
    private final String mText;

  }


  //#########################################################################
  //# Local Class ProcessorHASNEXT
  private class ProcessorHASNEXT implements ProcessorConditional {

    //######################################################################
    //# Constructors
    private ProcessorHASNEXT(final Iterator<?> iter)
    {
      mIterator = iter;
    }

    //######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorConditional
    public boolean isConditionSatisfied()
    {
      return mIterator.hasNext();
    }

    //######################################################################
    //# Data Members
    private final Iterator<?> mIterator;

  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final TemplateFragment mBody;

}
