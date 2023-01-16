#!/usr/bin/env bash
set -e
./mvnw clean package --file standalone.pom.xml
java -cp target/*-with-deps.jar com.onthegomap.planetiler.examples.PlanetilerForestSimplification --download --area=switzerland
pmtiles convert data/planetiler-forest-simplification.mbtiles data/planetiler-forest-simplification.pmtiles
npx serve .
