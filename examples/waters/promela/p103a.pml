/*
 * A test for the Promela importer in Waters.
 * This tests whether an elementary if-statement can be processed.
 */

#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }
proctype B() { ch!b }
proctype C()
{	if
	:: ch?a
	:: ch?b
	fi
}
init { run A(); run B(); run C() }
