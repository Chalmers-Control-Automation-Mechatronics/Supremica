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


public class EventListParameter extends Parameter
{

  protected EventListParameter(final int id, final String name,
                                           final String description)
  {
    super(id, name, description);
    //mUNCONTROLLABLEList = new ArrayList<EventProxy>();
    //mCONTROLLABLEList = new ArrayList<EventProxy>();
    mUncontrollableList = null;
    mControllableList = null;

  }

  public EventListParameter(final EventListParameter template)
  {
    this(template.getID(), template.getName(), template.getDescription());
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mUncontrollableList = ((EventListParameter) p).getUncontrollable();
    mControllableList = ((EventListParameter) p).getControllable();
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

  @Override
  public String toString()
  {
    return ("ID: " + getID() + " Name: " + getName() + " Controllable: " + getControllable() + " Uncontrollable: " + getUncontrollable());
  }

  // TODO getValue() -- returns "controllable" list
  public List<EventProxy> getControllable()
  {
      return mControllableList;
  }

  public List<EventProxy> getUncontrollable()
  {
    return mUncontrollableList;
  }


  //#########################################################################
  //# Inner Class ControlLoopHideDialog
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

      final DefaultListModel<EventProxy> modelUncontrollableList =
        new DefaultListModel<EventProxy>();
      final DefaultListModel<EventProxy> modelControllableList =
        new DefaultListModel<EventProxy>();

      //Initial generation, uninitialised lists
      if (mControllableList == null) {
        mUncontrollableList = new ArrayList<EventProxy>();
        mControllableList = new ArrayList<EventProxy>();
        for (final EventProxy event : model.getProductDES().getEvents()) {
          if (event.getKind() == EventKind.UNCONTROLLABLE) {
            modelUncontrollableList.addElement(event);
          } else if (event.getKind() == EventKind.CONTROLLABLE) {
            modelControllableList.addElement(event);
          }
        }
      } else {
        for (final EventProxy event: mControllableList) {
          modelControllableList.addElement(event);
        }
        for (final EventProxy event: mUncontrollableList) {
          modelUncontrollableList.addElement(event);
        }
      }

      final JList<EventProxy> uncontrollableList = new JList<EventProxy>();
      uncontrollableList.setModel(modelUncontrollableList);
      final JScrollPane leftUncontrollableScroller = new JScrollPane();
      leftUncontrollableScroller.setViewportView(uncontrollableList);
      uncontrollableList.setLayoutOrientation(JList.VERTICAL);

      final JButton shiftRightButton = new JButton(">>>");
      shiftRightButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          uncontrollableList.getSelectedValuesList().stream()
            .forEach((data) -> {
              modelControllableList.addElement(data);
              modelUncontrollableList.removeElement(data);
            });
        }
      });

      final JList<EventProxy> controllableList = new JList<EventProxy>();
      controllableList.setModel(modelControllableList);
      final JScrollPane rightControllableScroller = new JScrollPane();
      rightControllableScroller.setViewportView(controllableList);
      controllableList.setLayoutOrientation(JList.VERTICAL);

      final JButton shiftLeftButton = new JButton("<<<");
      shiftLeftButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          controllableList.getSelectedValuesList().stream()
            .forEach((data) -> {
              modelControllableList.removeElement(data);
              modelUncontrollableList.addElement(data);
            });
        }
      });

      final JButton okButton = new JButton("OK");
      final JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          dispose();
        }
      });

      okButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
          for (int i = 0; i < modelUncontrollableList.getSize(); i++) {
            mUncontrollableList.add(modelUncontrollableList.get(i));
          }

          for (int i = 0; i < modelControllableList.getSize(); i++) {
            mControllableList.add(modelControllableList.get(i));
          }
          dispose();
        }
      });

      // TODO Use GridBagLayout
      add(new JLabel("Non-Loop Events"));
      add(new JLabel("Loop Events"));

      add(leftUncontrollableScroller);
      add(rightControllableScroller);

      add(shiftRightButton);
      add(shiftLeftButton);

      add(okButton);
      add(cancelButton);
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 4132888698192730783L;
  }


  //#######################################################################
  //# Data Members
  private List<EventProxy> mUncontrollableList;
  private List<EventProxy> mControllableList;

}
