package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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
import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.gui.ide.ModuleContainer;

public class EventJTree extends JTree implements InternalFrameObserver, SimulationObserver, ComponentListener
{

  public EventJTree(final Simulation sim, final AutomatonDesktopPane desktop, final ModuleContainer container)
  {
    super();
    this.setCellRenderer(new EventTreeCellRenderer());
    mSim = sim;
    mDesktop = desktop;
    mPane = null;
    desktop.attach(this);
    sim.attach(this);
    automatonAreOpen = new ArrayList<String>();
    mContainer = container;
    mSortingMethods = new ArrayList<Pair<Boolean, Integer>>();
    expandedNodes = new ArrayList<String>();
    this.setModel(new EventTreeModel(mSim, mSortingMethods));
    this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setRootVisible(false);
    setShowsRootHandles(true);
    setAutoscrolls(true);
    setToggleClickCount(0);

    this.addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          final TreePath path = EventJTree.this.getClosestPathForLocation((int)e.getPoint().getX(), (int)e.getPoint().getY());
          final Object node = path.getLastPathComponent();
          if (node == null)
            return; // Nothing is selected
          if (EventProxy.class.isInstance(node))
          {
            try {
              if (eventCanBeFired(mSim, (EventProxy)node))
                fireEvent((EventProxy)node);
              else
                System.out.println("ERROR: That event is blocked");
            } catch (final NonDeterministicException exception) {
              System.out.println("ERROR: Uncontrollable Event detected: " + exception.getMessage() + ". No event has been fired.");
            } catch (final IllegalArgumentException exception) {
              System.out.println(exception.getMessage() + ". No event has been fired");
            }
          }
          else if (AutomatonProxy.class.isInstance(node))
          {
            final AutomatonProxy toAdd = (AutomatonProxy)node;
            mDesktop.addAutomaton(toAdd.getName(), mSim.getContainer(), mSim, 2);
          }
        }
      }

      private boolean eventCanBeFired(final Simulation sim, final EventProxy event)
      {
        for (final Step possibleStep : sim.getValidTransitions())
        {
          if (possibleStep.getEvent() == event)
            return true;
        }
        return false;
      }

      private void fireEvent(final EventProxy node) throws NonDeterministicException
      {

        final ArrayList<JLabel> labels = new ArrayList<JLabel>();
        final ArrayList<Step> steps = new ArrayList<Step>();
        for (final Step step: mSim.getValidTransitions())
        {
          if (step.getEvent() == node)
          {
            final JLabel toAdd = new JLabel(step.toString());
            if (node.getKind() == EventKind.CONTROLLABLE)
              toAdd.setIcon(IconLoader.ICON_CONTROLLABLE);
            else if (node.getKind() == EventKind.UNCONTROLLABLE)
              toAdd.setIcon(IconLoader.ICON_UNCONTROLLABLE);
            else
              toAdd.setIcon(IconLoader.ICON_PROPOSITION);
            labels.add(toAdd);
            steps.add(step);
          }
        }
        if (labels.size() == 0)
          mContainer.getIDE().error(": That event cannot be fired");
        else if (labels.size() == 1)
          mSim.step(steps.get(0));
        else
        {
          final JLabel[] arrayLabels = new JLabel[labels.size()];
          final Step[] arraySteps = new Step[steps.size()];
          for (int looper = 0; looper < labels.size(); looper++)
          {
            arrayLabels[looper] = labels.get(looper);
            arraySteps[looper] = steps.get(looper);
          }
          final EventChooserDialog dialog = new EventChooserDialog(mContainer.getIDE(), arrayLabels, arraySteps);
          dialog.setVisible(true);
          if (!dialog.wasCancelled())
          {
            mSim.step(dialog.getSelectedStep());
          }
        }
      }
    });
    this.addTreeWillExpandListener(new TreeWillExpandListener(){

      public void treeWillCollapse(final TreeExpansionEvent event)
          throws ExpandVetoException
      {
        if (EventProxy.class.isInstance(event.getPath().getLastPathComponent()))
        {
          expandedNodes.remove(((EventProxy)event.getPath().getLastPathComponent()).getName());
        }
      }

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

  public void addPane(final JScrollPane pane)
  {
    mPane = pane;
    mPane.addComponentListener(this);
  }

  // ##################################################################
  // # Simple Access

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
    this.setModel(new EventTreeModel(mSim, mSortingMethods));
    for (int looper = 0; looper < expandedNodes.size(); looper++)
    {
      final String name = expandedNodes.get(looper);
      for (int nodeIndex = 0; nodeIndex < mSim.getAllEvents().size(); nodeIndex++)
      {
        if (mSim.getAllEvents().get(nodeIndex).getName().compareTo(name) == 0)
        {
          this.expandPath(new TreePath(new Object[]{mSim, mSim.getAllEvents().get(nodeIndex)}));
        }
      }
    }
  }

  public void simulationChanged(final SimulationChangeEvent event)
  {
    if (event.getKind() == SimulationChangeEvent.MODEL_CHANGED)
      forceRecalculation();
  }

  // #################################################################
  // # Interface ComponentListener

  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentMoved(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentResized(final ComponentEvent e)
  {
    forceRecalculation();
  }

  public void componentShown(final ComponentEvent e)
  {
    forceRecalculation();
  }

  //##################################################################
  // # Interface InternalFrameObserver

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

  // ########################################################################
  // # Inner Classes

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
      mAutomataPanel = new JPanel();
      mAutomataPanel.setLayout(layout);
      mAutomataNameLabel = new JLabel();
      mAutomataIconLabel = new JLabel();
      mAutomataStatusLabel = new JLabel();
      mAutomataPanel.add(mAutomataNameLabel);
      mAutomataPanel.add(mAutomataIconLabel);
      mAutomataPanel.add(mAutomataStatusLabel);
    }

    //#######################################################################
    //# Interface javax.swing.tree.TreeCellRenderer
    public Component getTreeCellRendererComponent
      (final JTree tree, final Object value, final boolean sel,
       final boolean expanded, final boolean leaf,
       final int row, final boolean hasFocus)
    {
       if (EventProxy.class.isInstance(value))
       {
         if (sel)
           mEventPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
         else
           mEventPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        final EventProxy event = (EventProxy)value;
        if (event.getKind() == EventKind.CONTROLLABLE)
          mEventNameLabel.setIcon(IconLoader.ICON_CONTROLLABLE);
        else
          mEventNameLabel.setIcon(IconLoader.ICON_UNCONTROLLABLE);
        mEventNameLabel.setText(event.getName());
        mEventStatusLabel.setIcon(mSim.getEventActivityIcon(event));
        final int width = mPane.getViewport().getWidth();
        final int rightWidth = eventColumnWidth[1];
        final int leftWidth = width - rightWidth - noduleWidth;
        mEventPanel.setPreferredSize(new Dimension(width, rowHeight));
        mEventNameLabel.setPreferredSize(new Dimension(leftWidth, rowHeight));
        mEventStatusLabel.setPreferredSize(new Dimension(rightWidth, rowHeight));
        return mEventPanel;
      }
      else if (AutomatonProxy.class.isInstance(value))
      {
        if (sel)
          mAutomataPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
        else
          mAutomataPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
        final AutomatonProxy autoProxy = (AutomatonProxy)value;
        mAutomataNameLabel.setText(autoProxy.getName());
        if (mContainer.getSourceInfoMap().get(autoProxy).getSourceObject().getClass() == VariableComponentSubject.class)
          mAutomataNameLabel.setIcon(IconLoader.ICON_VARIABLE);
        else
          mAutomataNameLabel.setIcon(ModuleContext.getComponentKindIcon(autoProxy.getKind()));
        final EventProxy parentEvent = getParentEvent(row);
        if (mSim.getNonControllable(parentEvent) != null)
        {
          if (mSim.getNonControllable(parentEvent).contains(autoProxy))
          {
            mAutomataIconLabel.setIcon(IconLoader.ICON_WARNING);
          }
          else
          {
            if (mSim.getBlocking(parentEvent).contains(autoProxy))
              mAutomataIconLabel.setIcon(IconLoader.ICON_CROSS);
            else
              mAutomataIconLabel.setIcon(IconLoader.ICON_TICK);
          }
        }
        else
        {
          if (mSim.getBlocking(parentEvent).contains(autoProxy))
            mAutomataIconLabel.setIcon(IconLoader.ICON_CROSS);
          else
            mAutomataIconLabel.setIcon(IconLoader.ICON_TICK);
        }
        StateProxy currentState;
        currentState = mSim.getCurrentStates().get(autoProxy);
        mAutomataStatusLabel.setText(currentState.getName());
        mAutomataStatusLabel.setIcon(mSim.getMarkingIcon(currentState, autoProxy));
        final int width = mPane.getWidth();
        final int rightWidth = (width * automataColumnWidth[2] - 2 * noduleWidth * automataColumnWidth[2]) / (sum(automataColumnWidth));
        final int centerWidth = (width * automataColumnWidth[1] - 2 * noduleWidth * automataColumnWidth[1]) / (sum(automataColumnWidth));
        final int leftWidth = (width * automataColumnWidth[0] - 2 * noduleWidth * automataColumnWidth[0]) / (sum(automataColumnWidth));
        mAutomataNameLabel.setPreferredSize(new Dimension(leftWidth, rowHeight));
        mAutomataIconLabel.setPreferredSize(new Dimension(centerWidth, rowHeight));
        mAutomataStatusLabel.setPreferredSize(new Dimension(rightWidth, rowHeight));
        if (automatonAreOpen.contains(autoProxy.getName()))
        {
          mAutomataNameLabel.setFont(mAutomataNameLabel.getFont().deriveFont(Font.BOLD));
          mAutomataStatusLabel.setFont(mAutomataStatusLabel.getFont().deriveFont(Font.BOLD));
        }
        else
        {
          mAutomataNameLabel.setFont(mAutomataNameLabel.getFont().deriveFont(Font.PLAIN));
          mAutomataStatusLabel.setFont(mAutomataStatusLabel.getFont().deriveFont(Font.PLAIN));
        }
        return mAutomataPanel;
      }
      else
      {
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

    private EventProxy getParentEvent(final int row)
    {
      if (EventJTree.this.getPathForRow(row) == null)
        return null; // The row is not visible, so it doesn't matter what event has it's parent
      if (EventJTree.this.getPathForRow(row).getPathCount() == 2)
        return (EventProxy)EventJTree.this.getPathForRow(row).getLastPathComponent();
      else
        return getParentEvent(row-1);
    }

    // ###########################################################################
    // # Data Members
    private final JPanel mEventPanel;
    private final JLabel mEventNameLabel;
    private final JLabel mEventStatusLabel;
    private final JPanel mAutomataPanel;
    private final JLabel mAutomataNameLabel;
    private final JLabel mAutomataIconLabel;
    private final JLabel mAutomataStatusLabel;

    // ###########################################################################
    // # Class Constants
    private static final long serialVersionUID = 6788022446662090661L;
  }

  private final AutomatonDesktopPane mDesktop;
  private final Simulation mSim;
  private final ModuleContainer mContainer;
  private final ArrayList<String> automatonAreOpen;
  private final ArrayList<Pair<Boolean, Integer>> mSortingMethods;
  private final ArrayList<String> expandedNodes;
  private JScrollPane mPane;

  private static final long serialVersionUID = -4373175227919642063L;
  private static final int[] automataColumnWidth = {110, 20, 60};
  private static final int[] eventColumnWidth = {200, 20};
  private static final int noduleWidth = 30;
  public static final int rowHeight = 20;

}
