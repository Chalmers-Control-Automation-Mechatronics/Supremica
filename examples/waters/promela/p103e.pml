/*
 * A test for the Promela importer in Waters.
 * This tests whether a skip statement inside an if statement can be compiled.
 */

#define a 1
#define b 2

chan ch = [0] of { byte };

proctype A() { ch!a }
proctype B() { ch!b }

proctype C()
{	
  if
  :: ch?a
  :: skip
  fi;
  ch?b
}

init { run A(); run B(); run C() }
