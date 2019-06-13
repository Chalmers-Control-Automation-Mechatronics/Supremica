package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sourceforge.waters.model.des.ProductDESProxy;

public class ParameterJScrollPane extends JScrollPane
{
  JPanel activePanel = new JPanel();

  //#########################################################################
  //# Constructors
  public ParameterJScrollPane(final List<Parameter> p, final ProductDESProxy model)
  {
    final JPanel tmp = new JPanel();
    final List<ParameterPanel> parameterPanels = new ArrayList<ParameterPanel>();

    tmp.setLayout(new BoxLayout(tmp, BoxLayout.Y_AXIS));

    for(final Parameter param: p)
      parameterPanels.add(new ParameterPanel(param, model));

    for (final ParameterPanel panel : parameterPanels) {
      tmp.add(panel);
    }
    activePanel = tmp;
    setViewportView(tmp);
  }

  public void replaceView(final List<Parameter> p, final ProductDESProxy model)
  {
    //Generate new parameterPanels
    final JPanel current = new JPanel();
    final List<ParameterPanel> newPanels = new ArrayList<ParameterPanel>();
    current.setLayout(new BoxLayout(current, BoxLayout.Y_AXIS));

    for(final Parameter param: p)
      newPanels.add(new ParameterPanel(param, model));

    for (final ParameterPanel panel : newPanels) {
      current.add(panel);
    }

    //Copy values from old panel
    final JPanel old = (JPanel) getViewport().getComponent(0); //same as using active panel

    final List<ParameterPanel> oldPanels = new ArrayList<>();

    for(final Component c: old.getComponents()) {
       oldPanels.add((ParameterPanel) c);
      //((ParameterPanel) c).commitParameter();
    }

    copyValue(oldPanels,newPanels);

    //change to new viewport
    setViewportView(current);
    activePanel = current;
  }


  public void commit() {
    for(final Component c: activePanel.getComponents())
      ((ParameterPanel) c).commitParameter();
  }


  public static void copyValue(final List<ParameterPanel> oldPanels,
                               final List<ParameterPanel> newPanels)
  {

    final List<Integer> oldPanelIDs = new ArrayList<Integer>();
    final List<Integer> newPanelIDs = new ArrayList<Integer>();

    for (final ParameterPanel panel : oldPanels) {
      oldPanelIDs.add(panel.getParameter().getID());
    }

    for (final ParameterPanel panel : newPanels) {
      newPanelIDs.add(panel.getParameter().getID());
    }

    //intersection
    oldPanelIDs.retainAll(newPanelIDs);

    //copy value from old panel to new one
    for (final Integer i : oldPanelIDs) {

      final ParameterPanel o = findID(oldPanels, i);
      final ParameterPanel n = findID(newPanels, i);

    //  o.commitParameter();
      n.copyFromPanel(o);
    }
  }

  public static ParameterPanel findID(final List<ParameterPanel> Panels,
                                      final int id)
  {
    for (final ParameterPanel p : Panels) {
      if (p.getParameter().getID() == id)
        return p;
    }

    return null;
  }

  private static final long serialVersionUID = 1L;

}
