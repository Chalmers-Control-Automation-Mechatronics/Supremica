/*
 * A test for the Promela importer in Waters.
 * This model includes a simple channel of length 3.
 */

chan ch = [3] of { byte };

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
