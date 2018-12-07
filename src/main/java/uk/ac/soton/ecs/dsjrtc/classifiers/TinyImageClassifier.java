package uk.ac.soton.ecs.dsjrtc.classifiers;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.FImage;
import org.openimaj.ml.annotation.basic.KNNAnnotator;
import uk.ac.soton.ecs.dsjrtc.features.TinyImageFeatureExtractor;
import org.openimaj.feature.FloatFVComparison;

/**
 * Classifier that uses the tiny image feature vector with k-nearest neighbour classification.<br>
 * Must be trained before classification.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class TinyImageClassifier implements TrainableClassifier<String, FImage> {
  public static final int DEFAULT_NN_K = 20;
  public static final TinyImageFeatureExtractor DEFAULT_FE = new TinyImageFeatureExtractor();

  /** Class nearest neighbour classifier for current training set */
  private final KNNAnnotator<FImage, String, FloatFV> knn;

  /**
   * Instantiate a tiny image classifier using the class defaults.
   */
  public TinyImageClassifier() {
    this(DEFAULT_NN_K);
  }

  /**
   * Instantiate a tiny image classifier with the provided number of considered neighbours with the
   * class default tiny image feature extractor.
   * 
   * @param nNeighbours Number of neighbours to consider for classification
   */
  public TinyImageClassifier(int nNeighbours) {
    this(nNeighbours, DEFAULT_FE);
  }

  /**
   * Instantiate a tiny image classifier with the provided number of considered neighbours, and a
   * instantiated tiny image feature extractor.
   * 
   * @param nNeighbours Number of neighbours to consider for classification
   * @param fe Feature extractor to use for classification
   */
  public TinyImageClassifier(int nNeighbours, TinyImageFeatureExtractor fe) {
    if (fe == null) {
      throw new IllegalArgumentException("Feature extractor cannot be null");
    }
    this.knn = new KNNAnnotator<>(fe, FloatFVComparison.EUCLIDEAN, nNeighbours);
  }

  @Override
  public ClassificationResult<String> classify(FImage img) {
    if (knn.getAnnotations().isEmpty()) {
      throw new IllegalStateException("Classifier has not yet been trained");
    }
    return knn.classify(img);
  }

  @Override
  public void train(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    knn.train(trainingSet);
  }

  @Override
  public void retrain(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    knn.reset();
    train(trainingSet);
  }

}
