The packages javadoc-umented are given in the file package-list

To regenerate output for the listed packages you can do
	javadoc -d <out-dir> @package-list

An new package-list will be written, I assuem this is no problem (reading and writing "same" file) but you never know...

Note: javadoc deos not recurse through subdir (why? because sun-java people are rock stupid!)
To recursively generate javadoc for directory structures, you can do the following (on Windows):

Run recursive (/s) dir command only listing directory (/ad) names (/b) output (>) to file (dir.txt)
	dir /ad /s /b > dir.txt

Edit (Textpad!) dir.txt; 
	sort
	remove everything before "org.supremica" 
	remove all lines that include ".svn" or "CVS".
	replace \ with .

Save the file

Now run javadoc with the saved file as parameter
	javadoc -d <out-dir> @dir.txt
