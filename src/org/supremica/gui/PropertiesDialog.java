
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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.text.NumberFormat;
import javax.swing.*;
import java.util.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.supremica.properties.BooleanProperty;
import org.supremica.properties.IntegerProperty;
import org.supremica.properties.PropertyType;
import org.supremica.properties.Property;
import org.supremica.properties.StringProperty;
import org.supremica.properties.SupremicaProperties;
import javax.swing.text.NumberFormatter;
import org.supremica.properties.Config;

public class PropertiesDialog
    extends JDialog
{
    private static final long serialVersionUID = 1L;
    
    private Frame owner;
    private JPanel dialogPanel = null;
    /** Where the properties show up. */
    private JTabbedPane tabbedPane = null;
    /** The place for controlbuttons for this dialog. */
    private PropertiesControllerPanel controlPanel = null;
    
    private LinkedList<Chooser> chooserList = new LinkedList<Chooser>();
    
    public PropertiesDialog(Frame owner)
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
        for (PropertyType type: PropertyType.values())
        {
            // Create a new panel to put in a tabbed pane
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            
            /*
            JPanel panel = new JPanel();
            BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
            panel.setLayout(layout);
            */

            // Find all properties of this type and add to the panel
            Iterator<Property> it = Property.iterator();
            for (Property property = it.next(); it.hasNext(); property = it.next())
            {
                // I only want properties of the current type
                if (property.getPropertyType() == type)
                {
                    // Depending on the kind of property, different choice mechanics...
                    if (property instanceof BooleanProperty)
                    {
                        BooleanChooser chooser = new BooleanChooser((BooleanProperty) property);
                        chooser.setEnabled(!property.isImmutable());
                        chooserList.add(chooser);
                        panel.add(chooser);
                    }
                    else if (property instanceof IntegerProperty)
                    {
                        IntegerChooser chooser = new IntegerChooser((IntegerProperty) property);
                        chooser.setEnabled(!property.isImmutable());
                        chooserList.add(chooser);
                        panel.add(chooser);
                    }
                    else if (property instanceof StringProperty)
                    {
                        StringChooser chooser = new StringChooser((StringProperty) property);
                        chooser.setEnabled(!property.isImmutable());
                        chooserList.add(chooser);
                        panel.add(chooser);
                    }
                }
            }
            
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
        
        setSize(700, 500);
        //pack();
        
        // Center the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
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
            public void windowClosing(WindowEvent e)
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
            catch (IOException exx)
            {
                System.err.println("Failed to save changes to config-file: " + exx.getMessage());
            }
            
            doCancel();    // ok, not really cancel. what we do is to close the dialog
        }
        
        owner.repaint();
    }
    
    public void setVisible(boolean toVisible)
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
        for (Chooser chooser : chooserList)
        {
            chooser.getFromConfig();
        }
    }
    
    /**
     * Set all properties in Config to the current values in this dialog.
     */
    private boolean setAttributes()
    {
        for (Chooser chooser : chooserList)
        {
            chooser.setInConfig();
        }
        
        return true;
    }
    
    /**
     * Interface for setting and getting a property from Config.
     */
    private interface Chooser
    {
        /**
         * Put the current value in the config.
         */
        public void setInConfig();
        
        /**
         * Update to current value in the config.
         */
        public void getFromConfig();
    }
    
    private class BooleanChooser
        extends JCheckBox
        implements Chooser
    {
        private final BooleanProperty property;
        
        BooleanChooser(BooleanProperty property)
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
    }

    /**
     * Chooser for IntegerProperty:s. The chooser is a JFormattedTextField and if the property has a range 
     * (a min and a max value) then there's also a JSlider.
     */
    private class IntegerChooser
        extends JPanel
        implements Chooser
    {
        private final IntegerProperty property;
        
        JFormattedTextField text;
        JSlider slider;
        
        IntegerChooser(final IntegerProperty property)
        {
            super();
            
            // Label
            JLabel label = new JLabel(property.getComment());
            this.add(label);

            // JFormattedTextField!
            NumberFormat numberFormat = NumberFormat.getIntegerInstance();
            NumberFormatter formatter = new NumberFormatter(numberFormat);
            formatter.setMinimum(new Integer(property.getMinValue()));
            formatter.setMaximum(new Integer(property.getMaxValue()));
            text = new JFormattedTextField(formatter);
            text.setColumns(Math.max((property.get()+"").length()+1,4));

            // If there are limits, create a slider, otherwise just an editable textbox!
            if (property.getMinValue() == Integer.MIN_VALUE || property.getMaxValue() == Integer.MAX_VALUE)
            {
                slider = null;
            }
            else
            {
                // JSlider!
                slider = new JSlider(property.getMinValue(), property.getMaxValue(), property.get());
                slider.setMinorTickSpacing(property.getTick());
                slider.setSnapToTicks(true);
                slider.setPaintLabels(true); // No effect?
                slider.setPaintTrack(true);  // No effect?
                this.add(slider);

                // Add listeners for updating text and slider to correspond
                text.addPropertyChangeListener(new PropertyChangeListener()
                {
                    // If the text changes value, update the slider
                    public void propertyChange(PropertyChangeEvent e)
                    {
                        // Is the value being changed?
                        if ("value".equals(e.getPropertyName()))
                        {
                            Number value = (Number) e.getNewValue();
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
                    public void stateChanged(ChangeEvent e)
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
                        
            this.add(text);

            this.property = property;
        }
        
        public void setInConfig()
        {
            if (!property.isImmutable())
            {
                try
                {
                    property.set(Integer.parseInt(text.getText()));
                }
                catch (NumberFormatException ex)
                {
                    // Error in number format, ignore this result without error message!
                }
                catch (IllegalArgumentException ex)
                {
                    System.err.println("Error setting value of property " + property + ", value out of range.");
                }
            }
        }
        
        public void getFromConfig()
        {
            text.setText(""+property.get());
        }
    }

    /**
     * Chooser for StringProperty:s. If the StringProperty has a set of legal values, 
     * this becomes a JComboBox with those as choices, otherwise this becomes an 
     * editable JTextField.
     */
    private class StringChooser
        extends JPanel
        implements Chooser
    {
        private final StringProperty property;
        
        private JTextField text = null;
        private JComboBox selector = null;
                
        StringChooser(StringProperty property)
        {
            super();
            this.property = property;
            JLabel label = new JLabel(property.getComment());
            this.add(label);
            
            if (property.legalValues() == null)
            {
                text = new JTextField();
                text.setColumns(Math.max(property.get().length()+1,5));
                this.add(text);
            }
            else
            {
                selector = new JComboBox(((StringProperty)property).legalValuesAsStrings());
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
                        property.set(selector.getSelectedItem().toString());
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
                text.setText(property.get());
            else
                selector.setSelectedItem(property.get());
        }
    }
}

class PropertiesControllerPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;
    private PropertiesDialog theDialog = null;
    
    public PropertiesControllerPanel(PropertiesDialog theDialog)
    {
        this.theDialog = theDialog;
        
        Box buttonBox = new Box(BoxLayout.X_AXIS);
        
        Action applyAction = new ApplyChangesAction(theDialog);
        JButton applyButton = new JButton(applyAction);
        theDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"apply");
        theDialog.getRootPane().getActionMap().put("apply", applyAction);
        add(applyButton, BorderLayout.CENTER);

        Action cancelAction = new CancelDialogAction(theDialog);
        JButton cancelButton = new JButton(cancelAction);
        theDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancel");
        theDialog.getRootPane().getActionMap().put("cancel", cancelAction);
        add(cancelButton, BorderLayout.CENTER);
    }
    
    private class CancelDialogAction
        extends AbstractAction
    {
        private final JDialog dialog;
        
        public CancelDialogAction(JDialog dialog)
        {
            super("Cancel");
            putValue(SHORT_DESCRIPTION, "Cancel the dialog without saving the preferences");
            putValue(MNEMONIC_KEY, KeyEvent.VK_C);
            putValue(ACCELERATOR_KEY, KeyEvent.VK_ESCAPE); // Does not work?
      
            this.dialog = dialog;
        }

        public void actionPerformed(ActionEvent e)
        {
            dialog.setVisible(false);
        }
    }

    private class ApplyChangesAction
        extends AbstractAction
    {
        private final PropertiesDialog dialog;
        
        public ApplyChangesAction(PropertiesDialog dialog)
        {
            super("Apply");
            putValue(SHORT_DESCRIPTION, "Saves the preferences and closes this dialog");
            putValue(MNEMONIC_KEY, KeyEvent.VK_A);
            putValue(ACCELERATOR_KEY, KeyEvent.VK_ENTER);
        
            this.dialog = dialog;
        }

        public void actionPerformed(ActionEvent e)
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
