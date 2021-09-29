#!/bin/bash

cd chariot

prev=$(git describe --tags --abbrev=0 2> /dev/null || echo "v0.0.0")
curr=$(git describe --tags --always)

if [ "$prev" == "$curr" ]; then
    version=$curr
    modifier=""
else
    next=$(echo $prev | awk -F . '{ print $1 "." $2 "." $3+1 }')
    version=$next
    modifier="-SNAPSHOT"
fi
version=${version#"v"}
rev=$(git rev-parse --short HEAD)
unixtstamp=$(git log -1 --format=%at)

echo "rev:       [$rev]"
echo "version:   [$version]"
echo "modifier:  [$modifier]"
echo "unixstamp: [$unixtstamp]"

java -Xinternalversion
java build/Build.java module=chariot version="$version$modifier"

cd -

rm -rf bundle
mkdir -p bundle

basename="chariot-$version$modifier"
pom="$basename.pom"
files="chariot/out/modules/$basename.jar chariot/out/$basename-sources.jar chariot/out/$basename-javadoc.jar"

for file in $files; do
    cp $file bundle
    indir=bundle/$(basename $file)
    strip-nondeterminism -t jar -T $unixtstamp $indir
done
sed "s/TEMPLATEVERSION/$version$modifier/g" pom.template.xml > bundle/$pom

# Will look into signing the files using GitHub Actions later...
#tosign=$(ls bundle)
#for file in $tosign; do
#    gpg --armor --detach-sign bundle/$file
#done

# GitHub Actions will zip the artifacts when downloading, so not needed to jar them...
#rm -f bundle.jar
#jar Mcvf bundle.jar -C bundle .

