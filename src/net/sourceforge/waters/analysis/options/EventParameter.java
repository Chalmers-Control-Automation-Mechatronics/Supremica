package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;

public class EventParameter extends Parameter
{
  public EventParameter(final int id, final String name, final String description)
  {
    super(id, name, description);
    mValue = EventDeclProxy.DEFAULT_MARKING_NAME;
  }

  @Override
  public Component createComponent(final ProductDESProxy model)
  {
    // TODO Change to List<EventProxy>
    final List<String> propositions = new ArrayList<>();
    propositions.add("null");   //option to select null event
    // ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
    // EventProxy noEvent = factory.createEventProxy("(none)", EventKind.PROPOSITION);

    for(final EventProxy event: model.getEvents()) {
      // event.getKind() == EventKind.PROPOSITION
      // TODO Only add propositions
      propositions.add(event.getName());
    }

    Collections.sort(propositions);

    final JComboBox<String> ret = new JComboBox<>(propositions.toArray(new String[propositions.size()]));
    ret.setSelectedItem(mValue);

    //:accepting is selected as default
    // TODO Use the loop above to check
    if(propositions.contains(EventDeclProxy.DEFAULT_MARKING_NAME) && mValue.equals(EventDeclProxy.DEFAULT_MARKING_NAME))
      ret.setSelectedItem(EventDeclProxy.DEFAULT_MARKING_NAME);

    return ret;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<String> comboBox = (JComboBox<String>) comp;
    mValue =  (String) comboBox.getSelectedItem();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<String> comboBox = (JComboBox<String>) comp;
    comboBox.setSelectedItem(mValue);
  }

  //#########################################################################
  //# Data Members
  // TODO Needs to be EventProxy
  private String mValue;
}
