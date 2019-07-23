package net.sourceforge.waters.analysis.options;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public abstract class AbstractTextFieldParameter extends Parameter
{

  public AbstractTextFieldParameter(final int id, final String name, final String description)
  {
    super(id, name, description);
  }

  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final JTextField textField = new JTextField();
    final PlainDocument doc = (PlainDocument) textField.getDocument();
    doc.setDocumentFilter(new InputFilter());
    textField.setColumns(10);
    return textField;
  }

  protected abstract boolean testAlphabet(final String text);

  private class InputFilter extends DocumentFilter {

    private boolean test(final String text) {
      return testAlphabet(text);
    }

    @Override
    public void replace(final FilterBypass fb, final int offset, final int length, final String text,
          final AttributeSet attrs) throws BadLocationException {

       final Document doc = fb.getDocument();
       final StringBuilder sb = new StringBuilder();
       sb.append(doc.getText(0, doc.getLength()));
       sb.replace(offset, offset + length, text);

       if (test(sb.toString())) {
          super.replace(fb, offset, length, text, attrs);
       } else {
          // warn the user and don't allow the insert
         //System.out.println("Bad Replace " + text);
       }
    }
    /*
    @Override
    public void remove(final FilterBypass fb, final int offset, final int length)
          throws BadLocationException {
       final Document doc = fb.getDocument();
       final StringBuilder sb = new StringBuilder();
       sb.append(doc.getText(0, doc.getLength()));
       sb.delete(offset, offset + length);

       if (test(sb.toString())) {
          super.remove(fb, offset, length);
       } else {
          // warn the user and don't allow the insert
         System.out.println("3");
       }
    }*/
 }
}
