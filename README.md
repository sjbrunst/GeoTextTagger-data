# GeoTextTagger-data
Data collection and formatting tools for the research paper by Brunsting et al.

The data that was used for tests in the paper is included in the Data directory.

The rest of this documentation describes the tools that were used to obtain and format this data. Final output from the formatting is a tab separated text file.

Note that many of the instuctions assume you are working in a Linux environment.

## Obtaining Twitter Data

This was done using the StreamFromTwitter program. See that folder for more information. Output from this program can be consolidated with the MergeParquet program.

## Formatting Twitter Data

To create a tab-separated file where each line represents a single tweet, use the CreateTweetText program (see that folder for more details).

To create a tab-separated file where each line represents the concatenation of all tweets from a single user, use the CreateTweetTextByUser program (see that folder for more details).

## Obtaining Wikipedia Data

Dumps of the English Wikipedia are available at http://dumps.wikimedia.org/enwiki.

The files you need are:

* enwiki-\*-pages-articles.xml.bz2
* enwiki-\*-geo\_tags.sql.gz

Where \* can be replaced with "latest" or the date of the data dump.

On Linux, you can download the files used in our paper with:

1. `wget http://dumps.wikimedia.org/enwiki/20150602/enwiki-20150602-pages-articles.xml.bz2` (download 11 GB file with all articles)
2. `wget http://dumps.wikimedia.org/enwiki/20150602/enwiki-20150602-geo_tags.sql.gz` (download 27 MB file with all geo tags)
3. `gunzip enwiki-20150602-geo_tags.sql.gz` (unzip the geo tags file)

## Formatting Wikipedia Data

This involves a number of steps, and assumes you have downloaded dumps of Wikipedia articles and geotags (see previous section).

### Article Extraction

The first step is to extract the articles from your pages-articles.xml.bz2 file. We did this using software by Giuseppe Attardi. The version we used has been included in this repository (WikiExtractor.py), but the most recent version can be found here: https://github.com/attardi/wikiextractor

There are a number of command line arguments to Attardi's program. We ran the program with `python WikiExtractor.py -b 10M -o extracted enwiki-20150602-pages-articles.xml`

Note that due to the large number of articles in Wikipedia, this program may take a few days time to complete.

### Reformatting Articles

To format the articles using Spark, we must have one article per line. The output of the Article Extraction step above does not meet this requirement, but we can fix that with the following commands:

1. `perl -p -i -e 's/\n/ /g' extracted/*/*` (replace all newlines with spaces)
2. `perl -p -i -e 's/<\/doc> /<\/doc>\n/g' extracted/*/*` (place a newline at the end of each article)
3. `cat extracted/*/* > extracted/all.txt` (place all articles into one file for convenience)

Now we are ready to join the articles with the geotags and create a tab-separated text file. This is done using the CreateWikiText program. See that folder for more information.
