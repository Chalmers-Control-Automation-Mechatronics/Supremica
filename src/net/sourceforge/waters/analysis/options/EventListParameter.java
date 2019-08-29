package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * @author Brandon Bassett
 */
public class EventListParameter extends Parameter
{


  protected EventListParameter(final int id,
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
  public Component createComponent(final ProductDESContext model)
  {
    mDESContext = model;

    final JPanel panel = new JPanel();
    final JButton button = new JButton("...");
    text = new JTextField();
    text.setColumns(10);
    text.setEditable(false);

    //Initial generation, uninitialized lists
    if (mControllableList == null) {
      mUncontrollableList = generateUncontrollable();
      mControllableList = generateControllable();
    }

    setText(text);

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

  public void setText(final JTextField text) {

    final ArrayList<EventProxy> tmpCon = (ArrayList<EventProxy>) generateControllable();
    final ArrayList<EventProxy> tmpUncon = (ArrayList<EventProxy>) generateUncontrollable();

    Collections.sort(mControllableList);
    Collections.sort(tmpCon);
    Collections.sort(tmpUncon);

   if(mControllableList.equals(tmpCon))
     text.setText("Controllable");
   else if(mControllableList.equals(tmpUncon))
     text.setText("Uncontrollable");
   else
     text.setText("Custom");

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
    return new EventListTranslater();
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

        for (final EventProxy event: mControllableList) {
          modelControllableList.addElement(event);
        }
        for (final EventProxy event: mUncontrollableList) {
          modelUncontrollableList.addElement(event);
        }

      final JList<EventProxy> uncontrollableList = new JList<EventProxy>();
      uncontrollableList.setModel(modelUncontrollableList);
      uncontrollableList.setCellRenderer(new EventListRenderer(model.getModuleContext()));
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
      controllableList.setCellRenderer(new EventListRenderer(model.getModuleContext()));
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
          setText(text);
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

  // Display icons (see net.sourceforge.waters.gui.EventListCell,
  // use setCellRenderer)

  private class EventListRenderer extends JLabel
    implements ListCellRenderer<EventProxy>
  {

    //#########################################################################
    //# Constructor
    EventListRenderer(final ModuleContext context)
    {
      mContext = context;
    }

    @Override
    public Component getListCellRendererComponent(final JList<? extends EventProxy> list,
                                                  final EventProxy value,
                                                  final int index,
                                                  final boolean isSelected,
                                                  final boolean cellHasFocus)
    {
      //class cast error
      @SuppressWarnings("unused")
      final EventProxy decl = value;
      //final String text = HTMLPrinter.getHTMLString(decl, mContext);
      //final Icon icon = mContext.getIcon(decl);
      //setIcon(icon);
      //final String tooltip = mContext.getToolTipText(decl);

      setText(value.getName());
      setIcon(mDESContext.getEventIcon(value));
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

    //#########################################################################
    //# Data Members
    @SuppressWarnings("unused")
    private final ModuleContext mContext;

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 760104252849112475L;

  }

  // Create KindTranslator to pass events to control loop checker.
  // Loop events are controllable, other uncontrollable.
  // See net.sourceforge.waters.model.analysis.KindTranslator

  //#########################################################################
  //# Inner Class EventListTranslater
  private class EventListTranslater implements KindTranslator{

    @Override
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      return aut.getKind();
    }

    @Override
    public EventKind getEventKind(final EventProxy event)
    {
      if(event.getKind() == EventKind.PROPOSITION)
        return EventKind.PROPOSITION;
      else if(getControllable().contains(event))
        return EventKind.CONTROLLABLE;
      else
        return EventKind.UNCONTROLLABLE;
    }}

  //#######################################################################
  //# Data Members
  private List<EventProxy> mUncontrollableList;
  private List<EventProxy> mControllableList;
  private ProductDESContext mDESContext;
  private JTextField text;

}
