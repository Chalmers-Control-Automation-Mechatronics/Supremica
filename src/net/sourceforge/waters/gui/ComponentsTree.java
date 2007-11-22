//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ComponentsTree
//###########################################################################
//# $Id: ComponentsTree.java,v 1.1 2007-11-22 03:40:12 robi Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;


/**
 * The tree-view panel that shows the components list of a module.
 *
 * @author Robi Malik
 */

class ComponentsTree
  extends JTree
  implements MouseListener		     
{

  //#########################################################################
  //# Constructor
  public ComponentsTree(final ModuleWindowInterface root,
			final ActionListener popuplistener)
  {
    super(new ComponentsTreeModel(root.getModuleSubject()));
    mRoot = root;
    mModuleContext = root.getModuleContext();
    mPrinter = new HTMLPrinter();
    mDoubleClickVisitor = new DoubleClickVisitor();
    mPopupActionListener = popuplistener;
    addMouseListener(this);
    setCellRenderer(new ComponentsTreeCellRenderer());
  }


  //#########################################################################
  //# Interface java.awt.event.MouseListener
  public void mouseClicked(final MouseEvent event)
  {
    if (event.getClickCount() == 2) {
      final Proxy proxy = getClickedItem(event);
      if (proxy != null) {
	mDoubleClickVisitor.doubleClick(proxy);
      }
    }
   }

  public void mouseEntered(final MouseEvent event)
  {
  }

  public void mouseExited(final MouseEvent event)
  {
  }

  public void mousePressed(final MouseEvent event)
  {
    maybeShowPopup(event);
  }

  public void mouseReleased(final MouseEvent event)
  {
    maybeShowPopup(event);
  }


  //#########################################################################
  //# Auxiliary Methods
  private void maybeShowPopup(final MouseEvent event)
  {
    if (event.isPopupTrigger()) {
      final Proxy proxy = getClickedItem(event);
      if (proxy != null) {
	final ModuleTreePopupMenu popup =
	  new ModuleTreePopupMenu(mPopupActionListener, proxy);
	popup.show(this, event.getX(), event.getY());
      }
    }
  }

  private Proxy getClickedItem(final MouseEvent event)
  {
    final TreePath path = getPathForLocation(event.getX(), event.getY());
    if (path == null) {
      return null;
    } else {
      return (Proxy) path.getLastPathComponent();
    }
  }


  //#########################################################################
  //# Inner Class ComponentsTreeCellRenderer
  private class ComponentsTreeCellRenderer
    extends DefaultTreeCellRenderer
  {

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
      super.getTreeCellRendererComponent
	(tree, value, sel, expanded, leaf, row, hasFocus);
      final Proxy proxy = (Proxy) value;
      final String text = mPrinter.toString(proxy);
      setText(text);
      final ImageIcon icon = mModuleContext.getImageIcon(proxy);
      setIcon(icon);
      final String tooltip = mModuleContext.getToolTipText(proxy);
      setToolTipText(tooltip);
      return this;
    }

  }


  //#########################################################################
  //# Inner Class DoubleClickVisitor
  private class DoubleClickVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Invocation
    private void doubleClick(final Proxy proxy)
    {
      try {
	proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
	JOptionPane.showMessageDialog
	  (ComponentsTree.this, exception.getMessage());
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.printer.ModuleProxyVisitor
    public Object visitModuleProxy(final ModuleProxy module)
    {
      mRoot.showComment();
      return null;
    }

    public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
      throws VisitorException
    {
      try {
	final SimpleComponentSubject subject = (SimpleComponentSubject) comp;
	mRoot.showEditor(subject);
	return null;
      } catch (final GeometryAbsentException exception) {
	throw wrap(exception);
      }
    }

    public Object visitVariableComponentProxy(final VariableComponentProxy var)
    {
      final VariableComponentSubject subject = (VariableComponentSubject) var;
      new VariableEditorDialog(mRoot, subject);
      return null;
    }

  }


  //#########################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;
  private final ModuleContext mModuleContext;
  private final ProxyPrinter mPrinter;
  private final DoubleClickVisitor mDoubleClickVisitor;
  private final ActionListener mPopupActionListener;

}