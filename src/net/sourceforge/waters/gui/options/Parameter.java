package net.sourceforge.waters.gui.options;

import java.awt.Component;

import javax.swing.JLabel;

/**
 * @author Brandon Bassett
 * @description Abstract class for parameters, stores the default ID's
 */

public abstract class Parameter{

        @SuppressWarnings("unused")
        private final String name;
        private final JLabel label;
        private Component component;

        public Parameter(final String name){
            this.name = name;
            label = new JLabel(name);
        }

        public JLabel getLabel() {
            return label;
        }

        public Component getComponent() {
            return component;
        }
}
