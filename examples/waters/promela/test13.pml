/*
  * A test for the Promela importer in Waters.
  * In this model, different processes send and receive on two
  * channels. Messages need to be indexed by senders and receivers,
  * and careful care needs to be taken as to who sends and receives what.
  */

chan ab = [0] of { byte };
chan abc = [0] of { byte };

proctype A()
{
  ab!0;
  ab!1;
  abc!2;
}

proctype B()
{
  byte val;
  ab?val;
  abc!2;
}

proctype C()
{
  do
  :: abc?2
  :: break
  od
}

init
{
  run A();
  run B();
  run C()
}
