# Data

This directory contains the raw data that was used for tests in the paper by Brunsting et al.

WikipediaArticles.txt contains the 5976 articles that were used in section 3.

TweetsByUser.txt contains the aggregated tweets for 1101 users that were used in section 4.2. 219 of these users received inconsistent tokenization by the NER and POS taggers (discussed in section 3.2), leaving 882 users for the reported results.

Tweets.txt contains the 18,720 individual tweets that were used for the last paragraph of section 4.2.

## Format

The data is in tab-separated text files. Each file has 5 columns in the following order:

1. Article id (for Wikipedia articles) or tweet id (for single tweets) or number of tweets (when tweets are concatenated by user)
2. Text of the article or tweet
3. The type of geotag (for Wikipedia articles) or "tweet" for tweets
4. Latitude
5. Longitude
