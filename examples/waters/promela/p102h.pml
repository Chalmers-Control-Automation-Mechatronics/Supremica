/*
 * A test for the Promela importer in Waters.
 * This tests whether the compiler can handle a message
 * that is never received from an asynchronous channel.
 */

chan ch = [1] of { byte };

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
