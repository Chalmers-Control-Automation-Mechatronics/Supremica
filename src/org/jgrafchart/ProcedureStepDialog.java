package org.jgrafchart;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.nwoods.jgo.*;

public class ProcedureStepDialog extends JDialog {

  JPanel panel1 = new JPanel();  
  JLabel label = new JLabel();
  JLabel label1 = new JLabel();
  JTextArea nameField = new JTextArea();
  JTextField nameField1 = new JTextField();
  JButton OKButton = new JButton();
  JButton CancelButton = new JButton();

  public GCDocument myObject;
  public ProcedureStep ps;

  public ProcedureStepDialog(Frame frame, GCDocument obj, ProcedureStep sin, GCView view)
  {
    super(frame, "Step", true);
    try  {
      myObject = obj;
      ps = sin;

      init();
      pack();
      setLocationRelativeTo(view);
      updateDialog();
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }
  public ProcedureStepDialog()
  {
    super((Frame)null, "Step", true);
  }

  private final void init()
  {
    panel1.setLayout(null);
    panel1.setMinimumSize(new Dimension(294, 280));
    panel1.setPreferredSize(new Dimension(294, 280));
    OKButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        OnOK();
      }
    });
    CancelButton.addActionListener(new java.awt.event.ActionListener() {

      public void actionPerformed(ActionEvent e) {
        OnCancel();
      }
    });
    getContentPane().add(panel1);

    OKButton.setText("OK");
    panel1.add(OKButton);
    OKButton.setFont(new Font("Dialog", Font.PLAIN, 12));
    OKButton.setBounds(new Rectangle(60,250,79,22));
    CancelButton.setText("Cancel");
    panel1.add(CancelButton);
    CancelButton.setFont(new Font("Dialog", Font.PLAIN, 12));
    CancelButton.setBounds(new Rectangle(168,250,79,22));


    label.setText("Grafcet Procedure:");
    label.setHorizontalAlignment(JLabel.LEFT);
    panel1.add(label);
    label.setBounds(new Rectangle(50,10,148,24));
    panel1.add(nameField1);
    nameField1.setBounds(new Rectangle(50,40,200,24));
    nameField1.setEnabled(myObject.isModifiable());
    label1.setText("Parameters:");
    label1.setHorizontalAlignment(JLabel.LEFT);
    panel1.add(label1);
    label1.setBounds(new Rectangle(50,80,200,24));
    panel1.add(nameField);
    nameField.setBounds(new Rectangle(50,120,200,100));
    nameField.setEnabled(myObject.isModifiable());
  }

  void updateDialog() {
    if (myObject == null) return;
    nameField1.setText(ps.gp);
    nameField.setText(ps.parameters);
    nameField1.setFont(new Font("Dialog", Font.BOLD, 14));
    nameField.setFont(new Font("Dialog", Font.BOLD, 14));
  }

  void updateData() {
    if (myObject == null) return;
      ps.gp = nameField1.getText();
      ps.parameters = nameField.getText();
  }

  public void addNotify()
  {
    // Record the size of the window prior to calling parents addNotify.
    Dimension d = getSize();

    super.addNotify();

    if (fComponentsAdjusted)
      return;

    // Adjust components according to the insets
    Insets insets = getInsets();
    setSize(insets.left + insets.right + d.width, insets.top + insets.bottom + d
.height);
    Component components[] = getComponents();
    for (int i = 0; i < components.length; i++)
    {
      Point p = components[i].getLocation();
      p.translate(insets.left, insets.top);
      components[i].setLocation(p);
    }
    fComponentsAdjusted = true;
  }

  // Used for addNotify check.
  boolean fComponentsAdjusted = false;

  void OnOK()
  {
    try {
      updateData();
      this.dispose();             // Free system resources
    } catch (Exception e) {
    }
  }

  void OnCancel()
  {
    try {
      this.dispose();             // Free system resources
    } catch (Exception e) {
    }
  }
}
