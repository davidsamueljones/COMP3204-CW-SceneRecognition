package uk.ac.soton.ecs.dsjrtc.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.BasicClassificationResult;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.image.FImage;

/**
 * Classifier that just picks a random class from those it was trained on.<br>
 * Optimised for classification speed vs training speed. Must be trained before classification.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class RandomClassifier implements TrainableClassifier<String, FImage> {

  /** Set of possible groups */
  private final List<String> groups;

  /**
   * Initialise the random classifier.
   */
  public RandomClassifier() {
    this.groups = new ArrayList<>();
  }

  @Override
  public ClassificationResult<String> classify(FImage object) {
    if (groups.isEmpty()) {
      throw new IllegalStateException("Classifier has not yet been trained");
    }
    // Pick a random group from the trained list 
    final Random r = new Random();
    final BasicClassificationResult<String> result = new BasicClassificationResult<>();
    result.put(groups.get(r.nextInt(groups.size())), 1);
    return result;
  }

  @Override
  public void train(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    // Clone the existing list in a structure that only contains uniques and add any new groups
    Set<String> uniqueGroups = new HashSet<>(groups);
    uniqueGroups.addAll(trainingSet.getGroups());
    // Set group list to new set of uniques
    groups.clear();
    groups.addAll(uniqueGroups);
  }

  @Override
  public void retrain(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    groups.clear();
    train(trainingSet);
  }

}
