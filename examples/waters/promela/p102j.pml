/*
 * A test for the Promela importer in Waters.
 * This tests whether the compiler can handle an asynchronous channel
 * containing both messages only sent and only received.
 */

chan ch = [1] of { byte };

proctype A()
{
  ch!2;
  ch!3
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
