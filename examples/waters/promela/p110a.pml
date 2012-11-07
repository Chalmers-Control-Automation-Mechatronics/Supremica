chan ch = [0] of { byte };

proctype A()
{
  ch!255;
}

proctype B()
{
  byte x;
  ch?x;
  x = x+1;
  x = x+1
}

init
{
  run A();
  run B()
}
