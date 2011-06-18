/*
 * A test for the Promela importer in Waters.
 * This tests whether a loop with statements before and after can be handled.
 */

#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }

proctype B() { ch!b }

proctype C()
{
  ch?a;
  do
  :: ch?a
  :: ch?b
  :: break
  od;
  ch?b
}

init { run A(); run B(); run C() }
