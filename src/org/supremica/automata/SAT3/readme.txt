we need a chain.

Automata
Arb formula with integers 
Arbitrary formula with booleans
CNF formula: Dimacs file / Charged satj4

But more than datatypes we need converters. 
Look at the last datatype - it has completely different internal structure.
What we can have is only unified interface for creation of this datastructure.
Ok, that's enough. We will start with it.

Decision: 
CNF clause is a collection of (signed) integers.
Each integer represents literal. Positive or Negative. Zero never used.

lowest level done. now it should be tested. junit don't work for some reason...

Next step is to create class that will accept arbitrary boolean formula and will 
feed cnf formula out of it to the lowest level.

Arbitrary formula to cnf works. In two ways now.

next step is to get boolean formula. Out of integer formula or out of automaton directly
I should look what automaton produces and then see what do i neet to implement it
I think I would prefer strait conversion to boolean by means of the function, 
withoiut intermediate storage of integer in form of some datatype.

/*****
  make Expr immutable and add ExprMutable extends Expr
  regression test
  test VariablesLayout
  write AtomataToFormulaMSR (or fire marking event FME) - this one will get acceptor of formulas and layouter. layouter will give formulas, and this class will combine them with ExprFactory and feed to acceptor
  test with print (similar to Variables Layout tests)
  test with sat-solver
  write AutomataToFormulaCV
  test print (sanity check)
  test solver
  find out how GUI works
  implement GUI

  ok, immutable Expr requires much more thinking. Abstract Collection and Visitor pattern is a way to go I think.
****/
 
OK, there is a class to return boolean expression for what we need to 
represent automata: State-eq-some-particular-state, transition-eq-some-label,
state-eq-state-next-step and only-one-event-at-one-step. This four things are
required, and they work right now. Encoding can be linear or binary, class for
doing that should be passed to this one.

Next stage is to write FireMarkingEvent 

