/*
 * A test for the Promela importer in Waters.
 * This tests whether a goto-statement inside a do-statement can be processed.
 */

#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }
proctype B() { ch!b }
proctype C()
{	
L1:	do
	:: ch?a -> goto L1
	:: ch?b -> goto L2
	od;
L2:	ch?a
}
init { run A(); run B(); run C() }
