/*                                                                    
 * A test for the Promela importer in Waters.                         
 * This tests whether the compiler can handle a three-byte channel and
 * match different combinations of constants and variables when receiving.
 */                                                                   

chan ch = [1] of { byte, byte, byte };

proctype A()
{
  ch!1(2,3);
  ch!1(2,4);
  ch!2(2,3)
}

proctype B()
{
  byte x, y, z;
  ch?1(2,z);
  ch?x(2,z);
  ch?2(y,z);
}

init
{
  run A();
  run B()
}
