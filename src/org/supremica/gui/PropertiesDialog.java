//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica
//# PACKAGE: org.supremica.gui
//# CLASS:   PropertiesDialog
//###########################################################################
//# $Id$
//###########################################################################

/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
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
    private static final long serialVersionUID = 1L;

    private final Frame owner;
    private JPanel dialogPanel = null;
    /** Where the properties show up. */
    private JTabbedPane tabbedPane = null;
    /** The place for control buttons for this dialog. */
    private PropertiesControllerPanel controlPanel = null;

    private final List<Chooser> chooserList = new LinkedList<Chooser>();

    public PropertiesDialog(final Frame owner)
    {
        // Create dialog
        super(owner, "Preferences", true);

        // Remember owner
        this.owner = owner;

        // Get the panel of this dialog
        dialogPanel = (JPanel) getContentPane();
        // Add tabbed pane to panel
        tabbedPane = new JTabbedPane();
        dialogPanel.add(tabbedPane, BorderLayout.CENTER);

        // The panel for controlling the dialog
        controlPanel = new PropertiesControllerPanel(this);
        dialogPanel.add(controlPanel, BorderLayout.SOUTH);

        // For all types of properties
        for (final PropertyType type: PropertyType.values())
        {
            // Create a new panel to put in a tabbed pane
            final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

            /*
            JPanel panel = new JPanel();
            BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
            panel.setLayout(layout);
            */

	    fillPropertyPanel(type, panel);
            /*
            JScrollPane pane = new JScrollPane(panel);
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            */

            // Add panel to tabbed pane (only add nonempty panels)
            if (panel.getComponentCount() > 0)
            {
                tabbedPane.add(type.toString(), panel);
            }
        }

        setSize(700, 525);
        //pack();

        // Center the window
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height)
        {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width)
        {
            frameSize.width = screenSize.width;
        }

        setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(final WindowEvent e)
            {
                doCancel();
            }
        });
    }

    public Frame getOwnerFrame()
    {
        return owner;
    }

    public void doCancel()
    {
        setVisible(false);
    }

    public void doApply()
    {
        // Attept to set attributes, if successful, save them!
        if (setAttributes())
        {
            try
            {
                // Write the changes to the config file
                SupremicaProperties.saveProperties();
            }
            catch (final IOException exx)
            {
                System.err.println("Failed to save changes to config-file: " + exx.getMessage());
            }

            doCancel();    // ok, not really cancel. what we do is to close the dialog
        }

        owner.repaint();
    }

    public void setVisible(final boolean toVisible)
    {
        if (toVisible)
        {
            getAttributes();
        }

        super.setVisible(toVisible);
    }

    /**
     * Update all properties to the current value in Config.
     */
    private void getAttributes()
    {
        for (final Chooser chooser : chooserList)
        {
            chooser.getFromConfig();
        }
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
            chooser = new StringChooser((ObjectProperty) property);
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
         * for {@link #SearchAction}.
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
            super(property.getComment(), ((BooleanProperty) property).get());
            this.property = property;
        }

        public void setInConfig()
        {
            if (!property.isImmutable())
                property.set(isSelected());
        }

        public void getFromConfig()
        {
            setSelected(property.get());
        }

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

        public void getFromConfig()
        {
            text.setText(""+property.get());
        }

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

        public void getFromConfig()
        {
            text.setText(""+property.get());
        }

        public String getLabel() {  return label.getText(); }
    }

    /**
     * Chooser for StringProperties. If the StringProperty has a set of legal values,
     * this becomes a JComboBox with those as choices, otherwise this becomes an
     * editable JTextField.
     */
    private class StringChooser
        extends JPanel
        implements Chooser
    {
        private static final long serialVersionUID = 1L;

        private final ObjectProperty property;

        private JTextField text = null;
        private JComboBox selector = null;
        private JLabel label = null;

        StringChooser(final ObjectProperty property)
        {
            super();
            this.property = property;
            this.label = new JLabel(property.getComment());
            this.add(label);

            if (property.legalValues() == null)
            {
                text = new JTextField();
                text.setColumns(Math.max(property.get().toString().length()+1,5));
                this.add(text);
            }
            else
            {
                selector = new JComboBox(property.legalValues());
                if (property.get() != null)
                {
                    selector.setSelectedItem(property.get());
                }
                this.add(selector);
            }
        }

        public void setInConfig()
        {
            if (!property.isImmutable())
            {
                if (selector != null)
                {
                    if (selector.getSelectedItem() != null)
                    {
                        property.set(selector.getSelectedItem());
                    }
                }
                else if (text != null)
                {
                    property.set(text.getText());
                }
            }
        }

        public void getFromConfig()
        {
            if (text != null)
                text.setText(property.getAsString());
            else
                selector.setSelectedItem(property.get());
        }

        public String getLabel() {  return label.getText(); }
    }


    //#######################################################################
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
      //#####################################################################
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

      //#####################################################################
      //# Interface org.supremica.properties.PropertiesDialog.Chooser
      public void setInConfig()
      {
        if (!mProperty.isImmutable()) {
          final Color color = getColor();
          mProperty.set(color);
        }
      }

      public void getFromConfig()
      {
        final Color color = mProperty.get();
        setColor(color);
      }

      public String getLabel()
      {
        return mLabel.getText();
      }

      //#####################################################################
      //# Interface java.awt.event.ActionListener
      public void actionPerformed(final ActionEvent event)
      {
        final String title = "Choose " + mProperty.getComment();
        final Color color = getColor();
        final Color newcolor = JColorChooser.showDialog(this, title, color);
        if (newcolor != null) {
          setColor(newcolor);
        }
      }

      //#####################################################################
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

      //#####################################################################
      //# Data Members
      private final ColorProperty mProperty;
      private final JLabel mLabel;
      private final JButton mButton;

      private static final long serialVersionUID = 1L;
    }

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

        public void actionPerformed(final ActionEvent e)
        {
            dialog.setVisible(false);
        }
    }

    private class SearchAction
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

        public void actionPerformed(final ActionEvent e)
        {
            dialog.doApply();

            /*
            // Update LookAndFeel
            String lookAndFeel = Config.GENERAL_LOOKANDFEEL.get();

            try
            {
                if ((lookAndFeel == null) || "System".equalsIgnoreCase(lookAndFeel))
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                else if ("Metal".equalsIgnoreCase(lookAndFeel))
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                else if ("Motif".equalsIgnoreCase(lookAndFeel))
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
                else if ("Windows".equalsIgnoreCase(lookAndFeel))
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                else if ("Mac".equalsIgnoreCase(lookAndFeel))
                    UIManager.setLookAndFeel("javax.swing.plaf.mac.MacLookAndFeel");
                else if ("GTK".equalsIgnoreCase(lookAndFeel))
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
                else
                    UIManager.setLookAndFeel(lookAndFeel);
            }
            catch (Exception ex)
            {
                System.err.println("Error while setting look and feel: " + ex);
                System.err.println("Reverting to System look and feel.");
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception nope)
                {
                    System.err.println(nope);
                    System.exit(0);
                }
            }
             */
        }
    }
}
