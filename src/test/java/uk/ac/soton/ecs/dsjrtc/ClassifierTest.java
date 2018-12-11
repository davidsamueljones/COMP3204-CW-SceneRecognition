package uk.ac.soton.ecs.dsjrtc;

import java.awt.Dimension;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.Classifier;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import uk.ac.soton.ecs.dsjrtc.classifiers.RandomClassifier;
import uk.ac.soton.ecs.dsjrtc.classifiers.TinyImageClassifier;
import uk.ac.soton.ecs.dsjrtc.features.TinyImageFeature;
import uk.ac.soton.ecs.dsjrtc.lib.TestingUtilities;

/**
 * Test application for image classification.
 *
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class ClassifierTest {

  /**
   * Main method for test.
   * 
   * @param args None (ignored)
   */
  public static void main(String[] args) {
    // Load the training and testing datasets
    final String trainingPath = TestingUtilities.getResourcePath("training.zip");
    final String testingPath = TestingUtilities.getResourcePath("testing.zip");
    if (trainingPath == null || testingPath == null) {
      return;
    }
    VFSGroupDataset<FImage> dsTraining = null;
    VFSListDataset<FImage> dsTesting = null;
    try {
      dsTraining = new VFSGroupDataset<FImage>("zip:" + trainingPath, ImageUtilities.FIMAGE_READER);
      dsTesting = new VFSListDataset<FImage>("zip:" + testingPath, ImageUtilities.FIMAGE_READER);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dsTraining == null || dsTesting == null) {
        return;
      }
    }

    // After this point both datasets must be loaded
    System.out.println("[Datasets]");
    System.out.println("Training (known classes): " + dsTraining.numInstances());
    System.out.println("Testing (unknown classes): " + dsTesting.numInstances());

    System.out.println("\n[Training]");
    // As we only know values for the training set use this for both training and testing
    int nTrain = 100 / 4 * 3;
    int nTest = 100 - nTrain;
    System.out.println(String.format(
        "Using %d samples for training, %d samples for testing for each class...", nTrain, nTest));
    // Split data into training and testing
    GroupedRandomSplitter<String, FImage> splitData =
        new GroupedRandomSplitter<>(dsTraining, nTrain, 0, nTest);
    GroupedDataset<String, ListDataset<FImage>, FImage> training = splitData.getTrainingDataset();
    GroupedDataset<String, ListDataset<FImage>, FImage> testing = splitData.getTestDataset();

    System.out.println("\n[Testing RandomClassifier]");
    RandomClassifier rc = new RandomClassifier();
    rc.train(training);
    testClassifier(rc, testing);

    System.out.println("\n[Testing TinyImageClassifier]");
    TinyImageFeature tife = new TinyImageFeature(new Dimension(16, 16), true);
    TinyImageClassifier tic = new TinyImageClassifier(20, tife);
    tic.train(training);
    testClassifier(tic, training);
    
  }

  private static void testClassifier(Classifier<String, FImage> classifier,
      GroupedDataset<String, ListDataset<FImage>, FImage> testset) {
    CMAnalyser<FImage, String> analyser = new CMAnalyser<>(CMAnalyser.Strategy.SINGLE);
    ClassificationEvaluator<CMResult<String>, String, FImage> evaluator =
        new ClassificationEvaluator<>(classifier, testset, analyser);
    CMResult<String> result = evaluator.analyse(evaluator.evaluate());
    System.out.println(result.getDetailReport());
  }

}
