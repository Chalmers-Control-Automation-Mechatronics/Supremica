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

//Next step is to create class that will accept arbitrary boolean formula and will 
//feed cnf formula out of it to the lowest level.

Arbitrary formula to cnf works.

next step is to get boolean formula. Out of integer formula or out of automaton directly
I should look what automaton produces and then see what do i neet to implement it

