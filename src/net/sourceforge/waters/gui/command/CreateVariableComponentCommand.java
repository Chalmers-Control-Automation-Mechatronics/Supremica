//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   CreateVariableComponentCommand
//###########################################################################
//# $Id: CreateVariableComponentCommand.java,v 1.1 2007-11-21 23:42:26 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui.command;

import java.util.Collection;

import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;


/**
 * <P>A command for creating an variable.</P>
 *
 * @author Robi Malik
 */

public class CreateVariableComponentCommand
  implements Command
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new variable create command.
   * @param  decl      The variable to be added.
   *                   The given object will be added to the module,
   *                   without any cloning.
   * @param  module    The module to which it is to be added.
   */
  public CreateVariableComponentCommand(final VariableComponentSubject decl,
                                        final ModuleSubject module)
  {
    mVariableComponent = decl;
    mModule = module;
  }
        

  //#########################################################################
  //# Simple Access
  /**
   * Gets the variable added by this command.
   */
  public VariableComponentSubject getVariableComponent()
  {
    return mVariableComponent;
  }

  /**
   * Gets the module affected by this command.
   */
  public ModuleSubject getModule()
  {
    return mModule;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    final Collection<AbstractSubject> collection =
      mModule.getComponentListModifiable();
    collection.add(mVariableComponent);
  }

  public void undo()
  {
    final Collection<AbstractSubject> collection =
      mModule.getComponentListModifiable();
    collection.remove(mVariableComponent);
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return mDescription;
  }


  //#########################################################################
  //# Data Members
  private final VariableComponentSubject mVariableComponent;
  private final ModuleSubject mModule;


  //#########################################################################
  //# Class Constants
  private static final String mDescription = "Create Variable";

}
