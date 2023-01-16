package com.onthegomap.planetiler.examples;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.FeatureMerge;
import com.onthegomap.planetiler.Planetiler;
import com.onthegomap.planetiler.Profile;
import com.onthegomap.planetiler.VectorTile;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import com.onthegomap.planetiler.util.ZoomFunction;

import java.nio.file.Path;
import java.util.List;
import java.util.Arrays;

public class PlanetilerForestSimplification implements Profile {

  @Override
  public List<OsmRelationInfo> preprocessOsmRelation(OsmElement.Relation relation) {
    return null;
  }


  @Override
  public void processFeature(SourceFeature sourceFeature, FeatureCollector features) {

    // wood layer
    if (sourceFeature.canBePolygon() && (
        sourceFeature.hasTag("landuse", "forest") ||
        sourceFeature.hasTag("natural", "wood")
    )) {
      features.polygon("wood")
        .setMinZoom(0);
    }
  }

  @Override
  public List<VectorTile.Feature> postProcessLayerFeatures(String layer, int zoom,
    List<VectorTile.Feature> items) {

    try {
      return FeatureMerge.mergeNearbyPolygons(items,
        128, // minArea
        128, // minHoleArea
        1, // minDist
        1 // buffer/unbuffer
      );
    }
    catch (GeometryException e) {
      return null;
    }
  }

  @Override
  public String name() {
    return "PlanetilerForestSimplification";
  }

  @Override
  public String description() {
    return "Simplify forest polygons with planetiler";
  }

  @Override
  public String attribution() {
    return "<a href=\"https://www.openstreetmap.org/copyright\" target=\"_blank\">&copy; OpenStreetMap contributors</a>";
  }

  public static void main(String[] args) throws Exception {
    run(Arguments.fromArgsOrConfigFile(args));
  }

  static void run(Arguments args) throws Exception {
    String area = args.getString("area", "geofabrik area to download", "monaco");
    Planetiler.create(args)
      .setProfile(new PlanetilerForestSimplification())
      .addOsmSource("osm", Path.of("data", "sources", area + ".osm.pbf"), "planet".equals(area) ? "aws:latest" : ("geofabrik:" + area))
      .overwriteOutput("mbtiles", Path.of("data", "planetiler-forest-simplification.mbtiles"))
      .run();
  }
}