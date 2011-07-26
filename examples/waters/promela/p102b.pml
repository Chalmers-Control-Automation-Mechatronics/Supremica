/*                                                                    
 * A test for the Promela importer in Waters.                         
 * This tests whether the compiler can handle a two-byte channel,
 * matching variables to different data components when receiving.
 */                                                                   

chan ch = [1] of { byte, byte };

proctype A()
{
  ch!1(3);
  ch!1(4);
  ch!2(4)
}

proctype B()
{
  byte x;
  ch?1(x);
  ch?x(4);
}

init
{
  run A();
  run B()
}
