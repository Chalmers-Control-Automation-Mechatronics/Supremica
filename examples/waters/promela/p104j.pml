/*
 * A test for the Promela importer in Waters.
 * This tests whether skip statements at the beginning of a loop
 * are compiled correctly.
 */

#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }

proctype B() { ch!b }

proctype C()
{
  do
  :: skip ; ch?a
  :: ch?b ; break
  od;
}

init { run A(); run B(); run C() }
