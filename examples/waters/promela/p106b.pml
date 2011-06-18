/*
 * A test for the Promela importer in Waters.
 * This tests whether a goto-statement inside a if-statement can be processed.
 */

#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }
proctype B() { ch!b }
proctype C()
{	
L1:	if
	:: ch?a -> goto L1
	:: ch?b 
	fi;
L2:	ch?a
}
init { run A(); run B(); run C() }
