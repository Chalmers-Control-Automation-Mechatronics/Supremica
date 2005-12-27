#!/bin/bash

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime TestBigComputation_par_10.sys tests:event:service 10 >result_10.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime TestBigComputation_par_20.sys tests:event:service 20 >result_20.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime TestBigComputation_par_30.sys tests:event:service 30 >result_30.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime TestBigComputation_par_40.sys tests:event:service 40 >result_40.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime TestBigComputation_par_50.sys tests:event:service 50 >result_50.txt

time /usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime TestBigComputation_par_60.sys tests:event:service 60 >result_60.txt