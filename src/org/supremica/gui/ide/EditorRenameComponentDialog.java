//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   EditorRenameComponentDialog
//###########################################################################
//# $Id: EditorRenameComponentDialog.java,v 1.1 2007-05-23 07:24:11 avenir Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.event.*;
import javax.swing.*;

import net.sourceforge.waters.gui.*;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

/** <p>A dialog for renaming of components in a module.</p>
*
* <p>This dialog renames components when the "rename" option is selected
* in the module window.</p>
*
* @author Avenir Kobetski
*/
public class EditorRenameComponentDialog 
    extends JDialog 
    implements ActionListener
{
    private JButton okButton = new JButton("OK");    
    private EditorPanelInterface mRoot = null;
    private SimpleComponentSubject component;
    private JTextField mNameInput = new JTextField(16);

    /** Creates a new instance of EditorRenameComponentDialog */
    public EditorRenameComponentDialog(EditorPanelInterface root, SimpleComponentSubject component) 
    {
        this.mRoot = root;
        this.setTitle("Rename Component");
        this.component = component;
        
        mNameInput.setText(component.getIdentifier().getName());

        // Center this element on the screen
        setModal(true);
        setLocationRelativeTo(null);

        Box dialogBox = new Box(BoxLayout.PAGE_AXIS);
        JPanel r1 = new JPanel();
        JPanel r2 = new JPanel();

        dialogBox.add(r1);
        dialogBox.add(r2);

        r1.add(new JLabel("New name: "));
        r1.add(mNameInput);
        mNameInput.addActionListener(this);

        JButton cancelButton = new JButton("Cancel");
        
        okButton.setActionCommand("ok");
        cancelButton.setActionCommand("cancel");
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        r2.add(okButton);
        r2.add(cancelButton);

        mNameInput.selectAll();
        mNameInput.requestFocusInWindow();
        
        this.setContentPane(dialogBox);
        this.pack();
        this.setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if ("ok".equals(e.getActionCommand()))
        {
            final ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
            final String nameText = mNameInput.getText();
            if (nameText == null || nameText.equals(""))
            {
                JOptionPane.showMessageDialog(this, "No name specified", "No name", JOptionPane.ERROR_MESSAGE);
                return;
            }

            IdentifierProxy ident = null;
            try 
            {
                ident = parser.parseIdentifier(nameText);
            } 
            catch (final ParseException exception) 
            {
                ErrorWindow.askRevert(exception, nameText);
                return;
            }
            
            if (nameText.equals(component.getIdentifier().getName()))
            {
                dispose();
                return;
            }

            if (!mRoot.componentNameAvailable(nameText))
            {
                JOptionPane.showMessageDialog(this, "Duplicate name: " + nameText, "Duplicate Name", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            component.getIdentifier().setName(nameText);
            dispose();
        }

        if ("cancel".equals(e.getActionCommand()))
        {
            this.setVisible(false);
            mNameInput.setText("");
        }

        if (e.getSource() == mNameInput)
        {
            okButton.doClick();
        }
    }
}
