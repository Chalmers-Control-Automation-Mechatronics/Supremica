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


  //#########################################################################
  //# Constructors
  public ParameterJScrollPane(final List<Parameter> p, final ProductDESProxy model)
  {
    generateViewPort(p, model);
  }

  public void replaceView(final List<Parameter> newParams, final ProductDESProxy model)
  {
    generateViewPort(newParams, model);
  }

  private void generateViewPort(final List<Parameter> parameters, final ProductDESProxy model) {

    final JPanel newView = new JPanel();
    final List<ParameterPanel> newParametersPanels = new ArrayList<ParameterPanel>();
    newView.setLayout(new BoxLayout(newView, BoxLayout.Y_AXIS));
    //Generate new parameterPanels
    for(final Parameter param: parameters) {
      newParametersPanels.add(new ParameterPanel(param, model));
    }
    //Store parameterPanels in Panel
    for (final ParameterPanel panel : newParametersPanels) {
      newView.add(panel);
    }

    setViewportView(newView);
  }

  public void commit() {
    for(final Component c: ((JPanel) getViewport().getComponent(0)).getComponents())
      ((ParameterPanel) c).commitParameter();
  }

  public List<Parameter> getParameters() {

    final List<Parameter> activeParameters = new ArrayList<>();

    for(final Component c: ((JPanel) getViewport().getComponent(0)).getComponents())
      activeParameters.add(((ParameterPanel) c).getParameter());

    return activeParameters;
  }

  private static final long serialVersionUID = 1L;
}
