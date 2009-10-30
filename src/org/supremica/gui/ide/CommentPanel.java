package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import net.sourceforge.waters.subject.base.DocumentSubject;

/**
 * A panel with editable information about a document. The information is divided into a header
 * (the name of the document) and a body (the comment of the document).
 */
class CommentPanel
    extends JPanel
{
    private static final long serialVersionUID = 1L;

    public CommentPanel(final DocumentSubject document)
    {
        super();
        this.setLayout(new BorderLayout());

        try
        {
            // Add stuff to a panel
            // Create title
            final JTextPane titlePane = new JTextPane();
            titlePane.setFont(new Font(null, Font.BOLD, 14));
            StyledDocument titleDoc = titlePane.getStyledDocument();
            titleDoc.insertString(titleDoc.getLength(), document.getName(), null);
            this.add(BorderLayout.NORTH, titlePane);
            //Create the comment text
            final JTextPane commentPane = new JTextPane();
            commentPane.setFont(new Font(null, Font.PLAIN, 12));
            StyledDocument commentDoc = commentPane.getStyledDocument();
            commentDoc.insertString(commentDoc.getLength(), document.getComment(), null);
            this.add(BorderLayout.CENTER, commentPane);
            
            // The information is stored in the document each time the mouse leaves the panel
            MouseListener listener = new MouseAdapter() 
            {               
                public void mouseExited(MouseEvent e) 
                {
                    try
                    {
                        StyledDocument titleDoc = titlePane.getStyledDocument();
                        document.setName(titleDoc.getText(0, titleDoc.getLength()));
                        StyledDocument commentDoc = commentPane.getStyledDocument();
                        document.setComment(commentDoc.getText(0, commentDoc.getLength()));
                    }
                    catch (BadLocationException ex)
                    {
                        // This can't happen...
                        //JOptionPane.showMessageDialog(ide.getFrame(), "Bad comment in module.", "Bad comment", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            titlePane.addMouseListener(listener);
            commentPane.addMouseListener(listener);
            //getRootPane().addMouseListener(listener); //Won't work?
        }
        catch (BadLocationException ex)
        {
            System.err.println("Bad comment in module. " + ex);
        }
    }
}
