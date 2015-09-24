//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.PropositionIcon.ColorInfo;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * An implementation of the {@link RenderingContext} interface based on
 * a module context. This implementation obtains event fonts and proposition
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
  @Override
  public ColorInfo getColorInfo(final GraphProxy graph,
                                final SimpleNodeProxy node)
  {
    return mModuleContext.guessPropositionColors(graph, node);
  }

  @Override
  public Font getFont(final IdentifierProxy ident)
  {
    if (mModuleContext.guessEventKind(ident) == EventKind.UNCONTROLLABLE) {
      return EditorColor.UNCONTROLLABLE_FONT;
    } else {
      return EditorColor.DEFAULT_FONT;
    }
  }

  @Override
  public boolean causesPropositionStatusChange(final ModelChangeEvent event,
                                               final GraphProxy graph)
  {
    return mModuleContext.causesPropositionStatusChange(event, graph);
  }


  //#########################################################################
  //# Data Members
  private final ModuleContext mModuleContext;

}
