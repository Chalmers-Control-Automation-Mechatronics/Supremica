
#XMLRPCClient.py
#A class to communicate with a server XML-RPC for import/export of automata
#Author: Matteo Cantarelli
#E-mail: matteo.cantarelli@lurpa.ens-cachan.fr
#Release: 0.2.6

import xmlrpclib
import xml.sax.saxutils
import urllib
import sys
from optparse import OptionParser

DECODE={
         '&quot;':'\"',     #quote
         '&#10;':'',        #carriage return
         '&#13;':'\n',      #newline
         '&#9;':'\t',       #tabulation
         '&#47;':'/',       #slash
        }

DEFAULT_SERVER='http://127.0.0.1'   #the default server, Supremica is running on local
DEFAULT_PORT='9112'                 #the default port for Supremica is the 9112

class CXMLRPCClient(object):

    def __init__(self,url=DEFAULT_SERVER+':'+DEFAULT_PORT):
        self.__server=xmlrpclib.Server(url)

    def uploadAutomata(self,filename):
        f=open(filename, 'r')
        self.__server.addAutomata(str(f.read()))
        f.close()

    def downloadAutomata(self,filename):
        f=open(filename,'w')

        v=[]
        for x in self.__server.getAutomataIdentities():
            v.append(x.strip('\''))     #add the automaton's name after having stripped the quotes
        f.write(xml.sax.saxutils.unescape(self.__server.getAutomata(v),DECODE))
        f.close()

    def downloadAutomaton(self,automaton,filename):
        f=open(filename,'w')
        f.write(xml.sax.saxutils.unescape(self.__server.getAutomaton(automaton.strip('\'')),DECODE))
        f.close()

    def listAutomata(self):
        v=[]
        for x in self.__server.getAutomataIdentities():
            v.append(x.strip('\''))     #add the automaton's name after having stripped the quotes
#            v.append(x.strip('\''))     #add the automaton's name after having stripped the quotes
        print v

    def synchronizeAutomata(self):
        f=open(filename, 'r')
        self.__server.addAutomata(str(f.read()))
        f.close()

def main():
   parser = OptionParser()
   parser.add_option("-q", "--quiet", action="store_false", dest="verbose", default=True, help="don't print status messages to stdout")
   parser.add_option("-f", "--inputfile", action="store_true", dest="inputfile", help="list the automata currently loaded in Supremica")
   parser.add_option("-o", "--operation", type="string", action="store", dest="operation",  help="Select operation [synchronization]")
   parser.add_option("-l", "--list", action="store_true", dest="listAutomata", help="list the automata currently loaded in Supremica")

   parser.set_defaults(inputfile="")
   parser.set_defaults(operation="synchronize")



   (options, args) = parser.parse_args()

   client = CXMLRPCClient()

   if options = "listAutomata":
      client.listAutomata()
       



if __name__ == "__main__":
    main()

