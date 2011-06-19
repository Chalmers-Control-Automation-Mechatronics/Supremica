/*
 * A test for the Promela importer in Waters.
 * This tests whether an if-statement inside a loop can be converted.
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
  :: if
     :: ch?a; ch?a
     :: ch?b; break
     fi;
     ch?a
  :: ch?e; break
  od;
}

init { run A(); run B() }
