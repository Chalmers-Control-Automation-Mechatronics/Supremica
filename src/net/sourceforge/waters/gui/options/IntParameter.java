package net.sourceforge.waters.gui.options;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * @author Brandon Bassett
 * @code IntParameter(String name, int min, int max)
 */
public class IntParameter extends Parameter {

    private final int min;
    private final int max;
    private final JSlider slider;
    private final JTextField currentValue = new JTextField();

    public IntParameter(final String name, final int min, final int max) {
        super(name);
        this.min = min;
        this.max = max;
        slider = new JSlider(min, max);
        currentValue.setText(String.valueOf(min + max / 2));

        //When focus lost clamp value
        currentValue.addFocusListener(new FocusListener() {
          @Override
          public void focusGained(final FocusEvent e) {}

          @Override
          public void focusLost(final FocusEvent e) {
            clamp();
          }
        });
    }

    public void clamp() {

        if(Integer.valueOf(currentValue.getText()) > max) {
            currentValue.setText(String.valueOf(max));

        }
        else if(Integer.valueOf(currentValue.getText()) < min) {
            currentValue.setText(String.valueOf(min));
        }
    }

    @Override
    public Component getComponent() {
    //  return currentValue;
        return slider;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
