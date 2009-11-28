//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.renderer
//# CLASS:   RenderingContext
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.renderer;

import java.awt.Font;

import net.sourceforge.waters.gui.PropositionIcon;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;


public interface RenderingContext
{
  /**
   * Gets a font for the display of the given identifier.
   * This method is used to provide different fonts for event labels that are
   * known to be controllable or uncontrollable.
   * This method is expected to return the same font each time it is called
   * for the same input; to change the font for an identifier, the identifier
   * must be removed from shape producer's cache.
   */
  public Font getFont(IdentifierProxy ident);

  /**
   * Gets colour information for the display of the given simple node.
   * This method is used to obtain proposition colours for node with marking
   * propositions.
   * This method is expected to return the same colour information each time
   * it is called for the same input; to change the colours for a node, the
   * node must be removed from shape producer's cache.
   */
  public PropositionIcon.ColorInfo getColorInfo(SimpleNodeProxy node);

  /**
   * Gets rendering information for the display of the given item.
   * The rendering information provides the colours and highlighting status
   * for an item to be displayed. This method is <I>not</I> required to
   * return the same result when called multiply with the same input.
   */
  public RenderingInformation getRenderingInformation(Proxy proxy);

}
