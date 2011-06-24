/*
 * A test for the Promela importer in Waters.
 * This tests whether a loop inside an if-statement can be converted.
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
  if
  :: do
     :: ch?a; ch?b
     :: ch?e; break
     od
  :: ch?b
  :: ch?e
  fi;
}

init { run A(); run B() }
