package uk.ac.soton.ecs.dsjrtc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.dataset.split.GroupedRandomSplitter;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import uk.ac.soton.ecs.dsjrtc.classifiers.TinyImageClassifier;
import uk.ac.soton.ecs.dsjrtc.lib.Debugger;
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


    TinyImageClassifier tic = new TinyImageClassifier();
    tic.train(training);
    int correct = 0;
    int incorrect = 0;
    for (Entry<String, ListDataset<FImage>> group : testing.entrySet()) {
      for (FImage img : group.getValue()) {
        ClassificationResult<String> result = tic.classify(img);
        Pair<String, Double> predicted = getClassification(result);
        if (group.getKey().equals(predicted.getKey())) {
          correct++;
        } else {
          incorrect++;
        }
        Debugger.println(String.format("A: %15s   P: %15s  C: %.02f", group.getKey(),
            predicted.getKey(), predicted.getValue()));
      }
    }
    System.out.println(String.format("Correct: %d,  Incorrect: %d", correct, incorrect));
  }

  private static Pair<String, Double> getClassification(
      ClassificationResult<String> classification) {
    List<Pair<String, Double>> predictions = getPredictions(classification);
    if (!predictions.isEmpty()) {
      return predictions.get(0);
    }
    return null;
  }

  private static List<Pair<String, Double>> getPredictions(
      ClassificationResult<String> classification) {
    List<Pair<String, Double>> predictions = new ArrayList<>();
    for (String group : classification.getPredictedClasses()) {
      predictions.add(new ImmutablePair<>(group, classification.getConfidence(group)));
    }

    Collections.sort(predictions, new Comparator<Pair<String, Double>>() {
      @Override
      public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    });
    return predictions;
  }



}
