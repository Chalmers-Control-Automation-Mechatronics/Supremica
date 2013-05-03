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

import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


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
   * Gets rendering information for the display of the given item.
   * The rendering information provides the colours and highlighting status
   * for an item to be displayed. This method is <I>not</I> required to
   * return the same result when called multiply with the same input.
   */
  public RenderingInformation getRenderingInformation(Proxy proxy);

  /**
   * Gets colour information for the display of the given simple node in
   * the given graph.
   * This method is used to obtain proposition colours for node with marking
   * propositions.
   * This method is expected to return the same colour information each time
   * it is called for the same input; to change the colours for a node, the
   * node must be removed from shape producer's cache.
   */
  public PropositionIcon.ColorInfo getColorInfo(GraphProxy graph,
                                                SimpleNodeProxy node);

  /**
   * Returns whether given event may cause the proposition status of
   * the given graph to change. The proposition status of a graph
   * indicates whether the graph uses any propositions, i.e., whether
   * states without propositions are rendered with a filled or a
   * transparent background.
   */
  public boolean causesPropositionStatusChange(ModelChangeEvent event,
                                               GraphProxy graph);

}
