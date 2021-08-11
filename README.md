# spella

Multi-language, Multi word, utf-8 spelling correction server for e.g. search
engines using a levenshtein automaton and a Trie. Written in kotlin.

* Is capable of splitting words, joining words, correcting phrases, etc
* Calculates distances according to damerau-levenshtein and scores multi
  character transliterations (german umlauts) with a distance of one.
* Applies a max edit distance per word.
* Uses several optimizations like re-using trie nodes when correcting
  phrases to achieve single digit millisecond response times most of the
  time.
* Applies multiple rules for choosing the best correction, including a
  user supplied score.

## Compilation

```shell
./gradlew shadowJar
```

## Start

```shell
./spella --help
./spella data/*.dic
```

The `.dic` files are user-supplied tab separated text files:

```
en  some phrase  3942
en  keyword  3491
...
```

containing three columns per line. First column is an arbitrary language
identifier, the second column is the keyword or phrase and the third column is
a frequency or score value. If there are multiple matches with the same
distance, the one with a higher score wins. Correction is done greedily, i.e.
phrases are generally preferred.

## Requests

The server listens on port 8888 and uses a simple JSON based protocol.

Request:

curl -X GET http://127.0.0.1:8888/corrections?language=en&text=some+phrse

Response:

```json
{
  "text": "some phrase",
  "distance": 1,
  "took": 6
}
```

`took` tells you how long the response took.

## Choosing Corrections

The criteria for choosing the best correction are:

1. the number of words (higher is better)
2. distance (smaller is better)
3. whether or not a correction matches the original when transliterated
4. the user supplied score (higher is better)

## Splitting Dictionary Phrases

It is possible to split all phrases present in the dictionary files with the
goal to have all single words available for corrections as well. You can
enable/disable that using the `--split/--no-split` command line option.
Splitting is enabled by default.

## Max Edit Distance

Currently, the defaults max allowed edit distances are:

* token length < 4 characters: won't be corrected
* token length < 9 characters: a maximum edit distance of 1 is used
* else: a maximum edit distance of 2 is used

But you can change those using the `--distances` command line option
and pass a comma separated list of string lenghts. For instance,
`--distance 3,6,9` means

* token length < 3 characters: won't be corrected
* token length < 6 characters: a maximum edit distance of 1 is used
* token length < 9 characters: a maximum edit distance of 2 is used
* else a maximum edit distance of 3 is used

It is strongly recommended to have an overall maximum edit distance of 2 for
performance reasons.

## Todo

* normalization/ignoring special characters
* keeping the case while being case insensitive
