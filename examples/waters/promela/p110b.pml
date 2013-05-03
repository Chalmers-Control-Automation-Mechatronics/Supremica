chan ch = [0] of { byte };

proctype A()
{
  byte x;
  ch!2;
  ch?x
}

proctype B()
{
  byte y;
  ch?y;
  if
  :: y==2 ; ch!2
  :: y!=2
  fi
}

init
{
  run A();
  run B()
}
