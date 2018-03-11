# Problem 2. (ICE) Sorted File Merge. #

----------

**Task:**  
**G**iven two sorted files, write Java program to merge them to preserve sort order. DO NOT assume either of these 
files can fit in memory.


**T**his program accepts following parameters:

java -jar target\sortedfilemerge-1.0-SNAPSHOT-jar-with-dependencies.jar 
target_file.txt source_file1.txt source_file_2.txt source_file_3.txt ... 

**Example:**  
java -jar target\sortedfilemerge-1.0-SNAPSHOT-jar-with-dependencies.jar 
e:\Home\file12.txt 
E:\Home\sortedfilemerge\src\test\resources\file1.txt E:\Home\sortedfilemerge\src\test\resources\file2.txt

Notes:  
This task is basically one of the operations of the external sort using merge sort algorithm. This is a step that 
will merge 2 files.