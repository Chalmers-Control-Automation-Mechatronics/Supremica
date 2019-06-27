package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

import net.sourceforge.waters.model.des.ProductDESProxy;


public class FileParameter extends Parameter
{
  File mValue;

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

    text.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(final FocusEvent e) {}

      @Override
      public void focusLost(final FocusEvent e) {
      //if text empty default to null file
        if(!text.getText().equals("")) {
          mValue = new File(System.getProperty("user.home") + File.separator + "Desktop", text.getText());
          text.setText(getValue().getAbsolutePath());
        }
        else
          mValue = null;
      }
    });

    final ActionListener openFile = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        final JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        //opening a file - jfc.showOpenDialog(null)
        //saving a file  - jfc.showSaveDialog(null)
        if (jfc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
          mValue = jfc.getSelectedFile();
          text.setText(getValue().getAbsolutePath());
        }
      }
    };

    if(getValue() != null)
      text.setText(getValue().getAbsolutePath());

    button.addActionListener(openFile);
    panel.add(text);
    panel.add(button);

    //text.setToolTipText(getDescription());
    //button.setToolTipText(getDescription());

    return panel;
  }

  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    final Component comp = panel.getEntryComponent();
    final JPanel compPanel = (JPanel) comp;
    final JTextField text =  (JTextField) compPanel.getComponent(0);
    //if text empty default to null file
    if(!text.getText().equals(""))
      mValue = new File(text.getText());
    else
      mValue = null;

    //there is text and a directory has been chosen else default to desktop
    /*
    if(!text.getText().equals(""))
      if(mValue != null)
        mValue = new File(mValue.getParent() + File.separator +    text.getText());
      else
        mValue = new File(System.getProperty("user.home") + File.separator + "Desktop", text.getText());
        */
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
    System.out.println("ID: " + getID() + " Name: " + getName() +" Value: " + getValue());
  }

}
