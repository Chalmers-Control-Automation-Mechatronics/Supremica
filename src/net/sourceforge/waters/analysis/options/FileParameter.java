package net.sourceforge.waters.analysis.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import net.sourceforge.waters.model.des.ProductDESProxy;


public class FileParameter extends Parameter
{

  File mFile;

  public FileParameter(final int id, final String name,
                       final String description)
  {
    super(id, name, description);
  }

  @Override
  public Component createComponent(final ProductDESProxy model)
  {
    // TODO Should have text field plus button
    final JButton button = new JButton("Open File");

    final ActionListener openFile = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        final JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        //opening a file - jfc.showOpenDialog(null)
        //saving a file  - jfc.showSaveDialog(null)
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          mFile = jfc.getSelectedFile();
         // System.out.println("Absolute Path: " + mFile);
         // System.out.println("Name: " + mFile.getName());
        }
      }
    };

    button.addActionListener(openFile);

    return button;
  }

  @Override
  public void updateFromGUI(final ParameterPanel panel)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void displayInGUI(final ParameterPanel panel)
  {
    // TODO Auto-generated method stub
  }

  public File getFile()
  {
    return mFile;
  }

}
