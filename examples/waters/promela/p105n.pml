/*
  * A test for the Promela importer in Waters.
  * In this model, different processes send and receive on two
  * channels. Messages need to be indexed by senders and receivers,
  * and careful care needs to be taken as to who sends and receives what.
  */

chan ab = [0] of { byte };
chan abc = [1] of { byte, byte };

proctype A()
{
  ab!0;
  ab!1;
  abc!2(1);
}

proctype B()
{
  byte val;
  ab?val;
  abc!2(2);
}

proctype C()
{
  byte val;
  do
  :: abc?2(val)
  :: abc?2(1); break
  od
}

init
{
  run A();
  run B();
  run B();
  run B();
  run C();
  run C()
}
