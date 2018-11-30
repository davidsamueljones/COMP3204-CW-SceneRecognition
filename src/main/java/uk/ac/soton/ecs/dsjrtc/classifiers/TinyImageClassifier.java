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

public class TinyImageClassifier implements TrainableClassifier<String, FImage> {
  public static final int DEFAULT_NN_K = 20;
  public static final boolean DEFAULT_NORMALISE = true;
  public static final Dimension DEFAULT_SCALE = TinyImageFeatureExtractor.DEFAULT_SCALE;

  private FloatNearestNeighboursExact knn;
  private int[] assignments;
  private String[] groups;

  /** Feature extractor */
  private final Dimension scale;
  private final FeatureExtractor<FloatFV, FImage> fe;
  private final int nNeighbours;

  public TinyImageClassifier() {
    this(DEFAULT_SCALE, DEFAULT_NORMALISE, DEFAULT_NN_K);
  }

  public TinyImageClassifier(Dimension scale, boolean normalise, int nNeighbours) {
    this.scale = scale;
    this.fe = new TinyImageFeatureExtractor(scale, normalise);
    this.nNeighbours = nNeighbours;
  }

  @Override
  public ClassificationResult<String> classify(FImage img) {
    float[] feature = fe.extractFeature(img).values;
    List<IntFloatPair> neighbours = knn.searchKNN(feature, nNeighbours);
    Map<Integer, Integer> results = new HashMap<>();
    int foundNeighbours = 0;
    for (IntFloatPair neighbour : neighbours) {
      int group = assignments[neighbour.first];
      Integer groupCount = results.get(group);
      if (groupCount == null) {
        groupCount = 0;
      }
      results.put(group, groupCount + 1);
      foundNeighbours++;
      // !!! TODO: Should we have a distance threshold that ignores far away neighbours?
    }

    // Check results
    BasicClassificationResult<String> classification = new BasicClassificationResult<>();
    for (Entry<Integer, Integer> entry : results.entrySet()) {
      classification.put(groups[entry.getKey()], entry.getValue() / (double) foundNeighbours);
    }

    return classification;
  }

  @Override
  public void train(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    // Instantiate assignments structures
    groups = new String[trainingSet.size()];
    assignments = new int[trainingSet.numInstances()];

    // Get features from each image in training set, recording its known assignment
    final float[][] data = new float[trainingSet.numInstances()][];
    int gi = 0, ii = 0;
    for (Entry<String, ListDataset<FImage>> group : trainingSet.entrySet()) {
      groups[gi] = group.getKey();
      for (final FImage img : group.getValue()) {
        data[ii] = fe.extractFeature(img).values;
        assignments[ii] = gi;
        ii++;
      }
      gi++;
    }
    // Train the model
    knn = new FloatNearestNeighboursExact(data);
  }

}
