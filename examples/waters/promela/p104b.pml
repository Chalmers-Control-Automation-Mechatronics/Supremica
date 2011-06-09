/*
 * A test for the Promela importer in Waters.
 * This tests whether a do-statement with break can be processed.
 */

#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }

proctype B() { ch!b }

proctype C()
{
  do
  :: ch?a
  :: ch?b -> break
  od
}

init { run A(); run B(); run C() }
