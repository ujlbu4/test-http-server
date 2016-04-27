#!/bin/bash -e

mkdir .package

mvn clean package

ln -s ../builded/bin .package/bin
ln -s ../builded/lib .package/lib