package net.sourceforge.waters.gui.options;

import java.awt.Component;

import javax.swing.JCheckBox;

public class BoolParameter extends Parameter
{
    @SuppressWarnings("unused")
    private final Boolean value;
    private final JCheckBox component;

    public BoolParameter(final String name, final Boolean value){
        super(name);
        this.value = value;
        component = new JCheckBox("Supervisor localization", value);
        component.setToolTipText("If enabled, write debuggin information to log files.");
    }

    @Override
    public Component getComponent() {
        return component;
    }
}

