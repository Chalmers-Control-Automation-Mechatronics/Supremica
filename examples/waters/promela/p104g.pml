/*
 * A test for the Promela importer in Waters.
 * This tests whether two nested loops can be converted.
 */

#define a 1
#define b 2
#define e 3

chan ch = [0] of { byte };

proctype A()
{
  do
  :: ch!a
  :: ch!b
  :: ch!e; break
  od
}

proctype B()
{
  do
  :: do
     :: ch?a; ch?b
     :: ch?b; break
     od;
     ch?a
  :: ch?e; break
  od;
}

init { run A(); run B() }
