/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether an enumeration type is recognised and converted
 * in combination with a synchronous channel.
 */

mtype = { D0, D1, FV }

chan ch = [0] of { mtype };

proctype A()
{
  do
  :: ch!D0
  :: ch!D1
  :: ch!FV ; break
  od
}

proctype B()
{
  mtype data;
  ch?data;
}

init
{
  run A();
  run B()
}
