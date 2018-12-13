package uk.ac.soton.ecs.dsjrtc.run2;

import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.sampling.Sampler;
import org.openimaj.experiment.dataset.sampling.StratifiedGroupedUniformRandomisedSampler;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.SparseIntFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.feature.local.data.LocalFeatureListDataSource;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.aggregate.BagOfVisualWords;
import org.openimaj.image.feature.local.aggregate.BlockSpatialAggregator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator;
import org.openimaj.ml.annotation.linear.LiblinearAnnotator.Mode;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.util.pair.IntFloatPair;
import de.bwaldvogel.liblinear.SolverType;
import uk.ac.soton.ecs.dsjrtc.lib.Debugger;
import uk.ac.soton.ecs.dsjrtc.lib.TrainableClassifier;

/**
 * Classifier that uses a linear classifier for annotation, with features extracted from a bag of
 * visual words (vocabulary) generated through k-means with the base feature of image patches.<br>
 * 
 * Must be trained before classification and the vocabulary can only be generated on first train.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class LinearBOVWClassifier implements TrainableClassifier<String, FImage> {
  // Base feature extractor
  public static final PatchesFeature DEFAULT_PATCHES_FEATURE = new PatchesFeature();
  // Vocabulary modifiers
  private static final float VOCAB_IMAGE_PERCENT = 0.2f;
  private static final float VOCAB_IMAGE_FEATURE_PERCENTAGE = 1.0f;
  private static final int K_MEANS_CLUSTERS = 500;
  // Aggregator modifiers
  private static final int EXTRACTOR_BLOCKS_X = 2;
  private static final int EXTRACTOR_BLOCKS_Y = 2;
  // Annotator modifiers
  private static final double ANNOTATOR_C_PARAM = 0.95;
  private static final double ANNOTATOR_EPS = 0.00001;

  
  /** Classifier instance, that gets set during first training (and reset during retraining) */
  private LiblinearAnnotator<FImage, String> annotator = null;
  
  private PatchesFeature patchesFeature;

  /**
   * Instantiate the classifier with all class defaults.
   */
  public LinearBOVWClassifier() {
    this(DEFAULT_PATCHES_FEATURE);
  }

  /**
   * Instantiate the classifier with a custom patches feature.
   * 
   * @param patchesFeature Patches feature extractor to use
   */
  public LinearBOVWClassifier(PatchesFeature patchesFeature) {
    if (patchesFeature == null) {
      throw new IllegalArgumentException("Patches feature extractor cannot be null");
    }
    this.patchesFeature = patchesFeature;
  }

  @Override
  public ClassificationResult<String> classify(FImage object) {
    if (annotator == null) {
      throw new IllegalStateException("Classifier has not yet been trained");
    }
    return annotator.classify(object);
  }

  /**
   * {@inheritDoc} N.B. Will only train the annotator after first train, vocabulary will not be
   * expanded.
   */
  @Override
  public void train(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    if (annotator == null) {
      // Create the vocabulary through clustered patch features (use a sample of the training set)
      Debugger.println("Making vocabulary...");
      Sampler<GroupedDataset<String, ListDataset<FImage>, FImage>> sampler =
          new StratifiedGroupedUniformRandomisedSampler<>(VOCAB_IMAGE_PERCENT);
      final HardAssigner<float[], float[], IntFloatPair> vocab =
          getVocabulary(sampler.sample(trainingSet), patchesFeature);
      // Group the features using a BOVW extractor
      Debugger.println("Making extractor...");
      final FeatureExtractor<SparseIntFV, FImage> extractor =
          new BOVWExtractor(patchesFeature, vocab);
      // Create a linear classifier for one vs many using mode suitable for classes that can only
      // belong to one class
      Debugger.println("Making annotator...");
      annotator = new LiblinearAnnotator<>(extractor, Mode.MULTICLASS, SolverType.L2R_L2LOSS_SVC,
          ANNOTATOR_C_PARAM, ANNOTATOR_EPS);
    }
    // Train the classifier
    Debugger.println("Training started...");
    annotator.train(trainingSet);
    Debugger.println("Training finished");
  }

  @Override
  public void retrain(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    train(trainingSet);
  }

  /**
   * Get a set of features and cluster them to create a vocabulary for a bag of visual words.
   * 
   * @param dataset Dataset to get features from
   * @param fe The patches feature extractor to use
   * @return An assigner for the trained cluster
   */
  private static HardAssigner<float[], float[], IntFloatPair> getVocabulary(Dataset<FImage> dataset,
      PatchesFeature fe) {

    // Find image features (patches)
    LocalFeatureList<LocalFeature<SpatialLocation, FloatFV>> features =
        new MemoryLocalFeatureList<>();
    Debugger.println("Extracting features...");
    int totalFeatures = 0;
    for (FImage img : dataset) {
      final LocalFeatureList<LocalFeature<SpatialLocation, FloatFV>> localFeatures =
          fe.extractFeature(img);
      totalFeatures += localFeatures.size();
      // Get a reduced random feature list
      final int featureCount = (int) (localFeatures.size() * VOCAB_IMAGE_FEATURE_PERCENTAGE);
      features.addAll(localFeatures.randomSubList(featureCount));
    }
    Debugger
        .println(String.format("Found %d features, using %d...", totalFeatures, features.size()));

    // Cluster using k-means
    Debugger.println(String.format("Clustering features with %d centroids...", K_MEANS_CLUSTERS));
    FloatKMeans km = FloatKMeans.createExact(K_MEANS_CLUSTERS);
    DataSource<float[]> datasource = new LocalFeatureListDataSource<>(features);
    FloatCentroidsResult result = km.cluster(datasource);
    Debugger.println("Clustering complete...");

    return result.defaultHardAssigner();
  }

  /**
   * Features extractor that uses featuresd from a a spatial pooled bag of visual words. 
   * 
   * @author David Jones (dsj1n15@soton.ac.uk)
   * @author Richard Crosland (rtc1g16@soton.ac.uk)
   */
  class BOVWExtractor implements FeatureExtractor<SparseIntFV, FImage> {
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

}
