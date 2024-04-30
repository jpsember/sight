#!/usr/bin/env bash
set -eu

lilypond --silent --format=png -dresolution=600 --output=out "$@"

