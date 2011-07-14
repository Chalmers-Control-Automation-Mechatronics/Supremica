/*
 * A test for the Promela importer in Waters.
 * This model includes a simple channel of length 2.
 */

chan ch = [2] of { byte };

proctype A()
{
  ch!2;
  ch!4;
}
proctype B()
{
  byte state;
  ch?2;
  ch?state;
}

init
{
  run A();
  run B()
}
