/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author shoaei
 */
public class TPDialog {
    
    private File[] files;
    private boolean TPSelected;
    private boolean importSelected;
    private final JDialog dialog;
    private JTextField jTextLocal;
    private JTextField jTextShare;
    private JCheckBox jBoxShare;
    private JCheckBox jBoxLocal;
    private boolean isShareSelected;
    
    public TPDialog(final Frame parentFrame){
        dialog = new JDialog(parentFrame, true);
        initComponents();
        files = null;
        TPSelected = false;
        importSelected = false;             
        dialog.setTitle("Transition Projection Panel");
        dialog.setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel jPanel = new javax.swing.JPanel();
        JButton jButtonImport = new javax.swing.JButton();
        JButton jButtonTP = new javax.swing.JButton();
        jTextLocal = new javax.swing.JTextField();
        jTextShare = new javax.swing.JTextField();
        jBoxShare = new javax.swing.JCheckBox("Shared events");
        jBoxLocal = new javax.swing.JCheckBox("Local events");
        dialog.setResizable(false);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });
        
        jButtonImport.setText("Import ADS ...");
        jButtonImport.setPreferredSize(new java.awt.Dimension(120, 23));
        jButtonImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonImportActionPerformed(evt);
            }
        });
        
        jButtonTP.setText("Compute Transition Projection");
        jButtonTP.setPreferredSize(new java.awt.Dimension(175, 23));
        jButtonTP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTPActionPerformed(evt);
            }
        });
        
        jBoxLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBoxLocalActionPerformed(evt);
            }

        });        
        
        jBoxShare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBoxShareActionPerformed(evt);
            }
        });        
        
        jTextLocal.setPreferredSize(new java.awt.Dimension(175, 23));
        jTextLocal.setEnabled(false);
        jTextShare.setPreferredSize(new java.awt.Dimension(175, 23));
        jTextShare.setEnabled(false);
        javax.swing.GroupLayout jPanelLayout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanelLayout);
        jPanelLayout.setHorizontalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jBoxLocal)
                .addComponent(jTextLocal)
                .addComponent(jBoxShare)
                .addComponent(jTextShare)
                .addComponent(jButtonTP)
                .addComponent(jButtonImport))
                .addContainerGap(40, Short.MAX_VALUE)));
        jPanelLayout.setVerticalGroup(
            jPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBoxLocal)
                .addComponent(jTextLocal)
                .addContainerGap(10, Short.MAX_VALUE)                
                .addComponent(jBoxShare)
                .addComponent(jTextShare)
                .addContainerGap(10, Short.MAX_VALUE)                
                .addComponent(jButtonTP)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addContainerGap()
                .addComponent(jButtonImport)
                .addContainerGap(10, Short.MAX_VALUE)
                )
        );

        dialog.add(jPanel, java.awt.BorderLayout.CENTER);

        dialog.pack();        
    }

    private void jBoxLocalActionPerformed(ActionEvent evt) {
        if(jTextLocal.isEnabled())
            jTextLocal.setEnabled(false);
        else
            jTextLocal.setEnabled(true);
    }

    private void jBoxShareActionPerformed(ActionEvent evt) {
        if(jTextShare.isEnabled())
            jTextShare.setEnabled(false);
        else
            jTextShare.setEnabled(true);
    }
    
    private void jButtonTPActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        TPSelected = true;
        dialog.setVisible(false);
        dialog.dispose();
    }

    private void jButtonImportActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:

        final String cwd = System.getProperty("user.dir");
        final JFileChooser fc = new JFileChooser(cwd);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = new FileNameExtensionFilter("ADS file", "ADS");
        fc.setFileFilter(filter);
        fc.setMultiSelectionEnabled(true);
        fc.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())) {
                    // Open or Save was clicked
                    files = fc.getSelectedFiles();
                    importSelected = true;
                    // Hide dialog
                    fc.setVisible(false);
                    dialog.setVisible(false);
                    dialog.dispose();
                } else if (JFileChooser.CANCEL_SELECTION.equals(evt.getActionCommand())) {
                    // Cancel was clicked
                    importSelected = false;
                    // Hide dialog
                    fc.setVisible(false);
                }
            }        
        });
        fc.showOpenDialog(dialog);
    }    
    
    public void show(){
        dialog.setVisible(true);
    }
    
    public boolean isImportSelected(){
        return importSelected;
    }
    
    public boolean isTPSelected(){
        return TPSelected;
    }

    public File[] getSelectedFiles() {
        return files;
    }
    
    public String getLocalText(){
        return jTextLocal.getText();
    }

    public String getShareText(){
        return jTextShare.getText();
    }
    
    public boolean isShareSelected(){
        return jBoxShare.isSelected();
    }
    
    public boolean isLocalSelected(){
        return jBoxLocal.isSelected();
    }
}
