//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentSequence
//###########################################################################
//# $Id: TemplateFragmentSequence.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.LinkedList;
import java.util.List;


class TemplateFragmentSequence extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentSequence()
  {
    super(-1);
    mBody = new LinkedList<TemplateFragment>();
  }


  //#########################################################################
  //# Simple Access
  void addFragment(final TemplateFragment fragment)
  {
    mBody.add(fragment);
  }


  //#########################################################################
  //# Code Generation
  void writeCppGlue(final CppGlueWriter writer, final TemplateContext context)
  {
    for (final TemplateFragment fragment : mBody) {
      fragment.writeCppGlue(writer, context);
    }
  }


  //#########################################################################
  //# Data Members
  private final List<TemplateFragment> mBody;

}