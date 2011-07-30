/*
 * A test for the Promela importer in Waters.
 * This tests whether the compiler can suppress a message
 * on a synchronous channel that is never sent. 
 */

chan ch = [0] of { byte };

proctype A()
{
  ch!2
}

proctype B()
{
  ch?2;
  ch?1
}

init
{
  run A();
  run B()
}
