package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.waters.model.des.ProductDESProxy;


public class FileParameter extends Parameter
{
  File mValue;
  JFileChooser jfc;

  public FileParameter(final int id, final String name,
                       final String description)
  {
    super(id, name, description);
    mValue = null;
  }

  @Override
  public Component createComponent(final ProductDESProxy model)
  {
    final JPanel panel = new JPanel();
    final JButton button = new JButton("...");
    final JTextField text = new JTextField();

    text.setColumns(10);

    if (getValue() != null)
      text.setText(getValue().getAbsolutePath());

    text.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e){}

      @Override
      public void focusLost(final FocusEvent e)
      {
        final File tmp = new File(text.getText());

        //no parent, default to desktop
        if (!text.getText().equals("") && tmp.getParent() == null) {
          mValue = new File(System.getProperty("user.home") + File.separator
                            + "Desktop", text.getText());
          text.setText(mValue.getAbsolutePath());
        }//file has a parent
        else if (!text.getText().equals("") && tmp.getParent() != null){

          //does parent exist
          if(new File(tmp.getParent()).exists()) {
            mValue = new File(text.getText());
            text.setText(mValue.getAbsolutePath());
          }
          else {
            JOptionPane.showMessageDialog(new JFrame(), "Invalid File");
            text.setText(mValue.getAbsolutePath());
          }

        }//empty textField
        else
          mValue = null;
      }
    });

    final ActionListener saveFile = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        //Go to parent directory, text field set to file name
        if (mValue != null) {
          jfc = new JFileChooser(mValue.getParent());
          jfc.setSelectedFile(mValue);
        } else
          jfc = new JFileChooser();

        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
          mValue = jfc.getSelectedFile();
          text.setText(getValue().getAbsolutePath());
        }
      }
    };

    button.addActionListener(saveFile);
    panel.add(text);
    panel.add(button);

    return panel;
  }

  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JPanel compPanel = (JPanel) comp;
    final JTextField text = (JTextField) compPanel.getComponent(0);
    //if text empty default to null file
    if (!text.getText().equals(""))
      mValue = new File(text.getText());
    else
      mValue = null;
  }

  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    // TODO Auto-generated method stub
  }

  public File getValue()
  {
    return mValue;
  }

  @Override
  public void updateFromParameter(final Parameter p)
  {
    mValue = ((FileParameter) p).getValue();
  }

  @Override
  public void printValue()
  {
    System.out.println("ID: " + getID() + " Name: " + getName() + " Value: "
                       + getValue());
  }

}
