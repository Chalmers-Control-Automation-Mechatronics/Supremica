/*                                                                    
 * A test for the Promela importer in Waters.                         
 * This tests whether the compiler can handle a two-byte channel
 * and receive values in different variables.
 */                                                                   

chan ch = [0] of { byte, byte };

proctype A()
{
  ch!1(3);
  ch!1(4);
  ch!2(0)
}

proctype B()
{
  show byte key;
  show byte data;
  ch?key(data);
}

init
{
  run A();
  run B()
}
