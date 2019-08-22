package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;


public class ControlLoopHideEventsParameter extends Parameter
{

  protected ControlLoopHideEventsParameter(final int id, final String name,
                                           final String description)
  {
    super(id, name, description);
    //mUNCONTROLLABLEList = new ArrayList<EventProxy>();
    //mCONTROLLABLEList = new ArrayList<EventProxy>();
    mUNCONTROLLABLEList = null;
    mCONTROLLABLEList = null;

  }

  public ControlLoopHideEventsParameter(final ControlLoopHideEventsParameter template)
  {
    this(template.getID(), template.getName(), template.getDescription());
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mUNCONTROLLABLEList = ((ControlLoopHideEventsParameter) p).getUncontrollable();
    mCONTROLLABLEList = ((ControlLoopHideEventsParameter) p).getControllable();
  }

  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final JPanel panel = new JPanel();
    final JButton button = new JButton("...");
    final JTextField text = new JTextField();

    text.setColumns(10);

    button.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent arg0)
      {
        final ControlLoopHideDialog hideEvents =
          new ControlLoopHideDialog(model);
        hideEvents.pack();
        hideEvents.setVisible(true);
      }
    });

    panel.add(text);
    panel.add(button);

    return panel;
  }

  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    // TODO Auto-generated method stub
  }

  //Data Members
  private List<EventProxy> mUNCONTROLLABLEList;
  private List<EventProxy> mCONTROLLABLEList;

  @Override
  public String toString()
  {
    return ("ID: " + getID() + " Name: " + getName() + " Controlable: " + getControllable() + " Uncontrolable: " + getUncontrollable());
  }

  public List<EventProxy> getControllable()
  {
      return mCONTROLLABLEList;
  }

  public List<EventProxy> getUncontrollable()
  {
    return mUNCONTROLLABLEList;
  }

  //#########################################################################
  //# Inner Class ControlLoopHideDialo

  private class ControlLoopHideDialog extends JDialog
  {
    //#######################################################################
    //# Constructor
    public ControlLoopHideDialog(final ProductDESContext model)
    {
      super();
      generate(model);
    }

    public void generate(final ProductDESContext model)
    {
      setLayout(new GridLayout(0, 2));

      final DefaultListModel<EventProxy> model_UNCONTROLLABLE_List = new DefaultListModel<EventProxy>();
      final DefaultListModel<EventProxy> model_CONTROLLABLE_List = new DefaultListModel<EventProxy>();

      //Initial generation, uninitialized lists
      if (mCONTROLLABLEList == null) {

        mUNCONTROLLABLEList = new ArrayList<EventProxy>();
        mCONTROLLABLEList = new ArrayList<EventProxy>();

        for (final EventProxy event : model.getProductDES().getEvents()) {
          if (event.getKind() == EventKind.UNCONTROLLABLE) {
            model_UNCONTROLLABLE_List.addElement(event);
          } else if (event.getKind() == EventKind.CONTROLLABLE) {
            model_CONTROLLABLE_List.addElement(event);
          }
        }
      }
      else {
        for(final EventProxy event: mCONTROLLABLEList) {
          model_CONTROLLABLE_List.addElement(event);
        }

        for(final EventProxy event: mUNCONTROLLABLEList) {
          model_UNCONTROLLABLE_List.addElement(event);
        }
      }

      final JList<EventProxy> UNCONTROLLABLE_List = new JList<EventProxy>();
      UNCONTROLLABLE_List.setModel(model_UNCONTROLLABLE_List);

      final JScrollPane Left_UNCONTROLLABLE_Scroller = new JScrollPane();
      Left_UNCONTROLLABLE_Scroller.setViewportView(UNCONTROLLABLE_List);
      UNCONTROLLABLE_List.setLayoutOrientation(JList.VERTICAL);

      final JButton shiftRightButton = new JButton(">>>");
      shiftRightButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          UNCONTROLLABLE_List.getSelectedValuesList().stream()
            .forEach((data) -> {
              model_CONTROLLABLE_List.addElement(data);
              model_UNCONTROLLABLE_List.removeElement(data);
            });
        }
      });

      final JList<EventProxy> CONTROLLABLE_List = new JList<EventProxy>();
      CONTROLLABLE_List.setModel(model_CONTROLLABLE_List);

      final JScrollPane Right_CONTROLLABLE_Scroller = new JScrollPane();
      Right_CONTROLLABLE_Scroller.setViewportView(CONTROLLABLE_List);
      CONTROLLABLE_List.setLayoutOrientation(JList.VERTICAL);

      final JButton shiftLeftButton = new JButton("<<<");
      shiftLeftButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          CONTROLLABLE_List.getSelectedValuesList().stream()
            .forEach((data) -> {
              model_CONTROLLABLE_List.removeElement(data);
              model_UNCONTROLLABLE_List.addElement(data);
            });
        }
      });

      final JButton Ok = new JButton("OK");
      final JButton Cancel = new JButton("Cancel");

      Cancel.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          dispose();
        }
      });

      Ok.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          for (int i = 0; i < model_UNCONTROLLABLE_List.getSize(); i++) {
            mUNCONTROLLABLEList.add(model_UNCONTROLLABLE_List.get(i));
          }

          for (int i = 0; i < model_CONTROLLABLE_List.getSize(); i++) {
            mCONTROLLABLEList.add(model_CONTROLLABLE_List.get(i));
          }
          dispose();
        }
      });

      add(new JLabel("Non-Loop Events"));
      add(new JLabel("Loop Events"));

      add(Left_UNCONTROLLABLE_Scroller);
      add(Right_CONTROLLABLE_Scroller);

      add(shiftRightButton);
      add(shiftLeftButton);

      add(Ok);
      add(Cancel);

    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 4132888698192730783L;
  }
}
