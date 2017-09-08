//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import org.supremica.properties.BooleanProperty;
import org.supremica.properties.ColorProperty;
import org.supremica.properties.DoubleProperty;
import org.supremica.properties.IntegerProperty;
import org.supremica.properties.ObjectProperty;
import org.supremica.properties.Property;
import org.supremica.properties.PropertyType;
import org.supremica.properties.SupremicaProperties;


public class PropertiesDialog
extends JDialog
{

  public PropertiesDialog(final Frame owner)
  {
    // Create dialog
    super(owner, "Preferences", true);

    // Get the panel of this dialog
    mDialogPanel = (JPanel) getContentPane();
    // Add tabbed pane to panel
    mTabbedPane = new JTabbedPane();
    mDialogPanel.add(mTabbedPane, BorderLayout.CENTER);

    // The panel for controlling the dialog
    mControlPanel = new PropertiesControllerPanel(this);
    mDialogPanel.add(mControlPanel, BorderLayout.SOUTH);

    // For all types of properties
    for (final PropertyType type: PropertyType.values()) {
      // Create a new panel to put in a tabbed pane
      final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      fillPropertyPanel(type, panel);
      // Add panel to tabbed pane (only add nonempty panels)
      if (panel.getComponentCount() > 0) {
        mTabbedPane.add(type.toString(), panel);
      }
    }
    setLocationAndSize();
    //pack();
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(final WindowEvent e)
      {
        doCancel();
      }
    });
  }

  public void doCancel()
  {
    setVisible(false);
  }

  public void doApply()
  {
    // Attempt to set attributes, if successful, save them!
    if (setAttributes()) {
      try {
        // Write the changes to the config file
        SupremicaProperties.saveProperties();
      } catch (final IOException exception) {
        System.err.println("Failed to save changes to config-file: " +
                           exception.getMessage());
      }
      doCancel(); // ok, not really cancel. what we do is to close the dialog
    }
    getOwner().repaint();
  }

  @Override
  public void setVisible(final boolean toVisible)
  {
    if (toVisible) {
      getAttributes();
    }
    super.setVisible(toVisible);
  }

  /**
   * Updates all properties to the current value in Config.
   */
  private void getAttributes()
  {
    for (final Chooser chooser : chooserList) {
      chooser.getFromConfig();
    }
  }


  //#######################################################################
  //# Auxiliary Methods
  private void setLocationAndSize()
  {
    final Rectangle bounds = getOwner().getBounds();
    final int x = bounds.x + (bounds.width - DEFAULT_DIALOG_SIZE.width) / 2;
    final int y = bounds.y + (bounds.height - DEFAULT_DIALOG_SIZE.height) / 2;
    setLocation(x, y);
    setSize(DEFAULT_DIALOG_SIZE);
  }

  /**
   * Set all properties in Config to the current values in this dialog.
   */
  private boolean setAttributes()
  {
    for (final Chooser chooser : chooserList)
    {
      chooser.setInConfig();
    }

    // The old BDD options (Arash's) are treated specially. They method still use integer variables
    // in BDD.Options instead of the Config-values. The BDD.Options values are updated by the
    // SupremicaProperties.saveProperties()-method (which is run after successfully running this method).
    return true;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void fillPropertyPanel(final PropertyType type,
                                 final JPanel panel)
  {
    // Find all properties of this type and add to the panel
    for (final Property property : Property.getAllProperties()) {
      // I only want properties of the current type
      if (property.getPropertyType() == type) {
        // Depending on the kind of property, different choice mechanics...
        final Chooser chooser;
        if (property instanceof BooleanProperty) {
          chooser = new BooleanChooser((BooleanProperty) property);
        } else if (property instanceof IntegerProperty) {
          chooser = new IntegerChooser((IntegerProperty) property);
        } else if (property instanceof DoubleProperty) {
          chooser = new DoubleChooser((DoubleProperty) property);
        } else if (property instanceof ObjectProperty) {
          chooser = new ObjectPropertyChooser((ObjectProperty) property);
        } else if (property instanceof ColorProperty) {
          chooser = new ColorChooser((ColorProperty) property);
        } else {
          continue;
        }
        chooser.setEnabled(!property.isImmutable());
        chooserList.add(chooser);
        panel.add((Component) chooser);
      }
    }
  }


  /**
   * Interface for setting and getting a property from Config.
   */
  interface Chooser
  {
    /**
     * Puts the current value in the config.
     */
    public void setInConfig();

    /**
     * Updates to current value in the config.
     */
    public void getFromConfig();

    /**
     * Enables or disables this control.
     */
    public void setEnabled(boolean enable);

    /**
     * Gets the label used to describe the property in the dialog.
     * Not really relevant here, but makes things immensely more easy
     * for {@link PropertiesControllerPanel.SearchAction}.
     */
    public String getLabel();
  }

  private class BooleanChooser
  extends JCheckBox
  implements Chooser
  {
    private static final long serialVersionUID = 1L;

    private final BooleanProperty property;

    BooleanChooser(final BooleanProperty property)
    {
      super(property.getComment(), property.get());
      this.property = property;
    }

    @Override
    public void setInConfig()
    {
      if (!property.isImmutable())
        property.set(isSelected());
    }

    @Override
    public void getFromConfig()
    {
      setSelected(property.get());
    }

    @Override
    public String getLabel() { return getText(); }
  }

  /**
   * Chooser for IntegerProperty:s. The chooser is a JFormattedTextField and if the property has a range
   * (a min and a max value) then there's also a JSlider.
   */
  private class IntegerChooser
  extends JPanel
  implements Chooser
  {
    private static final long serialVersionUID = 1L;

    private final IntegerProperty property;

    JFormattedTextField text;
    JSlider slider;
    JLabel label = null;
    NumberFormat numberFormat;

    IntegerChooser(final IntegerProperty property)
    {
      super();
      this.property = property;

      // Label
      this.label = new JLabel(property.getComment());
      this.add(label);

      // JFormattedTextField!
      numberFormat = NumberFormat.getIntegerInstance();
      final NumberFormatter formatter = new NumberFormatter(numberFormat);
      formatter.setMinimum(new Integer(property.getMinValue()));
      formatter.setMaximum(new Integer(property.getMaxValue()));
      text = new JFormattedTextField(formatter);
      text.setColumns(Math.max((property.get()+"").length()+1,4));

      // If there is a closed range, also create a slider
      if (property.getMinValue() == Integer.MIN_VALUE || property.getMaxValue() == Integer.MAX_VALUE)
      {
        slider = null;
      }
      else
      {
        // JSlider!
        slider = new JSlider(property.getMinValue(), property.getMaxValue(), property.get());
        slider.setMajorTickSpacing(property.getTick()*2);
        slider.setMinorTickSpacing(property.getTick());
        slider.setSnapToTicks(true);
        //slider.setPaintTicks(true);
        //slider.createStandardLabels(property.getTick());
        //slider.setPaintLabels(true);
        this.add(slider);

        // Add listeners for updating text and slider to correspond
        text.addPropertyChangeListener(new PropertyChangeListener()
        {
          // If the text changes value, update the slider
          @Override
          public void propertyChange(final PropertyChangeEvent e)
          {
            // Is the value being changed?
            if ("value".equals(e.getPropertyName()))
            {
              final Number value = (Number) e.getNewValue();
              if (value != null)
              {
                slider.setValue(value.intValue());
              }
            }
          }
        });
        slider.addChangeListener(new ChangeListener()
        {
          // If the slider changes state, update the text
          @Override
          public void stateChanged(final ChangeEvent e)
          {
            slider.setValue((int) (Math.round((double) (slider.getValue()) / property.getTick()) * property.getTick()));
            if (!slider.getValueIsAdjusting())
            {
              text.setValue(new Integer(slider.getValue()));
            }
            else
            {
              //value is adjusting; just set the text
              text.setText(String.valueOf(slider.getValue()));
            }
          }
        });
      }

      // Lastly, add text
      this.add(text);
    }

    @Override
    public void setInConfig()
    {
      if (!property.isImmutable())
      {
        try
        {
          final Number num = numberFormat.parse(text.getText());
          property.set(num.intValue());
        }
        catch (final ParseException ex)
        {
          System.err.println("ParseException: " + ex.getMessage());
          // Error in number format, ignore this result without error message!
        }
        catch (final NumberFormatException ex)
        {
          System.err.println("NumberFormatException: " + ex.getMessage());
          // Error in number format, ignore this result without error message!
        }
        catch (final IllegalArgumentException ex)
        {
          System.err.println("Error setting value of property " + property + ", value out of range.");
        }
      }
    }

    @Override
    public void getFromConfig()
    {
      text.setText(""+property.get());
    }

    @Override
    public String getLabel() {  return label.getText(); }
  }

  /**
   * Chooser for DoubleProperties. The chooser is a JFormattedTextField.
   */
  private class DoubleChooser
  extends JPanel
  implements Chooser
  {
    private static final long serialVersionUID = 1L;

    private final DoubleProperty property;

    JFormattedTextField text;
    JLabel label = null;
    //        JSlider slider;

    DoubleChooser(final DoubleProperty property)
    {
      super();
      this.property = property;

      // Label
      this.label = new JLabel(property.getComment());
      this.add(label);

      // JFormattedTextField!
      final NumberFormat numberFormat = NumberFormat.getNumberInstance();
      numberFormat.setParseIntegerOnly(false);
      final NumberFormatter formatter = new NumberFormatter(numberFormat);
      formatter.setMinimum(new Double(property.getMinValue()));
      formatter.setMaximum(new Double(property.getMaxValue()));
      text = new JFormattedTextField(formatter);
      text.setColumns(Math.max((property.get()+"").length()+1,6));

      // Lastly, add text
      this.add(text);
    }

    @Override
    public void setInConfig()
    {
      if (!property.isImmutable())
      {
        try
        {
          property.set(Double.parseDouble(text.getText()));
        }
        catch (final NumberFormatException ex)
        {
          // Error in number format, ignore this result without error message!
        }
        catch (final IllegalArgumentException ex)
        {
          System.err.println("Error setting value of property " + property + ", value out of range.");
        }
      }
    }

    @Override
    public void getFromConfig()
    {
      text.setText(""+property.get());
    }

    @Override
    public String getLabel() {  return label.getText(); }
  }


  //#########################################################################
  //# Inner Class ObjectPropertyChooser
  /**
   * Chooser for {@link ObjectProperty} items. If the property has a set of
   * legal values, this becomes a {@link JComboBox} with those as choices,
   * otherwise this becomes an editable JTextField.
   */
  private class ObjectPropertyChooser<T> extends JPanel implements Chooser
  {

    //#######################################################################
    //# Constructor
    private ObjectPropertyChooser(final ObjectProperty<T> property)
    {
      mProperty = property;
      mLabel = new JLabel(property.getComment());
      add(mLabel);
      if (property.getLegalValues() == null) {
        mTextField = new JTextField();
        mTextField.setColumns(Math.max(property.get().toString().length() + 1, 5));
        this.add(mTextField);
      } else {
        mComboBox = new JComboBox<T>(property.getLegalValues());
        if (property.get() != null) {
          mComboBox.setSelectedItem(property.get());
        }
        this.add(mComboBox);
      }
    }

    //#######################################################################
    //# Interface org.supremica.gui.PropertiesDialog.Chooser
    @Override
    public void setInConfig()
    {
      if (!mProperty.isImmutable()) {
        if (mComboBox != null) {
          final Object selected = mComboBox.getSelectedItem();
          @SuppressWarnings("unchecked")
          final T value = (T) selected;
          mProperty.setValue(value);
        } else if (mTextField != null) {
          mProperty.set(mTextField.getText());
        }
      }
    }

    @Override
    public void getFromConfig()
    {
      if (mTextField != null) {
        mTextField.setText(mProperty.getAsString());
      } else {
        mComboBox.setSelectedItem(mProperty.get());
      }
    }

    @Override
    public String getLabel()
    {
      return mLabel.getText();
    }

    //#######################################################################
    //# Data Members
    private final ObjectProperty<T> mProperty;
    private JTextField mTextField = null;
    private JComboBox<T> mComboBox = null;
    private JLabel mLabel = null;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Inner Class ColorChooser
  /**
   * Chooser for colour properties.
   * Consists of a label describing the property and a button showing
   * the colour. When the button is clicked, a {@link JColorChooser}
   * dialog pops up.
   */
  private class ColorChooser
    extends JPanel
    implements ActionListener, Chooser
  {
    //#######################################################################
    //# Constructors
    private ColorChooser(final ColorProperty property)
    {
      mProperty = property;
      mLabel = new JLabel(property.getComment());
      add(mLabel);
      mButton = new JButton("Click to change");
      final Border bevel = BorderFactory.createLoweredBevelBorder();
      final Border empty = BorderFactory.createEmptyBorder(4, 6, 4, 6);
      final Border border = BorderFactory.createCompoundBorder(bevel, empty);
      mButton.setBorder(border);
      mButton.setFocusPainted(false);
      getFromConfig();
      mButton.addActionListener(this);
      add(mButton);
    }

    //#######################################################################
    //# Interface org.supremica.properties.PropertiesDialog.Chooser
    @Override
    public void setInConfig()
    {
      if (!mProperty.isImmutable()) {
        final Color color = getColor();
        mProperty.set(color);
      }
    }

    @Override
    public void getFromConfig()
    {
      final Color color = mProperty.get();
      setColor(color);
    }

    @Override
    public String getLabel()
    {
      return mLabel.getText();
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      final String title = "Choose " + mProperty.getComment();
      final Color color = getColor();
      final Color newcolor = JColorChooser.showDialog(this, title, color);
      if (newcolor != null) {
        setColor(newcolor);
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private Color getColor()
    {
      return mButton.getBackground();
    }

    private void setColor(final Color color)
    {
      mButton.setBackground(color);
      if (30 * color.getRed() + 59 * color.getGreen() +
        11 * color.getBlue() > 12750) {
        mButton.setForeground(Color.BLACK);
      } else {
        mButton.setForeground(Color.WHITE);
      }
    }

    //#######################################################################
    //# Data Members
    private final ColorProperty mProperty;
    private final JLabel mLabel;
    private final JButton mButton;

    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Instance Variables
  private JPanel mDialogPanel = null;
  /** Where the properties show up. */
  private JTabbedPane mTabbedPane = null;
  /** The place for control buttons for this dialog. */
  private PropertiesControllerPanel mControlPanel = null;

  private final List<Chooser> chooserList = new LinkedList<Chooser>();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6389172563818103330L;
  private static final Dimension DEFAULT_DIALOG_SIZE = new Dimension(700, 525);

}


class PropertiesControllerPanel
extends JPanel
{
  private static final long serialVersionUID = 1L;
  @SuppressWarnings("unused")
  private PropertiesDialog theDialog = null;

  public PropertiesControllerPanel(final PropertiesDialog theDialog)
  {
    this.theDialog = theDialog;

    @SuppressWarnings("unused")
    final
    Box buttonBox = new Box(BoxLayout.X_AXIS);

    /*
     * Implementing a search function for the config dialog
     */
    final JTextField searchStr = new JTextField(20);
    searchStr.setPreferredSize(searchStr.getPreferredSize());
    final Action searchAction = new SearchAction(theDialog, searchStr);
    searchStr.addActionListener(searchAction);
    final JButton searchButton = new JButton(searchAction);
    final JPanel searchPanel = new JPanel();
    searchPanel.add(searchStr);
    searchPanel.add(searchButton);
    add(searchPanel, BorderLayout.WEST);
    add(Box.createHorizontalGlue());


    final Action applyAction = new ApplyChangesAction(theDialog);
    final JButton applyButton = new JButton(applyAction);
    theDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"apply");
    theDialog.getRootPane().getActionMap().put("apply", applyAction);
    add(applyButton, BorderLayout.EAST);

    final Action cancelAction = new CancelDialogAction(theDialog);
    final JButton cancelButton = new JButton(cancelAction);
    theDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancel");
    theDialog.getRootPane().getActionMap().put("cancel", cancelAction);
    add(cancelButton, BorderLayout.EAST);
  }


  //#######################################################################
  //# Inner Classes
  private class CancelDialogAction
  extends AbstractAction
  {
    private static final long serialVersionUID = 1L;

    private final JDialog dialog;

    public CancelDialogAction(final JDialog dialog)
    {
      super("Cancel");
      putValue(SHORT_DESCRIPTION, "Cancel the dialog without saving the preferences");
      putValue(MNEMONIC_KEY, KeyEvent.VK_C);
      putValue(ACCELERATOR_KEY, KeyEvent.VK_ESCAPE); // Does not work?

      this.dialog = dialog;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
      dialog.setVisible(false);
    }
  }

  class SearchAction
  extends AbstractAction
  {
    JTextField text_field;
    JTabbedPane tabbed_pane;
    int current_tab = 0;
    int current_label = 0;
    Object boxed_obj = null;

    private static final long serialVersionUID = 1L;

    final static String SEARCH_BUTTON_TEXT_1 = "Search";
    final static String SEARCH_BUTTON_TEXT_2 = "Again";

    public SearchAction(final PropertiesDialog dialog, final JTextField text_field)
    {
      super(SEARCH_BUTTON_TEXT_1);
      putValue(SHORT_DESCRIPTION, "Search this Preferences dialog for the given string");
      putValue(MNEMONIC_KEY, KeyEvent.VK_S);

      this.text_field = text_field;
      this.text_field.setToolTipText("Enter string to search for");
      // Get the JTabbedPane which contains all the JPanels with all the options
      final JComponent contp = (JComponent)dialog.getContentPane();  // we know for a JDialog this is really a JComponent
      final java.awt.Component[] components = contp.getComponents();
      for(int i = 0; i < contp.getComponentCount(); i++)
      {
        if(components[i] instanceof JTabbedPane)  // then this is the one! And there can be only one
          this.tabbed_pane = (JTabbedPane)components[i];
      }
    }

    private boolean search_tab(final JPanel tab)
    {
      final int component_count = tab.getComponentCount();
      final java.awt.Component[] components = tab.getComponents();
      final String srch_str = "(?i).*" + text_field.getText() + ".*";

      // Un-box if some element has already been boxed
      if(boxed_obj != null)
      {
        if(boxed_obj instanceof JCheckBox)
        {
          ((JCheckBox)boxed_obj).setBorderPainted(false);
          boxed_obj = null;
        }
        else if(boxed_obj instanceof JPanel)
        {
          ((JPanel)boxed_obj).setBorder(null);  // Unpaint the border
          boxed_obj = null;
        }
        else
          assert false : "instanceof error in search_tab"; // Should not happen!
      }
      while(current_label < component_count)
      {
        /* Elements on the panels can be either
         * BooleanChooser (which is_a JCheckBox (and a Chooser))
         * DoubleChooser (which is_a Chooser and a JPanel)
         * IntegerChooser (which is_a Chooser and a JPanel)
         * StringChooser (which is_a Chooser and a JPanel)
         */
        final java.awt.Component comp = components[current_label++];
        if(comp instanceof JCheckBox)
        {
          final JCheckBox cbox = (JCheckBox)comp;
          if(cbox.getText().matches(srch_str))
          {
            cbox.setBorderPainted(true);
            cbox.setBorder(new javax.swing.border.LineBorder(java.awt.Color.blue, 2));
            boxed_obj = cbox;
            return true;  // found one!
          }
        }
        else if(comp instanceof PropertiesDialog.Chooser)
        {
          final PropertiesDialog.Chooser schooser = (PropertiesDialog.Chooser)comp;
          if(schooser.getLabel().matches(srch_str))
          {
            // If it's a Chooser but not a JCheckBox, then it is a JPanel
            ((JPanel)schooser).setBorder(new javax.swing.border.LineBorder(java.awt.Color.blue, 2));
            boxed_obj = schooser;
            return true;
          }
        }
      }
      current_label = 0;
      return false; // did not find anything new on this tab
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
      // System.out.println("SearchAction.actionPerformed called, we are " + (searching ? "" : "not ") + "searching");

      putValue(NAME, SEARCH_BUTTON_TEXT_2);
      final int component_count = tabbed_pane.getComponentCount();
      final java.awt.Component[] components = tabbed_pane.getComponents();
      while(current_tab < component_count)
      {
        final java.awt.Component comp = components[current_tab];
        if(comp instanceof JPanel)
        {
          tabbed_pane.setSelectedComponent(comp);
          // Search this panels components
          if(!search_tab((JPanel)comp))
          {
            current_tab++;  // we didn't find anything on this tab, go to next
            continue;
          }
          return;
        }
      }
      current_tab = 0;
      tabbed_pane.setSelectedComponent(components[0]);
      putValue(NAME, SEARCH_BUTTON_TEXT_1);
    }
  }

  private class ApplyChangesAction
  extends AbstractAction
  {
    private static final long serialVersionUID = 1L;

    private final PropertiesDialog dialog;

    public ApplyChangesAction(final PropertiesDialog dialog)
    {
      super("Apply");
      putValue(SHORT_DESCRIPTION, "Saves the preferences and closes this dialog");
      putValue(MNEMONIC_KEY, KeyEvent.VK_A);
      putValue(ACCELERATOR_KEY, KeyEvent.VK_ENTER);

      this.dialog = dialog;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
      dialog.doApply();
    }
  }

}
