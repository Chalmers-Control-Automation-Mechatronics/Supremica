/*
 * A test for the Promela importer in Waters.
 * This tests whether two nested loops can be converted.
 */

chan ch = [0] of { byte };

proctype A()
{
  do
  :: ch!1
  :: ch!2
  :: ch!3
  :: ch!4
  :: break
  od

}

proctype B()
{
  do
  :: do
     :: ch?1
     :: ch?2
     :: ch?3; break
     od;
  :: ch?4; break
  od;
}

init { run A(); run B() }
