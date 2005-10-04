/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain However, it is freely
 * available without fee for education, research, and non-profit purposes. By
 * obtaining copies of this and other files that comprise the Supremica
 * software, you, the Licensee, agree to abide by the following conditions and
 * understandings with respect to the copyrighted software:
 *
 * The software is copyrighted in the name of Supremica, and ownership of the
 * software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its documentation for
 * education, research, and non-profit purposes is hereby granted to Licensee,
 * provided that the copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all such copies, and
 * that no charge be made for such copies. Any entity desiring permission to
 * incorporate this software into commercial products or to use it for
 * commercial purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org Supremica, Haradsgatan 26A 431 42
 * Molndal SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are available.
 *
 * Licensee may not use the name, logo, or any other symbol of Supremica nor the
 * names of any of its employees nor any adaptation thereof in advertising or
 * publicity pertaining to the software without specific prior written approval
 * of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE SUITABILITY OF THE
 * SOFTWARE FOR ANY PURPOSE. IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED
 * WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages suffered by Licensee from
 * the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
/**
 * @author Goran Cengic
 */
package org.supremica.gui.fbruntime;

import javax.swing.JFrame;
import com.nwoods.jgo.JGoView;


public class FBRuntimeMainFrame extends JFrame {

    private javax.swing.JButton jButton1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToolBar jToolBar1;
    

    /** Creates new form FBRuntimeMainFrame */
    public FBRuntimeMainFrame() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jTextPane1 = new javax.swing.JTextPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("FBRuntime");
        setFont(new java.awt.Font("Arial", 0, 12));
        setName("mainFrame");
        jButton1.setText("jButton1");
        jToolBar1.add(jButton1);

        getContentPane().add(jToolBar1, java.awt.BorderLayout.NORTH);

        jTextPane1.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        jTextPane1.setBorder(new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jTextPane1.setText("Status Bar");
        getContentPane().add(jTextPane1, java.awt.BorderLayout.SOUTH);

        jMenu1.setText("Menu");
        jMenu1.setFont(new java.awt.Font("Arial", 1, 12));
        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FBRuntimeGUI().setVisible(true);
            }
        });
    }
        
}
