/*
 * A test for the Promela importer in Waters.
 * This tests whether a break-statement on its own is translated into a step.
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
  :: ch?b
  :: break
  od
}

init { run A(); run B(); run C() }
