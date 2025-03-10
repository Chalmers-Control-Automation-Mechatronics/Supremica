//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import gnu.trove.set.hash.TIntHashSet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;


public class TraceJTree
  extends JTree
  implements InternalFrameObserver, ComponentListener
{
  TraceJTree(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    setCellRenderer(new TraceTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mExpandedIndexes = new TIntHashSet();
    final TraceMutableTreeNode root = new TraceMutableTreeNode(sim, this);
    this.setModel(new DefaultTreeModel(root, false));
    this.getSelectionModel().setSelectionMode
      (TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    setRowHeight(IconAndFontLoader.getWatersIconSize());
    setShowsRootHandles(true);
    setAutoscrolls(true);
    setToggleClickCount(0);
    mPopupFactory = new SimulatorPopupFactory(sim);
    this.addMouseListener(new MouseAdapter(){
      @Override
      public void mouseClicked(final MouseEvent event)
      {
        if (event.getButton() == MouseEvent.BUTTON1 &&
            event.getClickCount() == 2) {
          final TreePath path =
            getClosestPathForLocation(event.getX(), event.getY());
          final Object node = path.getLastPathComponent();
          if (node == null) {
            return; // Nothing is selected
          } else if (node instanceof TraceStepTreeNode) {
            final TraceStepTreeNode eventNode = (TraceStepTreeNode) node;
            final int targetTime = eventNode.getTime();
            mSim.setState(targetTime);
          } else if (node instanceof AutomatonLeafNode) {
            final AutomatonProxy aut = ((AutomatonLeafNode)node).getAutomaton();
            mDesktop.addAutomaton(aut, mSim.getModuleContainer(), mSim, 2);
          }
        }
      }

      @Override
      public void mousePressed(final MouseEvent event)
      {
        maybeShowPopup(event);
      }

      @Override
      public void mouseReleased(final MouseEvent event)
      {
        maybeShowPopup(event);
      }
    });
    this.addTreeWillExpandListener(new TreeWillExpandListener(){

      @Override
      public void treeWillCollapse(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (event.getPath().getLastPathComponent() instanceof TraceStepTreeNode)
        {
          mExpandedIndexes.remove((((TraceStepTreeNode)event.getPath().getLastPathComponent()).getTime()));
        }
      }

      @Override
      public void treeWillExpand(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        final Object last = event.getPath().getLastPathComponent();
        if (last instanceof TraceStepTreeNode) {
          final TraceStepTreeNode node = (TraceStepTreeNode) last;
          final Integer time = node.getTime();
          if (mExpandedIndexes.add(time)) {
            node.expand(mSim);
          }
        }
      }
    });
    this.addMouseMotionListener(new MouseMotionListener(){

      @Override
      public void mouseDragged(final MouseEvent e)
      {
        // Do nothing

      }

      @Override
      public void mouseMoved(final MouseEvent e)
      {
        final TreePath path = TraceJTree.this.getClosestPathForLocation(e.getX(), e.getY());
        final Object comp = path.getLastPathComponent();
        if (comp instanceof TraceStepTreeNode) {
          final TraceStepTreeNode node = (TraceStepTreeNode) comp;
          final String tooltip = node.getToolTipText(mSim);
          setToolTipText(tooltip);
        } else if (comp instanceof AutomatonLeafNode) {
          final AutomatonLeafNode node = (AutomatonLeafNode) comp;
          final AutomatonProxy aut = node.getAutomaton();
          final ToolTipVisitor visitor = mSim.getToolTipVisitor();
          final String tooltip = visitor.getToolTip(aut, false);
          setToolTipText(tooltip);
        }
      }
    });
  }

  void addScrollPane(final JScrollPane scroll)
  {
    mPane = scroll;
    mPane.addComponentListener(this);
  }


  //#########################################################################
  //# Simple Access
  public void forceRecalculation()
  {
    final TraceMutableTreeNode node = new TraceMutableTreeNode(mSim, this);
    setModel(new DefaultTreeModel(node, false));
    final int childCount = node.getChildCount();
    final TIntHashSet newExpanded = new TIntHashSet(childCount);
    for (int nodeIndex = 0; nodeIndex < childCount; nodeIndex++) {
      final TraceStepTreeNode child =
        (TraceStepTreeNode) node.getChildAt(nodeIndex);
      final int time = child.getTime();
      if (mExpandedIndexes.contains(time)) {
        child.expand(mSim);
        final TreePath path = new TreePath(child.getPath());
        expandPath(path);
        newExpanded.add(nodeIndex);
      }
    }
    mExpandedIndexes = newExpanded;
  }


  //#########################################################################
  //# Interface InternalFrameObserver
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

  //########################################################################
  // # Interface ComponentListener
  @Override
  public void componentHidden(final ComponentEvent e)
  {
    //Do nothing
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


  //#########################################################################
  //# Auxiliary Methods
  private void maybeShowPopup(final MouseEvent event)
  {
    final TreePath path = getClosestPathForLocation(event.getX(), event.getY());
    final MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
    if (node == null) {
      return; // Nothing is selected
    } else if (node instanceof TraceStepTreeNode) {
      final TraceStepTreeNode step = (TraceStepTreeNode) node;
      mPopupFactory.maybeShowPopup(this, event, null, step.getTime());
    } else if (node instanceof AutomatonLeafNode) {
      final AutomatonLeafNode leaf = (AutomatonLeafNode) node;
      mPopupFactory.maybeShowPopup(this, event, leaf.getAutomaton());
    }
  }


  //#########################################################################
  //# Inner Classes
  private class TraceTreeCellRenderer
    implements TreeCellRenderer
  {
    //#######################################################################
    //# Constructor
    private TraceTreeCellRenderer()
    {
      mEventPanel = new JPanel();
      final FlowLayout layout = new FlowLayout(FlowLayout.LEADING, 0, 0);
      mEventPanel.setLayout(layout);
      mEventNameLabel = new JLabel();
      mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.PLAIN));
      mEventPanel.add(mEventNameLabel);
      mAutomatonPanel = new JPanel();
      mAutomatonPanel.setLayout(layout);
      mAutomataNameLabel = new JLabel();
      mAutomataIconLabel = new JLabel();
      mAutomataStatusLabel = new JLabel();
      mAutomatonPanel.add(mAutomataNameLabel);
      mAutomatonPanel.add(mAutomataIconLabel);
      mAutomatonPanel.add(mAutomataStatusLabel);
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    @Override
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
      if (value instanceof TraceStepTreeNode) {
        if (sel)
          mEventPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
        else
          mEventPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        final TraceStepTreeNode node = (TraceStepTreeNode) value;
        final int time = node.getTime();
        final StringBuilder buffer = new StringBuilder();
        buffer.append(time);
        buffer.append(". ");
        buffer.append(node.getText());
        final TraceProxy trace = mSim.getTrace();
        if (trace != null && time == trace.getLoopIndex()) {
          buffer.append(" <---");
        }
        mEventNameLabel.setText(buffer.toString());
        final Icon icon = node.getIcon();
        mEventNameLabel.setIcon(icon);
        if (time == mSim.getCurrentTime()) {
          mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.BOLD));
        } else {
          mEventNameLabel.setFont(mEventNameLabel.getFont().deriveFont(Font.PLAIN));
        }
        return mEventPanel;
      } else if (value instanceof AutomatonLeafNode) {
        if (sel) {
          mAutomatonPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
        } else {
          mAutomatonPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        }
        final AutomatonLeafNode node = (AutomatonLeafNode) value;
        final AutomatonProxy aut = node.getAutomaton();
        mAutomataNameLabel.setText(aut.getName());
        final Icon autIcon = node.getAutomatonIcon(mSim);
        mAutomataNameLabel.setIcon(autIcon);
        final int time = node.getTime();
        final SimulatorState tuple = mSim.getHistoryState(time);
        final AutomatonStatus status = tuple.getStatus(aut);
        if (status.compareTo(AutomatonStatus.WARNING) >= 0) {
          final Icon statusIcon = status.getIcon();
          mAutomataIconLabel.setIcon(statusIcon);
        } else {
          mAutomataIconLabel.setIcon(null);
        }
        final StateProxy state = node.getOverloadedState();
        mAutomataStatusLabel.setText(state.getName());
        mAutomataStatusLabel.setIcon(mSim.getMarkingIcon(state, aut));
        final int width = mPane.getWidth();
        final int rightWidth = (width * automataColumnWidth[2] - 2 * noduleWidth * automataColumnWidth[2]) / (sum(automataColumnWidth));
        final int centerWidth = (width * automataColumnWidth[1] - 2 * noduleWidth * automataColumnWidth[1]) / (sum(automataColumnWidth));
        final int leftWidth = (width * automataColumnWidth[0] - 2 * noduleWidth * automataColumnWidth[0]) / (sum(automataColumnWidth));
        mAutomataNameLabel.setPreferredSize(new Dimension(leftWidth, rowHeight));
        mAutomataIconLabel.setPreferredSize(new Dimension(centerWidth, rowHeight));
        mAutomataStatusLabel.setPreferredSize(new Dimension(rightWidth, rowHeight));
        if (automatonAreOpen.contains(aut.getName()))
        {
          mAutomataNameLabel.setFont(mAutomataNameLabel.getFont().deriveFont(Font.BOLD));
          mAutomataStatusLabel.setFont(mAutomataStatusLabel.getFont().deriveFont(Font.BOLD));
        }
        else
        {
          mAutomataNameLabel.setFont(mAutomataNameLabel.getFont().deriveFont(Font.PLAIN));
          mAutomataStatusLabel.setFont(mAutomataStatusLabel.getFont().deriveFont(Font.PLAIN));
        }
        return mAutomatonPanel;
      } else {
        return new JPanel();
      }
    }

    private int sum (final int[] a)
    {
      int o = 0;
      for (int i = 0; i < a.length; i++)
        o+=a[i];
      return o;
    }

    //#######################################################################
    //# Data Members
    private final JPanel mEventPanel;
    private final JLabel mEventNameLabel;
    private final JPanel mAutomatonPanel;
    private final JLabel mAutomataNameLabel;
    private final JLabel mAutomataIconLabel;
    private final JLabel mAutomataStatusLabel;
  }


  //#########################################################################
  //# Data Members
  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ArrayList<String> automatonAreOpen;
  private JScrollPane mPane;
  private TIntHashSet mExpandedIndexes;
  private final SimulatorPopupFactory mPopupFactory;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int noduleWidth = 30;

}
