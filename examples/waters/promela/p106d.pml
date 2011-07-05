/*
 * A test for the Promela importer in Waters.
 * This tests whether a labels with a name starting in "end" produce
 * marked states.
 */

chan ch = [0] of { byte };

proctype A()
{
endA:
  do
  :: ch!1
  :: ch!2
  :: ch!3
  :: break
  od
}

proctype B()
{
restart:
  ch?1;
end:
  ch?2;
  ch?3;
end1:
  if
  :: ch?1; goto restart
  :: ch?2
  fi
}

init
{
end1:
  run A();
  run B()
}
