//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ForeachComponentEditorDialog
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.subject.module.ForeachComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


/**
 * A dialog to enter and edit foreach blocks for the components tree.
 * This dialog is an instance of the generic {@link ForeachEditorDialog}
 * to support the editing of
 * {@link net.sourceforge.waters.model.module.ForeachComponehtProxy
 * ForeachComponentProxy} objects, which appear a module's components list.
 *
 * @author Robi Malik
 */

public class ForeachComponentEditorDialog
  extends ForeachEditorDialog
{

  //#########################################################################
  //# Constructors
  public ForeachComponentEditorDialog(final ModuleWindowInterface root)
  {
    super(root);
  }

  public ForeachComponentEditorDialog(final ModuleWindowInterface root,
                                      final ForeachComponentSubject foreach)
  {
    super(root, foreach);
  }


  //#########################################################################
  //# Data Members
  ForeachComponentSubject getTemplate()
  {
    return TEMPLATE;
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final ForeachComponentSubject TEMPLATE =
    new ForeachComponentSubject("", new SimpleIdentifierSubject(""));

}
