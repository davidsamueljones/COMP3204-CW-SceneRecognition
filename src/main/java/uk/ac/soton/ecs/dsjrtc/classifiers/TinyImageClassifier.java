package uk.ac.soton.ecs.dsjrtc.classifiers;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.BasicClassificationResult;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.knn.FloatNearestNeighboursExact;
import org.openimaj.util.pair.IntFloatPair;
import uk.ac.soton.ecs.dsjrtc.features.TinyImageFeatureExtractor;

/**
 * Classifier that uses the tiny image feature vector with k-means classification.<br>
 * Must be trained before classification.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class TinyImageClassifier implements TrainableClassifier<String, FImage> {
  public static final int DEFAULT_NN_K = 20;
  public static final boolean DEFAULT_NORMALISE = TinyImageFeatureExtractor.DEFAULT_NORMALISE;
  public static final Dimension DEFAULT_SCALE = TinyImageFeatureExtractor.DEFAULT_SCALE;

  /** Feature extractor used by classifier */
  private final FeatureExtractor<FloatFV, FImage> fe;
  /** Number of neighbours considered during classification */
  private final int nNeighbours;

  /** Class nearest neighbour classifier for current training set */
  private FloatNearestNeighboursExact knn;
  /** Group for each training point */
  private String[] assignments;

  /**
   * Instantiate a tiny image classifier using all class defaults.
   */
  public TinyImageClassifier() {
    this(DEFAULT_NN_K);
  }

  /**
   * Instantiate a tiny image classifier that uses with the number of considered neighbours set, and
   * others as class defaults.
   * 
   * @param nNeighbours Number of neighbours to consider for classification
   */
  public TinyImageClassifier(int nNeighbours) {
    this(nNeighbours, DEFAULT_SCALE, DEFAULT_NORMALISE);
  }

  /**
   * Instantiate a tiny image classifier with all settings user set.
   * 
   * @param nNeighbours Number of neighbours to consider for classification
   * @param scale Scaling to use in tiny image feature extraction
   * @param normalise Whether normalisation should be used in tiny image feature extraction
   */
  public TinyImageClassifier(int nNeighbours, Dimension scale, boolean normalise) {
    this.fe = new TinyImageFeatureExtractor(scale, normalise);
    this.nNeighbours = nNeighbours;
    this.knn = null;
  }

  @Override
  public ClassificationResult<String> classify(FImage img) {
    if (knn == null) {
      throw new IllegalStateException("Classifier has not yet been trained");
    }

    // Find the nearest neighbours of the feature vector
    float[] feature = fe.extractFeature(img).values;
    List<IntFloatPair> neighbours = knn.searchKNN(feature, nNeighbours);
    // Collate the nearest neighbours into their groups
    Map<String, Integer> results = new HashMap<>();
    int foundNeighbours = 0;
    for (IntFloatPair neighbour : neighbours) {
      String group = assignments[neighbour.first];
      Integer groupCount = results.get(group);
      if (groupCount == null) {
        groupCount = 0;
      }
      results.put(group, groupCount + 1);
      foundNeighbours++;
      // !!! TODO: Should we have a distance threshold that ignores far away neighbours?
    }

    // Collate results as classifications with certainty equivalent to the percentage of points that
    // were of the found group out of all found neighbours
    BasicClassificationResult<String> classification = new BasicClassificationResult<>();
    for (Entry<String, Integer> entry : results.entrySet()) {
      classification.put(entry.getKey(), entry.getValue() / (double) foundNeighbours);
    }

    return classification;
  }

  @Override
  public void train(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    // Instantiate new assignment structure for this new training set
    assignments = new String[trainingSet.numInstances()];

    // Get features from each image in training set, recording its known assignment
    final float[][] data = new float[trainingSet.numInstances()][];
    int i = 0;
    for (Entry<String, ListDataset<FImage>> group : trainingSet.entrySet()) {
      for (final FImage img : group.getValue()) {
        data[i] = fe.extractFeature(img).values;
        assignments[i] = group.getKey();
        i++;
      }
    }
    // Train the model with all features
    knn = new FloatNearestNeighboursExact(data);
  }

}
