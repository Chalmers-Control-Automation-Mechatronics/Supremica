/*
 * A test for the Promela importer in Waters.
 * This tests whether the compiler can suppress the sending of a message
 * on a synchronous channel that is never received. 
 */

chan ch = [0] of { byte };

proctype A()
{
  ch!1;
  ch!2
}

proctype B()
{
  ch?1
}

init
{
  run A();
  run B()
}
