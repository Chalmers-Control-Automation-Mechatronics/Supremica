package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.subject.base.DocumentSubject;


/**
 * A panel with editable information about a document. The information is
 * divided into a header (the name of the document) and a body (the comment of
 * the document).
 */
class CommentPanel extends JPanel
{
  private static final long serialVersionUID = 1L;

  public CommentPanel(final DocumentSubject document)
  {
    setLayout(new BorderLayout());
    try {
      // Create title
      final Border titleBorder = BorderFactory.createLoweredBevelBorder();
      final JTextPane titlePane = new JTextPane();
      titlePane.setBorder(titleBorder);
      titlePane.setFont(new Font(null, Font.BOLD, 14));
      StyledDocument titleDoc = titlePane.getStyledDocument();
      titleDoc.insertString(titleDoc.getLength(), document.getName(), null);
      this.add(BorderLayout.NORTH, titlePane);
      // Create the comment text
      final Border commentBorder = BorderFactory.createLoweredBevelBorder();
      final JTextPane commentPane = new JTextPane();
      commentPane.setBorder(commentBorder);
      commentPane.setFont(new Font(null, Font.PLAIN, 12));
      StyledDocument commentDoc = commentPane.getStyledDocument();
      commentDoc.insertString
        (commentDoc.getLength(), document.getComment(), null);
      this.add(BorderLayout.CENTER, commentPane);
      // The information is stored in the document each time the mouse leaves
      // the panel.
      MouseListener listener = new MouseAdapter() {
        public void mouseExited(MouseEvent e)
        {
          try {
            StyledDocument titleDoc = titlePane.getStyledDocument();
            document.setName(titleDoc.getText(0, titleDoc.getLength()));
            StyledDocument commentDoc = commentPane.getStyledDocument();
            document.setComment(commentDoc.getText(0, commentDoc.getLength()));
          } catch (final BadLocationException exception) {
            throw new WatersRuntimeException(exception);
          }
        }
      };
      titlePane.addMouseListener(listener);
      commentPane.addMouseListener(listener);
    } catch (final BadLocationException exception) {
      throw new WatersRuntimeException(exception);
    }
  }
}
