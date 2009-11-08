//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.module
//# CLASS:   ModuleProxy
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.List;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>
 * A sequence of Waters modules.
 * </P>
 * 
 * <P>
 * Presently, this auxiliary document class is only used by some import tools
 * that need to parse a file containing more than one module. Although a module
 * sequence can in principle be saved in a <CODE>.wmodseq</CODE> file, this is
 * unlikely to ever happen. Instead, the import tools will save each module in
 * the sequence in its own <CODE>.wmod</CODE> file and return only the root
 * module.
 * </P>
 * 
 * @see ModuleProxy
 * @author Robi Malik
 */

public interface ModuleSequenceProxy
  extends DocumentProxy
{
  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the constant of this module sequence.
   * @return The list modules constituting the module sequence.
   */
  public List<ModuleProxy> getModules();
}
