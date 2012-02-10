//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   EditModuleNameCommand
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * <P>A command to change a module's name.</P>
 *
 * @author Robi Malik
 */

public class EditModuleNameCommand
    implements Command
{

  //#########################################################################
  //# Constructor
  public EditModuleNameCommand(final ModuleContainer container,
                               final String newName)
  {
    final ModuleSubject module = container.getModule();
    mModuleContainer = container;
    mOldName = module.getName();
    mNewName = newName;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    final ModuleSubject module = mModuleContainer.getModule();
    module.setName(mNewName);
    final ModuleWindowInterface iface = mModuleContainer.getEditorPanel();
    iface.showComment();
  }

  public void undo()
  {
    final ModuleSubject module = mModuleContainer.getModule();
    module.setName(mOldName);
    final ModuleWindowInterface iface = mModuleContainer.getEditorPanel();
    iface.showComment();
  }

  public void setUpdatesSelection(final boolean update)
  {
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return "Module Name Change";
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private final String mOldName;
  private final String mNewName;

}
