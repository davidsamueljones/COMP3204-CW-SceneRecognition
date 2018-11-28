package uk.ac.soton.ecs.dsjrtc;

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.ml.clustering.FloatCentroidsResult;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.ml.clustering.kmeans.FloatKMeans;
import org.openimaj.util.pair.IntFloatPair;
import uk.ac.soton.ecs.dsjrtc.features.TinyImage;
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
    System.out.println("Training: " + dsTraining.numInstances());
    System.out.println("Testing: " + dsTesting.numInstances());

    // As we only know values for the training set use this for both training and testing
    // !!! Hardcoded because I'm hard
    int nTrain = 100 / 4 * 3;
    int nTest = 100 - nTrain;
    System.out.println(String.format(
        "Using %d samples for training, %d samples for testing for each class...", nTrain, nTest));
    GroupedRandomSplitter<String, FImage> splitData =
        new GroupedRandomSplitter<>(dsTraining, nTrain, 0, nTest);
    GroupedDataset<String, ListDataset<FImage>, FImage> training = splitData.getTrainingDataset();
    GroupedDataset<String, ListDataset<FImage>, FImage> testing = splitData.getTestDataset();

    // Make some tiny images
    FloatKMeans cluster = FloatKMeans.createExact(training.size()*3);    
    Dimension d = new Dimension(4, 4);
    float[][] data = new float[training.numInstances()][d.width * d.height];
    int i = 0;
    for (Entry<String, ListDataset<FImage>> group : training.entrySet()) {
      for (final FImage img : group.getValue()) {
        data[i] = TinyImage.makeTinyImage(img, d);
        i++;
      }
    }
    System.out.println("Prepared data...");
    FloatCentroidsResult result = cluster.cluster(data);
    System.out.println("Clustered data...");
    System.out.println(result);
    HardAssigner<float[], float[], IntFloatPair> assigner = result.defaultHardAssigner();
    
    // Find what was assigned where
    System.out.println("Finding where data has been assigned...");
    Map<Integer, Map<String, Integer>> assignments = new HashMap<>();
    for (int c = 0; c < result.numClusters(); c++) {
      assignments.put(c, new HashMap<String, Integer>());
    }
    for (Entry<String, ListDataset<FImage>> group : training.entrySet()) {
      for (final FImage img : group.getValue()) {
        float[] imgTiny = TinyImage.makeTinyImage(img, d);
        int centroid = assigner.assign(imgTiny);
        Map<String, Integer> mappings = assignments.get(centroid);
        Integer groupCount = mappings.get(group.getKey());
        if (groupCount == null) {
          groupCount = 0;
        }
        mappings.put(group.getKey(), groupCount + 1);
      }
    }
    System.out.println("Generated assignments...");
    // Run the test data through
    System.out.println("Running test data through...");
    int correct = 0;
    int wrong = 0;
    for (Entry<String, ListDataset<FImage>> group : testing.entrySet()) {
      for (final FImage img : group.getValue()) {
        float[] imgTiny = TinyImage.makeTinyImage(img, d);
        int c = assigner.assign(imgTiny);
        Map<String, Integer> mappings = assignments.get(c);
        String bestGroup = null;
        int bestVal = 0;
        for (Entry<String, Integer> entry : mappings.entrySet()) {
          if (entry.getValue() > bestVal) {
            bestGroup = entry.getKey();
            bestVal = entry.getValue();
          }
        }
        if (bestGroup.equals(group.getKey())) {
          correct++;
        } else {
          wrong++;
        }
      }
    }
    System.out.println("Finished testing...");
    System.out.println("Correct: " + correct + "   Wrong: " + wrong);
    System.out.println("Finished!");
    
    for (int c = 0; c < result.numClusters(); c++) {
      Map<String, Integer> mappings = assignments.get(c);
      System.out.println(mappings);
    }
    
  }



}
