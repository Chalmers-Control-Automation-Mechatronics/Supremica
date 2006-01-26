#!/bin/bash

/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_1.sys >result_1_1_ser_1.txt
echo result_1_1_ser_1.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_ser_10.sys >result_1_1_ser_10.txt
echo result_1_1_ser_10.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_ser_20.sys >result_1_1_ser_20.txt
echo result_1_1_ser_20.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_ser_30.sys >result_1_1_ser_30.txt
echo result_1_1_ser_30.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_ser_40.sys >result_1_1_ser_40.txt
echo result_1_1_ser_40.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_ser_50.sys >result_1_1_ser_50.txt
echo result_1_1_ser_50.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_ser_60.sys >result_1_1_ser_60.txt
echo result_1_1_ser_60.txt


/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_1.sys >result_1_1_par_1.txt
echo result_1_1_par_1.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_par_10.sys >result_1_1_par_10.txt
echo result_1_1_par_10.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_par_20.sys >result_1_1_par_20.txt
echo result_1_1_par_20.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_par_30.sys >result_1_1_par_30.txt
echo result_1_1_par_30.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_par_40.sys >result_1_1_par_40.txt
echo result_1_1_par_40.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_par_50.sys >result_1_1_par_50.txt
echo result_1_1_par_50.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_par_60.sys >result_1_1_par_60.txt
echo result_1_1_par_60.txt


/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_1.sys >result_fb_fb_ser_1.txt
echo result_fb_fb_ser_1.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 10 -lp tests:event:service TestBigComputation_ser_10.sys >result_fb_fb_ser_10.txt
echo result_fb_fb_ser_10.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 20 -lp tests:event:service TestBigComputation_ser_20.sys >result_fb_fb_ser_20.txt
echo result_fb_fb_ser_20.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 30 -lp tests:event:service TestBigComputation_ser_30.sys >result_fb_fb_ser_30.txt
echo result_fb_fb_ser_30.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 40 -lp tests:event:service TestBigComputation_ser_40.sys >result_fb_fb_ser_40.txt
echo result_fb_fb_ser_40.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 50 -lp tests:event:service TestBigComputation_ser_50.sys >result_fb_fb_ser_50.txt
echo result_fb_fb_ser_50.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 60 -lp tests:event:service TestBigComputation_ser_60.sys >result_fb_fb_ser_60.txt
echo result_fb_fb_ser_60.txt


/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 1 -lp tests:event:service TestBigComputation_1.sys >result_fb_fb_par_1.txt
echo result_fb_fb_par_1.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 10 -lp tests:event:service TestBigComputation_par_10.sys >result_fb_fb_par_10.txt
echo result_fb_fb_par_10.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 20 -lp tests:event:service TestBigComputation_par_20.sys >result_fb_fb_par_20.txt
echo result_fb_fb_par_20.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 30 -lp tests:event:service TestBigComputation_par_30.sys >result_fb_fb_par_30.txt
echo result_fb_fb_par_30.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 40 -lp tests:event:service TestBigComputation_par_40.sys >result_fb_fb_par_40.txt
echo result_fb_fb_par_40.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 50 -lp tests:event:service TestBigComputation_par_50.sys >result_fb_fb_par_50.txt
echo result_fb_fb_par_50.txt
/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 60 -lp tests:event:service TestBigComputation_par_60.sys >result_fb_fb_par_60.txt
echo result_fb_fb_par_60.txt

/usr/lib/java/bin/java -classpath /home/cengic/devel/workspace/Supremica/build:/home/cengic/devel/workspace/Supremica/dist/SupremicaLib.jar org.supremica.functionblocks.model.FBRuntime -t 30 -lp tests:event:service TestBigComputation_par_60.sys >result_30_30_par_60.txt
echo result_30_30_par_60.txt
