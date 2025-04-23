#!/bin/bash

dirs=("models" "business" "repositories" "controllers" "config" "utils" "dto" "exceptions")

for str in ${dirs[@]}; do
  mkdir $str
done
