package uk.ac.soton.ecs.dsjrtc.classifiers;

import java.time.LocalDateTime;
import org.joda.time.DateTime;
import org.openimaj.data.DataSource;
import org.openimaj.data.dataset.Dataset;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.feature.DoubleFV;
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
import uk.ac.soton.ecs.dsjrtc.features.PatchesFeature;
import uk.ac.soton.ecs.dsjrtc.lib.Debugger;

/**
 * Classifier that TODO...<br>
 * Must be trained before classification.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class LinearBOVWClassifier implements TrainableClassifier<String, FImage> {
  private static final PatchesFeature DEFAULT_PATCHES_FEATURE = new PatchesFeature();
  private static final float VOCAB_IMAGE_PERCENTAGE = 0.01f;
  private static final int K_MEANS_CLUSTERS = 500;
  private static final int EXTRACTOR_BLOCKS_X = 2;
  private static final int EXTRACTOR_BLOCKS_Y = 2;
  
  private LiblinearAnnotator<FImage, String> annotator = null;
  private PatchesFeature patchesFeature;

  /**
   * TODO
   */
  public LinearBOVWClassifier() {
    this(DEFAULT_PATCHES_FEATURE);
  }
  
  /**
   * TODO
   * 
   * @param patchesFeature
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
   * Reset the classifier if trained and train with the provided training set.
   */
  @Override
  public void train(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    // Create the vocabulary through clustered patch features
    final HardAssigner<float[], float[], IntFloatPair> vocab = getVocabulary(trainingSet, patchesFeature);
    Debugger.println("Making extractor...");
    final FeatureExtractor<DoubleFV, FImage> extractor = new BOVWExtractor(patchesFeature, vocab);
    Debugger.println("Making annotator...");
    annotator = new LiblinearAnnotator<>(extractor, Mode.MULTILABEL, SolverType.L2R_L2LOSS_SVC, 1.0, 0.00001);
    Debugger.println("Training started at: " + LocalDateTime.now() + "...");
    annotator.train(trainingSet);
    Debugger.println("Trained at: " + LocalDateTime.now());
  }

  /**
   * {@inheritDoc} <br>
   * Equivalent to train()
   */
  @Override
  public void retrain(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    train(trainingSet);
  }

  /**
   * TODO DODO TODO
   * 
   * @param trainingSet
   * @param fe
   * @return
   */
  private static HardAssigner<float[], float[], IntFloatPair> getVocabulary(
      Dataset<FImage> trainingSet, PatchesFeature fe) {

    // Find image features (patches)
    LocalFeatureList<LocalFeature<SpatialLocation, FloatFV>> features =
        new MemoryLocalFeatureList<>();
    Debugger.println("Extracting features...");
    for (FImage img : trainingSet) {
      features.addAll(fe.extractFeature(img));
    }
    // Get a reduced random feature list
    final int featureCount = (int) (features.size() * VOCAB_IMAGE_PERCENTAGE);
    Debugger.println(String.format("Found %d features, using %d...", features.size(), featureCount));
    features = new MemoryLocalFeatureList<>(features.randomSubList(featureCount));
    // Cluster using k-means
    Debugger.println(String.format("Clustering features with %d centroids [%s]...", K_MEANS_CLUSTERS, DateTime.now()));
    FloatKMeans km = FloatKMeans.createKDTreeEnsemble(K_MEANS_CLUSTERS);
    DataSource<float[]> datasource = new LocalFeatureListDataSource<>(features);
    FloatCentroidsResult result = km.cluster(datasource);
    Debugger.println("Clustering complete...");

    return result.defaultHardAssigner();
  }


  /**
   * Classifier that TODO...<br>
   * Must be trained before classification.
   * 
   * @author David Jones (dsj1n15@soton.ac.uk)
   * @author Richard Crosland (rtc1g16@soton.ac.uk)
   */
  class BOVWExtractor implements FeatureExtractor<DoubleFV, FImage> {
    private final PatchesFeature fe;
    private final BlockSpatialAggregator<float[], SparseIntFV> aggregator;

    /**
     * TODO
     * 
     * @param fe
     * @param vocab
     */
    public BOVWExtractor(PatchesFeature fe, HardAssigner<float[], float[], IntFloatPair> vocab) {
      this.fe = fe;
      // Create an aggregator for the vocabulary
      BagOfVisualWords<float[]> bovw = new BagOfVisualWords<>(vocab);
      aggregator = new BlockSpatialAggregator<float[], SparseIntFV>(bovw, EXTRACTOR_BLOCKS_X, EXTRACTOR_BLOCKS_Y);
    }

    @Override
    public DoubleFV extractFeature(FImage img) {
      LocalFeatureList<LocalFeature<SpatialLocation, FloatFV>> feature = fe.extractFeature(img);
      SparseIntFV res = aggregator.aggregate(feature, img.getBounds());
      return res.normaliseFV();
    }

  }

}
