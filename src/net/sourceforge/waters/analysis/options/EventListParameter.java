package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

import net.sourceforge.waters.model.analysis.kindtranslator.ControlLoopKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Brandon Bassett
 */
public class EventListParameter extends Parameter
{

  EventListParameter(final int id,
                     final String name,
                     final String description)
  {
    super(id, name, description);
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
  public Component createComponent(final ProductDESContext context)
  {
    mDESContext = context;

    final JPanel panel = new JPanel();
    final JButton button = new JButton("...");
    mTextField = new JTextField();
    mTextField.setColumns(10);
    mTextField.setEditable(false);

    //Initial generation, uninitialized lists
    if (mControllableList == null) {
      mUncontrollableList = generateUncontrollable();
      mControllableList = generateControllable();
    }

    setText(mTextField);

    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent arg0)
      {
        final EventListDialog dialog = new EventListDialog(context, ((Frame) button.getTopLevelAncestor().getParent()));
        dialog.setLocationRelativeTo(button);
        //Disable interaction with ancestors until dialog closed
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setVisible(true);
      }
    });

    panel.add(mTextField);
    panel.add(button);

    return panel;
  }

  public void setText(final JTextField text)
  {
    final List<EventProxy> tmpCon = generateControllable();
    final List<EventProxy> tmpUncon = generateUncontrollable();
    Collections.sort(mControllableList);
    Collections.sort(tmpCon);
    Collections.sort(tmpUncon);
    if (mControllableList.equals(tmpCon)) {
      text.setText("(controllable events)");
    } else if(mControllableList.equals(tmpUncon)) {
      text.setText("(uncontrollable events)");
    } else {
      text.setText("(custom list)");
    }
  }

  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    // Auto-generated method stub
  }

  @Override
  public String toString()
  {
    return ("ID: " + getID() + " Name: " + getName() + " Controllable: " + getControllable() + " Uncontrollable: " + getUncontrollable());
  }

  public List<EventProxy> getValue()
  {
    return mControllableList;
  }

  public List<EventProxy> getControllable()
  {
    return mControllableList;
  }

  public List<EventProxy> getUncontrollable()
  {
    return mUncontrollableList;
  }

  public KindTranslator getKindTranslator()
  {
    return new ControlLoopKindTranslator(mControllableList);
  }

  //Generate default controllable list
  public List<EventProxy> generateControllable()
  {
    final ArrayList<EventProxy> list = new ArrayList<EventProxy>();
    for (final EventProxy event : mDESContext.getProductDES().getEvents()) {
      if (event.getKind() == EventKind.CONTROLLABLE) {
        list.add(event);
      }
    }
    return list;
  }

  //Generate default uncontrollable list
  public List<EventProxy> generateUncontrollable()
  {
    final ArrayList<EventProxy> list = new ArrayList<EventProxy>();
    for (final EventProxy event : mDESContext.getProductDES().getEvents()) {
      if (event.getKind() == EventKind.UNCONTROLLABLE) {
        list.add(event);
      }
    }
    return list;
  }

  //#########################################################################
  //# Inner Class EventListDialog
  private class EventListDialog extends JDialog
  {
    //#######################################################################
    //# Constructor
    public EventListDialog(final ProductDESContext model, final Frame owner)
    {
      super(owner);
      generate(model);
      setTitle("Loop Events");
    }

    public void generate(final ProductDESContext model)
    {
      setLayout(new GridLayout(0, 2));

      final DefaultListModel<EventProxy> modelUncontrollableList =
        new DefaultListModel<>();
      final DefaultListModel<EventProxy> modelControllableList =
        new DefaultListModel<>();

      for (final EventProxy event: mControllableList) {
        modelControllableList.addElement(event);
      }
      for (final EventProxy event: mUncontrollableList) {
        modelUncontrollableList.addElement(event);
      }

      final JList<EventProxy> uncontrollableList = new JList<EventProxy>();
      uncontrollableList.setModel(modelUncontrollableList);
      uncontrollableList.setCellRenderer(new EventCellRenderer());
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
          sortModel(modelControllableList);
          sortModel(modelUncontrollableList);
        }
      });

      final JList<EventProxy> controllableList = new JList<EventProxy>();
      controllableList.setModel(modelControllableList);
      controllableList.setCellRenderer(new EventCellRenderer());
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

          sortModel(modelControllableList);
          sortModel(modelUncontrollableList);
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
          ArrayList<EventProxy> tmp = new ArrayList<EventProxy>();

          for (int i = 0; i < modelUncontrollableList.getSize(); i++) {
            tmp.add(modelUncontrollableList.get(i));
          }

          mUncontrollableList = tmp;
          tmp = new ArrayList<EventProxy>();

          for (int i = 0; i < modelControllableList.getSize(); i++) {
            tmp.add(modelControllableList.get(i));
          }

          mControllableList = tmp;
          setText(mTextField);
          dispose();
        }
      });

      setLayout(new GridBagLayout());

      final GridBagConstraints constraints = new GridBagConstraints();
      final Insets INSETS = new Insets(2, 4, 2, 4);
      constraints.insets = INSETS;
      constraints.gridx = 0;
      constraints.gridy = 0;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;

      add(new JLabel("Non-Loop Events"), constraints);

      constraints.gridx = 1;
      constraints.gridy = 0;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;

      add(new JLabel("Loop Events"), constraints);

      constraints.gridx = 0;
      constraints.gridy = 1;
      constraints.weightx = 1;
      constraints.weighty = 1;
      constraints.fill = GridBagConstraints.BOTH;

      add(leftUncontrollableScroller, constraints);

      constraints.gridx = 1;
      constraints.gridy = 1;
      constraints.weightx = 1;
      constraints.weighty = 1;
      constraints.fill = GridBagConstraints.BOTH;

      add(rightControllableScroller, constraints);

      constraints.gridx = 0;
      constraints.gridy = 2;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;

      add(shiftRightButton, constraints);

      constraints.gridx = 1;;
      constraints.gridy = 2;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;

      add(shiftLeftButton, constraints);

      constraints.gridx = 0;
      constraints.gridy = 3;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;

      add(okButton, constraints);

      constraints.gridx = 1;
      constraints.gridy = 3;
      constraints.weightx = 0;
      constraints.weighty = 0;
      constraints.fill = GridBagConstraints.NONE;

      add(cancelButton, constraints);

      pack();
      setVisible(true);

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void sortModel(final DefaultListModel model)
    {
      final Object[] data =  model.toArray();
      Arrays.sort(data);
      model.clear();
      for (final Object o : data) {
        model.addElement(o);
      }
    }


    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 4132888698192730783L;
  }


  //#########################################################################
  //# Inner Class EventCellRenderer
  private class EventCellRenderer extends JLabel
    implements ListCellRenderer<EventProxy>
  {
    //#######################################################################
    //# Interface javax.swing.ListCellRenderer<EventProxy>
    @Override
    public Component getListCellRendererComponent(final JList<? extends EventProxy> list,
                                                  final EventProxy event,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
      setText(event.getName());
      setIcon(mDESContext.getEventIcon(event));
      setBorder(new EmptyBorder(2, 2, 2, 2));
      if (isSelected) {
        setBackground(list.getSelectionBackground());
        setForeground(list.getSelectionForeground());
      } else {
        setBackground(list.getBackground());
        setForeground(list.getForeground());
      }
      setOpaque(true);
      return this;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 760104252849112475L;
  }


  //#########################################################################
  //# Data Members
  private List<EventProxy> mUncontrollableList;
  private List<EventProxy> mControllableList;
  private ProductDESContext mDESContext;
  private JTextField mTextField;

}
