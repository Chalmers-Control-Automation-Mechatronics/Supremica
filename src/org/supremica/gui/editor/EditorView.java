package org.supremica.gui.editor;

// import com.nwoods.jgo.JGoSelection;
import org.supremica.gui.editor.AutomataEditor;
import org.supremica.gui.editor.AutomatonView;
import org.supremica.gui.VisualProject;

//import org.supremica.gui.editor.AutomatonDocument;
public interface EditorView
{
	public AutomataEditor getAutomataEditor();

	public AutomatonView getCurrentAutomatonView();

	public VisualProject getVisualProject();

	// public void createFrame(AutomatonDocument theDocument);
}
