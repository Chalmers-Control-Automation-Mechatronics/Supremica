import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

public class InterfaceExample
{
    public void createAutomaton()
    {
		Automata theAutomata = new Automata();
		Automaton theAutomaton = new Automaton("theAutomaton");
		theAutomaton.setType(AutomatonType.PLANT);
		theAutomata.addAutomaton(theAutomaton);

		// Get alphabet
		Alphabet theAlphabet = theAutomaton.getAlphabet();

		// Create events
		LabeledEvent e1 = new LabeledEvent("e1");
		e1.setControllable(false);
		LabeledEvent e2 = new LabeledEvent("e2");

		// Add events to the alphabet
		theAlphabet.addEvent(e1);
		theAlphabet.addEvent(e2);

		// Create states
		State q1 = new State("q1");
		q1.setInitial(true);
		State q2 = new State("q2");
		q2.setAccepting(true);
		q2.setCost(3);

		// Add states to the automaton
		theAutomaton.addState(q1);
		theAutomaton.addState(q2);

		// Add transitions
		theAutomaton.addArc(new Arc(q1, q2, e1));
		theAutomaton.addArc(new Arc(q2, q1, e2));

		// Output the automata as XML
		AutomataToXml serializer = new AutomataToXml(theAutomata);

		serializer.serialize(new PrintWriter(new OutputStreamWriter(System.out)));
    }

    void projectBuilder(File file)
    {
        ProjectBuildFromXml builder = new ProjectBuildFromXml();
        try
        {
            Project theProject = builder.build(file);
        }
        catch (Exception ex)
        {
            System.err.println(ex);
            return;
        }
    }

	public static void main(String[] args)
		throws Exception
	{
        InterfaceExample example = new InterfaceExample();
        example.createAutomaton();
        example.projectBuilder(new File(args[0]));

	}
}