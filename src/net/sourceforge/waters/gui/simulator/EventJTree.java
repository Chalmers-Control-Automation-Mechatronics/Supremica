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

package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.util.IconLoader;
import net.sourceforge.waters.model.base.Pair;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;

public class EventJTree
  extends JTree
  implements InternalFrameObserver, SimulationObserver, ComponentListener
{

  //#########################################################################
  //# Constructor
  public EventJTree(final Simulation sim,
                    final AutomatonDesktopPane desktop)
  {
    setCellRenderer(new EventTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    sim.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mSortingMethods = new ArrayList<Pair<Boolean, Integer>>();
    expandedNodes = new ArrayList<String>();
    setModel(new EventTreeModel(mSim, mSortingMethods));
    getSelectionModel().setSelectionMode
      (TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    final int height =
      Math.max(MIN_ROW_HEIGHT, IconLoader.getWatersIconHeight());
    setRowHeight(height);
    setShowsRootHandles(true);
    setAutoscrolls(true);
    setToggleClickCount(0);
    mPopupFactory = new SimulatorPopupFactory(sim);
    addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2) {
          final TreePath path =
            getClosestPathForLocation((int) e.getPoint().getX(),
                                      (int) e.getPoint().getY());
          final Object node = path.getLastPathComponent();
          if (node == null)
            return; // Nothing is selected
          if (node instanceof EventProxy) {
            final EventProxy event = (EventProxy) node;
            mSim.step(event);
          } else if (node instanceof AutomatonProxy) {
            final AutomatonProxy aut = (AutomatonProxy) node;
            final ModuleContainer container = mSim.getModuleContainer();
            mDesktop.addAutomaton(aut.getName(), container, mSim, 2);
          }
        }
      }

      @Override
      public void mousePressed(final MouseEvent event)
      {
        final TreePath path = getPathForLocation(event.getX(), event.getY());
        if (path != null) {
          final Proxy proxy = (Proxy) path.getLastPathComponent();
          mPopupFactory.maybeShowPopup(EventJTree.this, event, proxy);
        }
      }

      @Override
      public void mouseEntered(final MouseEvent e)
      {
        // Do nothing
      }

      @Override
      public void mouseExited(final MouseEvent e)
      {
        // Do nothing
      }

      @Override
      public void mouseReleased(final MouseEvent event)
      {
        final TreePath path = getPathForLocation(event.getX(), event.getY());
        if (path != null) {
          final Proxy proxy = (Proxy) path.getLastPathComponent();
          mPopupFactory.maybeShowPopup(EventJTree.this, event, proxy);
        }
      }
    });
    this.addMouseMotionListener(new MouseMotionListener(){
      @Override
      public void mouseMoved(final MouseEvent e)
      {
        final TreePath path = EventJTree.this.getClosestPathForLocation(e.getX(), e.getY());
        final Object comp = path.getLastPathComponent();
        final Proxy proxy = (Proxy) comp;
        final ToolTipVisitor visitor = mSim.getToolTipVisitor();
        final String tooltip = visitor.getToolTip(proxy, true);
        setToolTipText(tooltip);
      }

      @Override
      public void mouseDragged(final MouseEvent e)
      {
        // Do nothing
      }
    });
    this.addTreeWillExpandListener(new TreeWillExpandListener(){

      @Override
      public void treeWillCollapse(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (EventProxy.class.isInstance(event.getPath().getLastPathComponent()))
        {
          expandedNodes.remove(((EventProxy)event.getPath().getLastPathComponent()).getName());
        }
      }

      @Override
      public void treeWillExpand(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (EventProxy.class.isInstance(event.getPath().getLastPathComponent()))
        {
          for (final String name : expandedNodes)
          {
            if (((EventProxy)event.getPath().getLastPathComponent()).getName().compareTo(name) == 0)
              return;
          }
          expandedNodes.add(((EventProxy)event.getPath().getLastPathComponent()).getName());
        }
      }
    });
  }

  //########################################################################
  //# Access
  public void addPane(final JScrollPane pane)
  {
    mPane = pane;
    mPane.addComponentListener(this);
  }

  public void sortBy(final int index)
  {
    int oldIndex = -1;
    boolean oldAscending = false;
    for (int looper = 0; looper < mSortingMethods.size(); looper++)
    {
      if (mSortingMethods.get(looper).getSecond() == index)
      {
        oldIndex = looper;
        oldAscending = mSortingMethods.get(looper).getFirst();
      }
    }
    if (oldIndex != -1)
    {
      if (oldIndex != 0)
      {
        final Pair<Boolean, Integer> oldMethod = mSortingMethods.get(oldIndex);
        mSortingMethods.remove(oldIndex);
        mSortingMethods.add(0, oldMethod);
      }
      else
      {
        final Pair<Boolean, Integer> newMethod = new Pair<Boolean, Integer>(!oldAscending, index);
        mSortingMethods.remove(oldIndex);
        mSortingMethods.add(0, newMethod);
      }
    }
    else
    {
      final Pair<Boolean, Integer> newMethod = new Pair<Boolean, Integer>(true, index);
      mSortingMethods.add(0, newMethod);
    }
    forceRecalculation();
  }

  // ##################################################################
  // # Interface SimulationObserver

  public void forceRecalculation()
  {
    //final long time = System.currentTimeMillis();
    final EventTreeModel model = new EventTreeModel(mSim, mSortingMethods);
    this.setModel(model);
    for (int looper = 0; looper < expandedNodes.size(); looper++)
    {
      final String name = expandedNodes.get(looper);
      for (int nodeIndex = 0; nodeIndex < mSim.getOrderedEvents().size(); nodeIndex++)
      {
        if (mSim.getOrderedEvents().get(nodeIndex).getName().compareTo(name) == 0)
        {
          this.expandPath(new TreePath(new Object[]{mSim, mSim.getOrderedEvents().get(nodeIndex)}));
        }
      }
    }
  }

  @Override
  public void simulationChanged(final SimulationChangeEvent event)
  {
    // Any event can change the order, so ALWAYS redraw the entire graph
    forceRecalculation();
  }

  // #################################################################
  // # Interface ComponentListener

  @Override
  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  @Override
  public void componentMoved(final ComponentEvent e)
  {
    // Do nothing
  }

  @Override
  public void componentResized(final ComponentEvent e)
  {
    forceRecalculation();
  }

  @Override
  public void componentShown(final ComponentEvent e)
  {
    forceRecalculation();
  }

  //##################################################################
  // # Interface InternalFrameObserver

  @Override
  public void onFrameEvent(final InternalFrameEvent event)
  {
    if (event.isOpeningEvent())
    {
      if (!automatonAreOpen.contains(event.getName()))
      {
        automatonAreOpen.add(event.getName());
      }
    }
    else
    {
      if (automatonAreOpen.contains(event.getName()))
      {
        automatonAreOpen.remove(event.getName());
      }
    }
    repaint();
  }


  //#########################################################################
  //# Inner Class EventTreeCellRenderer
  private class EventTreeCellRenderer
    implements TreeCellRenderer
  {

    //#######################################################################
    //# Constructor
    private EventTreeCellRenderer()
    {
      mEventPanel = new JPanel();
      final FlowLayout layout = new FlowLayout(FlowLayout.LEADING, 0, 0);
      mEventPanel.setLayout(layout);
      mEventNameLabel = new JLabel();
      mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.PLAIN));
      mEventStatusLabel = new JLabel();
      mEventPanel.add(mEventNameLabel);
      mEventPanel.add(mEventStatusLabel);
      mAutomatonPanel = new JPanel();
      mAutomatonPanel.setLayout(layout);
      mAutomatonNameLabel = new JLabel();
      mAutomatonIconLabel = new JLabel();
      mAutomatonStatusLabel = new JLabel();
      mAutomatonPanel.add(mAutomatonNameLabel);
      mAutomatonPanel.add(mAutomatonIconLabel);
      mAutomatonPanel.add(mAutomatonStatusLabel);
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    @Override
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
       if (value instanceof EventProxy) {
         if (sel)
           mEventPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
         else
           mEventPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        final EventProxy event = (EventProxy) value;
        final EventKind kind = event.getKind();
        final boolean observable = event.isObservable();
        final Icon icon = ModuleContext.getEventKindIcon(kind, observable);
        mEventNameLabel.setIcon(icon);
        mEventNameLabel.setText(event.getName());
        final Icon eventActivityIcon = mSim.getEventActivityIcon(event);
        mEventStatusLabel.setIcon(eventActivityIcon);
        final int height = getRowHeight();
        final int width = mPane.getViewport().getWidth();
        final int rightWidth = Math.max(eventColumnWidth[1], height);
        final int leftWidth = width - rightWidth - noduleWidth;
        mEventPanel.setPreferredSize(new Dimension(width, height));
        mEventNameLabel.setPreferredSize(new Dimension(leftWidth, height));
        mEventStatusLabel.setPreferredSize(new Dimension(rightWidth, height));
        return mEventPanel;
      } else if (value instanceof AutomatonProxy) {
        if (sel) {
          mAutomatonPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
        } else {
          mAutomatonPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        }
        final AutomatonProxy aut = (AutomatonProxy) value;
        mAutomatonNameLabel.setText(aut.getName());
        final Icon autIcon = AutomatonLeafNode.getAutomatonIcon(mSim, aut);
        mAutomatonNameLabel.setIcon(autIcon);
        if (mSim.getAutomatonStatus(aut) == AutomatonStatus.DISABLED) {
          mAutomatonIconLabel.setIcon(null);
          mAutomatonStatusLabel.setText("");
          mAutomatonStatusLabel.setIcon(null);
        } else {
          final EventProxy event = getParentEvent(row);
          final EventStatus status = mSim.getEventStatus(event, aut);
          final Icon statusIcon = status.getIcon();
          mAutomatonIconLabel.setIcon(statusIcon);
          final StateProxy currentState = mSim.getCurrentState(aut);
          mAutomatonStatusLabel.setText(currentState.getName());
          mAutomatonStatusLabel.setIcon
            (mSim.getMarkingIcon(currentState, aut));
        }
        final int height = getRowHeight();
        final int width = mPane.getWidth();
        final int rightWidth = (width * automataColumnWidth[2] - 2 * noduleWidth * automataColumnWidth[2]) / (sum(automataColumnWidth));
        final int centerWidth = (width * automataColumnWidth[1] - 2 * noduleWidth * automataColumnWidth[1]) / (sum(automataColumnWidth));
        final int leftWidth = (width * automataColumnWidth[0] - 2 * noduleWidth * automataColumnWidth[0]) / (sum(automataColumnWidth));
        mAutomatonNameLabel.setPreferredSize(new Dimension(leftWidth, height));
        mAutomatonIconLabel.setPreferredSize(new Dimension(centerWidth, height));
        mAutomatonStatusLabel.setPreferredSize(new Dimension(rightWidth, height));
        if (automatonAreOpen.contains(aut.getName())) {
          mAutomatonNameLabel.setFont(mAutomatonNameLabel.getFont().deriveFont(Font.BOLD));
          mAutomatonStatusLabel.setFont(mAutomatonStatusLabel.getFont().deriveFont(Font.BOLD));
        } else {
          mAutomatonNameLabel.setFont(mAutomatonNameLabel.getFont().deriveFont(Font.PLAIN));
          mAutomatonStatusLabel.setFont(mAutomatonStatusLabel.getFont().deriveFont(Font.PLAIN));
        }
        return mAutomatonPanel;
      } else {
        return new JPanel();
      }
    }

    private int sum(final int[] a)
    {
      int o = 0;
      for (int i = 0; i < a.length; i++) {
        o += a[i];
      }
      return o;
    }

    private EventProxy getParentEvent(int row)
    {
      if (row >= getRowCount()) {
        row = getRowCount() - 1;
      }
      final TreePath path = getPathForRow(row);
      return (EventProxy) path.getPathComponent(1);
    }

    //#######################################################################
    //# Data Members
    private final JPanel mEventPanel;
    private final JLabel mEventNameLabel;
    private final JLabel mEventStatusLabel;
    private final JPanel mAutomatonPanel;
    private final JLabel mAutomatonNameLabel;
    private final JLabel mAutomatonIconLabel;
    private final JLabel mAutomatonStatusLabel;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ArrayList<String> automatonAreOpen;
  private final ArrayList<Pair<Boolean, Integer>> mSortingMethods;
  private final ArrayList<String> expandedNodes;
  private JScrollPane mPane;
  private final SimulatorPopupFactory mPopupFactory;


  //#########################################################################
  //# Class Constants
  private static final int MIN_ROW_HEIGHT = 20;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {200, 20};
  private static final int noduleWidth = 30;

}
