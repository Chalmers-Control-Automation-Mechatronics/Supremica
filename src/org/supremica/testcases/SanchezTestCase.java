


package org.supremica.testcases;

import org.supremica.automata.*;



public class SanchezTestCase extends Automata {
	private Project project;
	private int blocks;

	public SanchezTestCase(int blocks, int type)
	{
		this.blocks = blocks;

		project =  new Project();

		if(type == 0) {
			for (int i = 0; i < blocks; i++) createAsyncProduct(i);
		} else if(type == 1) {
			for (int i = 0; i < blocks; i++) createExactSyncProduct(i);
		} else if(type == 2) {
			TransferLine tl = new TransferLine(blocks, 1,1, true);
			project = tl.getProject();
		} else {
			System.err.println("Unknown benchmark!");
		}
	}


	// --[ benchmark # 1] ---------------------------------------------------

	private void createAsyncProduct(int n) {
		LabeledEvent e11 = new LabeledEvent("11"+n);
		LabeledEvent e12 = new LabeledEvent("12"+n);
		LabeledEvent e21 = new LabeledEvent("21"+n);
		LabeledEvent e22 = new LabeledEvent("22"+n);
		LabeledEvent e31 = new LabeledEvent("31"+n);
		LabeledEvent e32 = new LabeledEvent("32"+n);
		LabeledEvent e33 = new LabeledEvent("33"+n);
		LabeledEvent e34 = new LabeledEvent("34"+n);

		e21.setControllable(false);
		e22.setControllable(false);
		e31.setControllable(false);
		e32.setControllable(false);
		e33.setControllable(false);
		e34.setControllable(false);

		Automaton a1 = new Automaton("V" +n );
		Automaton a2 = new Automaton("B" +n );
		Automaton a3 = new Automaton("PS" +n );

		a1.getAlphabet().addEvent( e11);
		a1.getAlphabet().addEvent( e12);

		a2.getAlphabet().addEvent( e21);
		a2.getAlphabet().addEvent( e22);

		a3.getAlphabet().addEvent( e31);
		a3.getAlphabet().addEvent( e32);
		a3.getAlphabet().addEvent( e33);
		a3.getAlphabet().addEvent( e34);


		State s00 = new State("open");
		State s01 = new State("closed");
		s01.setInitial(true);
		a1.addState(s00);
		a1.addState(s01);
		a1.addArc(new Arc(s00, s01, e12));
		a1.addArc(new Arc(s01, s00, e11));


		State s10 = new State("on");
		State s11 = new State("off");
		s11.setInitial(true);
		a2.addState(s11);
		a2.addState(s10);
		a2.addArc(new Arc(s11, s10, e21));
		a2.addArc(new Arc(s10, s11, e22));




		State s20 = new State("low");
		State s21 = new State("ok");
		State s22 = new State("high");

		s20.setInitial(true);
		a3.addState(s20);
		a3.addState(s21);
		a3.addState(s22);
		a3.addArc(new Arc(s20,s21,e33));
		a3.addArc(new Arc(s21,s22,e34));
		a3.addArc(new Arc(s22,s21,e31));
		a3.addArc(new Arc(s21,s20,e32));


		a1.setType(AutomatonType.Plant);
		a3.setType(AutomatonType.Plant);
		a2.setType(AutomatonType.Plant);
		project.addAutomaton(a1);
		project.addAutomaton(a2);
		project.addAutomaton(a3);
	}

	// --[ benchmark # 2] ---------------------------------------------------
	private void createExactSyncProduct(int n) {
		LabeledEvent e11 = new LabeledEvent("11"+n);
		LabeledEvent e12 = new LabeledEvent("12"+n);
		LabeledEvent e13 = new LabeledEvent("13"+n);
		LabeledEvent e14 = new LabeledEvent("14"+n);

		Automaton m1 = new Automaton("M1" +n );
		Automaton m2 = new Automaton("M2" +n );

		m1.getAlphabet().addEvent( e11);
		m1.getAlphabet().addEvent( e12);
		m1.getAlphabet().addEvent( e13);
		m1.getAlphabet().addEvent( e14);


		m2.getAlphabet().addEvent( e11);
		m2.getAlphabet().addEvent( e12);
		m2.getAlphabet().addEvent( e13);
		m2.getAlphabet().addEvent( e14);


		State s10 = new State("10");
		State s11 = new State("11");
		State s12 = new State("12");
		State s13 = new State("13");
		s10.setInitial(true);
		m1.addState(s10);
		m1.addState(s11);
		m1.addState(s12);
		m1.addState(s13);


		m1.addArc(new Arc(s10,s12,e11) );
		m1.addArc(new Arc(s12,s10,e12) );

		m1.addArc(new Arc(s12,s13,e13) );
		m1.addArc(new Arc(s13,s12,e14) );

		m1.addArc(new Arc(s13,s11,e12) );
		m1.addArc(new Arc(s11,s13,e11) );

		m1.addArc(new Arc(s11,s10,e14) );
		m1.addArc(new Arc(s10,s11,e13) );



		State s20 = new State("20");
		State s21 = new State("21");
		s20.setInitial(true);
		m2.addState(s20);
		m2.addState(s21);



		m2.addArc(new Arc(s20,s21,e11) );
		m2.addArc(new Arc(s21,s20,e12) );

		m2.addArc(new Arc(s20,s20,e12) );
		m2.addArc(new Arc(s20,s20,e13) );
		m2.addArc(new Arc(s20,s20,e14) );


		m1.setType(AutomatonType.Plant);
		m2.setType(AutomatonType.Plant);
		project.addAutomaton(m1);
		project.addAutomaton(m2);
	}

	// ----------------------------------------------------------------------
	public Project getProject()
	{
		return project;
	}
}