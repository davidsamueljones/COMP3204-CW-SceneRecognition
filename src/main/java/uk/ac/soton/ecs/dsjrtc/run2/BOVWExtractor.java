package uk.ac.soton.ecs.dsjrtc.run2;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.pair.IntFloatPair;

/**
 * Features extractor that uses featuresd from a a spatial pooled bag of visual words. 
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class BOVWExtractor implements FeatureExtractor<SparseIntFV, FImage> {
  private static final int EXTRACTOR_BLOCKS_X = 2;
  private static final int EXTRACTOR_BLOCKS_Y = 2;
  
  private final PatchesFeature fe;
  private final BlockSpatialAggregator<float[], SparseIntFV> aggregator;

  /**
   * Instantiate a bag of visual words extractor for the given vocabulary, wrapping the created
   * extractor in a block aggregator for spatial pooling.
   * 
   * @param fe Feature extractor used by vocabulary
   * @param vocab Vocabularly in the form of an assigner
   */
  public BOVWExtractor(PatchesFeature fe, HardAssigner<float[], float[], IntFloatPair> vocab) {
    this.fe = fe;
    // Create an bag of visual words from the vocabularly
    final BagOfVisualWords<float[]> bovw = new BagOfVisualWords<>(vocab);
    // Wrap BOVW inside a block aggregator to improve accuracy
    aggregator = new BlockSpatialAggregator<float[], SparseIntFV>(bovw, EXTRACTOR_BLOCKS_X,
        EXTRACTOR_BLOCKS_Y);
  }

  @Override
  public SparseIntFV extractFeature(FImage img) {
    LocalFeatureList<LocalFeature<SpatialLocation, FloatFV>> feature = fe.extractFeature(img);
    return aggregator.aggregate(feature, img.getBounds());
  }

}
