/*
 * A test for the Promela importer in Waters.
 * This tests whether skip statements are recognised.
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
  :: ch?a;
  :: ch?b; break
  :: ch?e; goto exit
  od;
  ch?a;
exit:
  skip
}

init { run A(); run B() }
