/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.supremica.automata.algorithms.TP;

import java.awt.Frame;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
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
    private final Frame frame;
    private final JDialog dialog;
    
    public TPDialog(final Frame parentFrame){
        dialog = new JDialog(parentFrame, true);
        initComponents();
        files = null;
        TPSelected = false;
        importSelected = false;             
        this.frame = parentFrame;
        dialog.setTitle("Transition Projection Panel");
        dialog.setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel jPanel = new javax.swing.JPanel();
        JButton jButtonImport = new javax.swing.JButton();
        JButton jButtonTP = new javax.swing.JButton();

        dialog.setResizable(false);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        });

        jButtonImport.setText("Import ADS ...");
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel);
        jPanel.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonTP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonImport))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonTP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonImport)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        dialog.add(jPanel, java.awt.BorderLayout.CENTER);

        dialog.pack();        
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
//        fc.setAcceptAllFileFilterUsed(false);
        fc.showOpenDialog(dialog);
        files = fc.getSelectedFiles();
        dialog.setVisible(false);
        dialog.dispose();
        importSelected = true;
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
    
}
