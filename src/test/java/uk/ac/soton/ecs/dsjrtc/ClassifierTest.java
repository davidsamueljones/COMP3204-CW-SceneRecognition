package uk.ac.soton.ecs.dsjrtc;

import java.awt.Dimension;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import uk.ac.soton.ecs.dsjrtc.classifiers.LinearBOVWClassifier;
import uk.ac.soton.ecs.dsjrtc.classifiers.RandomClassifier;
import uk.ac.soton.ecs.dsjrtc.classifiers.TinyImageClassifier;
import uk.ac.soton.ecs.dsjrtc.features.PatchesFeature;
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
    // ----------------------------------------
    // DATASET LOADING
    // ----------------------------------------
    
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
    
    // Get labelled data for both training and verification
    System.out.println("\n[Training]");
    int nTrain = 75;
    int nTest = 100 - nTrain;
    System.out.println(String.format(
        "Using %d samples for training, %d samples for testing for each class...", nTrain, nTest));
    // Split data into training and testing
    GroupedRandomSplitter<String, FImage> splitData =
        new GroupedRandomSplitter<>(dsTraining, nTrain, 0, nTest);
    GroupedDataset<String, ListDataset<FImage>, FImage> training = splitData.getTrainingDataset();
    GroupedDataset<String, ListDataset<FImage>, FImage> labeledTesting = splitData.getTestDataset();
    
    // ----------------------------------------
    // CLASSIFIER TESTING
    // ----------------------------------------
    System.out.println("\n[Testing RandomClassifier]");
    RandomClassifier rc = new RandomClassifier();
    rc.train(training);
    TestingUtilities.evaluateClassifier(rc, labeledTesting);
    TestingUtilities.classifyDataset(dsTesting, rc, true, "run0.txt");

    System.out.println("\n[Testing TinyImageClassifier]");
    TinyImageFeature tife = new TinyImageFeature(new Dimension(16, 16), true);
    TinyImageClassifier tic = new TinyImageClassifier(20, tife);
    tic.train(training);
    TestingUtilities.evaluateClassifier(tic, labeledTesting);
    TestingUtilities.classifyDataset(dsTesting, tic, true, "run1.txt");

    System.out.println("\n[Testing LinearBOVWClassifier]");
    PatchesFeature patchesFeature = new PatchesFeature();
    LinearBOVWClassifier lbc = new LinearBOVWClassifier(patchesFeature);
    lbc.train(training);
    TestingUtilities.evaluateClassifier(lbc, labeledTesting);
    TestingUtilities.classifyDataset(dsTesting, lbc, true, "run2.txt");
  }



}
