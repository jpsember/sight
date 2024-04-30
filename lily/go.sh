#!/usr/bin/env bash
set -eu

RES=300


lilypond\
 --silent\
 --format=png\
 -dresolution=$RES\
 --output=out "$@"

