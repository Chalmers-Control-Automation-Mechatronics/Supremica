package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import net.sourceforge.waters.subject.base.DocumentSubject;

import org.supremica.log.*;

class CommentPanel
    extends JPanel
{
    private static Logger logger = LoggerFactory.createLogger(CommentPanel.class);
    
    /**
     * Displays a comment about the module.
     */
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
            FocusListener listener = new FocusListener() 
            {
                public void focusGained(FocusEvent e) 
                {
                }
                public void focusLost(FocusEvent e) 
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
            titlePane.addFocusListener(listener);
            commentPane.addFocusListener(listener);
            this.add(BorderLayout.CENTER, commentPane);
        }
        catch (BadLocationException ex)
        {
            // This can't happen...
            //JOptionPane.showMessageDialog(ide.getFrame(), "Bad comment in module.", "Bad comment", JOptionPane.ERROR_MESSAGE);
        }
    }
}
