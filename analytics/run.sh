#!/bin/bash
one="$1"
shift
if [ -x "$(command -v parallel)" ]; then
    seq "$one" | parallel --bar -n0 java -jar Rodnelps.jar "$@" >> results/"$*".txt
else
    i=1
    while [ $i -le "$one" ]; do
        java -jar Rodnelps.jar "$@" >> results/"$*".txt
        i=$((i + 1))
    done
fi
