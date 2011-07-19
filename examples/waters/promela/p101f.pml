/*                                                                    
 * A test for the Promela importer in Waters.                         
 * This tests whether the compiler can recognise two different channels.
 */                                                                   

chan msg = [0] of { byte };
chan ack = [0] of { byte };

proctype producer()
{
  msg!5;
  ack?1;
}

proctype consumer()
{
  byte data;
  msg?data;
  ack!1;
}

init
{
  run producer();
  run consumer()
}
