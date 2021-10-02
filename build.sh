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

# Copy freshly generated jar files
for file in $files; do
    cp $file bundle
done
sed "s/TEMPLATEVERSION/$version$modifier/g" pom.template.xml > bundle/$pom

# Check sha before stripping non-deterministic bits (mainly for debugging purposes)
sha256sum bundle/*

# Run "strip-nondeterminism" on the jar files, which will rewrite the jars
# with the goal to making it possible to create exactly the same binaries
# across different build environments.
for file in $files; do
    indir=bundle/$(basename $file)
    strip-nondeterminism -t jar -T $unixtstamp $indir
done

# Check sha after stripping non-deterministic bits (mainly for debugging purposes)
sha256sum bundle/*


# Will look into signing the files using GitHub Actions later...
#tosign=$(ls bundle)
#for file in $tosign; do
#    gpg --armor --detach-sign bundle/$file
#done

# GitHub Actions will zip the artifacts when downloading, so not needed to jar them...
#rm -f bundle.jar
#jar Mcvf bundle.jar -C bundle .

