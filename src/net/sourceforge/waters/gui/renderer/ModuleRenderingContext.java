//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   ModuleRenderingContext
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.PropositionIcon.ColorInfo;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An implementation of the {@link RenderingContext} interface based on
 * a module context. This implementation obtain event fonts and proposition
 * colours using the best guess of a module context, while suppressing all
 * highlighting.
 *
 * @author Robi Malik
 */

public class ModuleRenderingContext
  extends DefaultRenderingContext
{

  //#########################################################################
  //# Constructors
  public ModuleRenderingContext(final ModuleContext context)
  {
    mModuleContext = context;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.renderer.RenderingContext
  public ColorInfo getColorInfo(final SimpleNodeProxy node)
  {
    return mModuleContext.guessPropositionColors(node);
  }

  public Font getFont(final IdentifierProxy ident)
  {
    if (mModuleContext.guessEventKind(ident) == EventKind.UNCONTROLLABLE) {
      return EditorColor.UNCONTROLLABLE_FONT;
    } else {
      return EditorColor.DEFAULT_FONT;
    }
  }


  //#########################################################################
  //# Data Members
  private final ModuleContext mModuleContext;

}
