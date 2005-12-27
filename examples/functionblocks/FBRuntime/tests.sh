#!/bin/bash

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 10 -lp tests:event:service TestBigComputation_par_10.sys >result_10.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 20 -lp tests:event:service TestBigComputation_par_20.sys >result_20.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 30 -lp tests:event:service TestBigComputation_par_30.sys >result_30.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 40 -lp tests:event:service TestBigComputation_par_40.sys >result_40.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 50 -lp tests:event:service TestBigComputation_par_50.sys >result_50.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 60 -lp tests:event:service TestBigComputation_par_60.sys >result_60.txt
