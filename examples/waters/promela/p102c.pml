/*                                                                    
 * A test for the Promela importer in Waters.                         
 * This tests whether the compiler can handle a three-byte channel.
 */                                                                   

chan ch = [1] of { byte, byte, byte };

proctype A()
{
  ch!1(2,3);
  ch!1(2,4);
  ch!2(3,3)
}

proctype B()
{
  byte x, y, z;
  ch?x(y,z);
}

init
{
  run A();
  run B()
}
