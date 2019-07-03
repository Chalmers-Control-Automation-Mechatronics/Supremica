package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComboBox;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.EventKind;

public class EventParameter extends Parameter
{
  public EventParameter(final int id, final String name, final String description, final boolean allowNull)
  {
    super(id, name, description);
    mValue = null;
    allowNullEvent = allowNull;
  }

  @Override
  public Component createComponent(final ProductDESProxy model)
  {

    final List<EventProxy> propositions = new ArrayList<>();

    final ProductDESProxyFactory factory = ProductDESElementFactory.getInstance();
    final EventProxy noEvent = factory.createEventProxy("(none)", EventKind.PROPOSITION);
    //final EventProxy accepting = factory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME, EventKind.PROPOSITION);

    if(allowNullEvent)
      propositions.add(noEvent);

    for(final EventProxy event: model.getEvents()) {
      if(event.getKind() == EventKind.PROPOSITION) {
        propositions.add(event);
        //if :accepting exists and this is first creation
        if(event.getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME) && mValue == null)
          mValue = event;
      }
    }

    Collections.sort(propositions);

    //:accepting no in alphabet, default to null event
    if(mValue == null)
      mValue = noEvent;

    final JComboBox<EventProxy> ret = new JComboBox<>(propositions.toArray(new EventProxy[propositions.size()]));
    ret.setSelectedItem(mValue);
    return ret;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<EventProxy> comboBox = (JComboBox<EventProxy>) comp;
    mValue = (EventProxy) comboBox.getSelectedItem();
  }

  public EventProxy getValue() { return mValue; }


  @SuppressWarnings("unchecked")
  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JComboBox<String> comboBox = (JComboBox<String>) comp;
    comboBox.setSelectedItem(mValue);
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((EventParameter) p).getValue();
  }

  @Override
  public void printValue()
  {
    System.out.println("ID: " + getID() + " Name: " + getName() +" Value: " + getValue());
  }


  //#########################################################################
  //# Data Members
  private EventProxy mValue;
  private final boolean allowNullEvent;

}
