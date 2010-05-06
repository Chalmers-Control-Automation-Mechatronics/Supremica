//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.command
//# CLASS:   EditModuleCommentCommand
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.command;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * <P>A command to change a module's comment.</P>
 *
 * @author Robi Malik
 */

public class EditModuleCommentCommand
    implements Command
{

  //#########################################################################
  //# Constructor
  public EditModuleCommentCommand(final ModuleContainer container,
                                  final String newComment)
  {
    final ModuleSubject module = container.getModule();
    mModuleContainer = container;
    mOldComment = module.getComment();
    mNewComment = newComment.equals("") ? null : newComment;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.command.Command
  public void execute()
  {
    final ModuleSubject module = mModuleContainer.getModule();
    module.setComment(mNewComment);
    final ModuleWindowInterface iface = mModuleContainer.getEditorPanel();
    iface.showComment();
  }

  public void undo()
  {
    final ModuleSubject module = mModuleContainer.getModule();
    module.setComment(mOldComment);
    final ModuleWindowInterface iface = mModuleContainer.getEditorPanel();
    iface.showComment();
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getName()
  {
    return "Module Comment Editing";
  }


  //#########################################################################
  //# Data Members
  private final ModuleContainer mModuleContainer;
  private final String mOldComment;
  private final String mNewComment;

}
