//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   ErrorWindow
//###########################################################################
//# $Id: ErrorWindow.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.*;
import java.awt.GridLayout;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Font;

/** <p>Provides an easy interface for viewing errors which occur.</p>
 *
 * <p>Features a descriptive message and an output text area.</p>
 *
 * @author Gian Perrone
 */
public class ErrorWindow extends JDialog implements ActionListener {
    private JPanel contentPane;

    /** Displays an error message, with reference to pos in buf as the location of the error
     * @param message The error mesage
     * @param buf The input which caused the error
     * @param pos The offset within the input which caused the error.  -1 if not applicable */
    public ErrorWindow(String message, String buf, int pos) {
	setTitle("Waters - Error!");

	//TODO: Use JEditorPane instead, and <pre> tags to make it do fixed-width font rendering

	// Center this element on the screen
	setModal(true);
	setLocationRelativeTo(null);

	contentPane = new JPanel();

        JFrame.setDefaultLookAndFeelDecorated(true);

	Box b = new Box(BoxLayout.PAGE_AXIS);

	contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
	
	JPanel messagePanel = new JPanel();
	messagePanel.add(new JLabel("<html><b>Error:</b> " + message + "</html>"));

	contentPane.add(messagePanel);

	JPanel buttonPanel = new JPanel();
	JButton okButton = new JButton("OK");
	okButton.addActionListener(this);
	okButton.setActionCommand("ok");
	buttonPanel.add(okButton);
	
	JEditorPane outArea = new JEditorPane("text/html", "");
	outArea.setEditable(false);

	//outArea.setFont(new Font("system", Font.PLAIN, 12));

	String text = "<html>";

	if(pos == -1) {
	    text += "<b>Error:</b>\n" + buf;
	}

	else {
	    text += "<b>Error at position " + (pos+1) + " in:</b><br>\n";
	    text += "<pre>" + buf + "\n";
	    for(int i = 0; i<pos; i++) {
		text += "-";
	    }

	    text += "^</pre></html>\n";
	}

	outArea.setText(text);

	contentPane.add(new JScrollPane(outArea));
	contentPane.add(buttonPanel);
	setContentPane(contentPane);
	pack();
	show();
    }

    public void actionPerformed(ActionEvent e) {
	if(e.getActionCommand().equals("ok")) {
	    dispose();
	}
    }
}
	    
