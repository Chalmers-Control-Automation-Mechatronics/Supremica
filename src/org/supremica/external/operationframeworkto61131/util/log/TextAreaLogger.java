package org.supremica.external.operationframeworkto61131.util.log;
/**
 * @author LC
 *
 */
import javax.swing.JTextArea;

public class TextAreaLogger implements Logger {

	JTextArea textArea;

	public TextAreaLogger(JTextArea _textArea) {

		textArea = _textArea;
		// textArea.setLineWrap(true);

		textArea.setText("");
		textArea.setCaretPosition(textArea.getDocument().getLength());

	}

	public void log(String message) {

		textArea.append(message);
		textArea.append("\n");

		textArea.setCaretPosition(textArea.getDocument().getLength());

	}

}
