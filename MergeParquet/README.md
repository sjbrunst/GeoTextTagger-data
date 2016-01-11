# MergeParquet #

This is a program to combine Parquet files.

Parquet files are saved in mulitple parts. They more efficient if the number of parts is kept low, so this program combines them to gain efficiency. Note, however, that if file parts are too large then there can be memory problems. I've seen problems begin when a file part reached about 200 MB in size.

For more documentation about the program (including run instructions), see the source code at src/main/scala/Merge.scala.
