# Makefile for AdlinkPCI7432 programs

CFLAGS := -Wall -Wstrict-prototypes -O2 -I../../../include
                                                     
7432test: 7432test.o conio.o
	gcc -o 7432test -lpci_dask 7432test.o conio.o

7432test.o: 7432test.c
	gcc -c $(CFLAGS) 7432test.c

conio.o: conio.c
	gcc -c $(CFLAGS) conio.c

AdlinkPCI7432: AdlinkPCI7432.c 
	gcc $(CFLAGS) -c AdlinkPCI7432.c -I/usr/java/jdk1.4/include/ -I/usr/java/jdk1.4/include/linux
	gcc -shared $(CFLAGS) -o libAdlinkPCI7432.so -fPIC AdlinkPCI7432.o

clean:
	rm -f *.o *~
