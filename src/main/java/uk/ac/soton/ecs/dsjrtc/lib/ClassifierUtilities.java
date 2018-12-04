package uk.ac.soton.ecs.dsjrtc.lib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;

/**
 * Utilities directly applicable to classification.
 *
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class ClassifierUtilities {

  /**
   * Get the single strongest prediction from a classification.
   * 
   * @param result Grouped result from classification
   * @return Immutable pair with left the classification and right the classification strength.<br>
   *         null if no classification is found.
   */
  public static Pair<String, Double> getClassification(ClassificationResult<String> result) {
    // Get the sorted list of predictions
    List<Pair<String, Double>> predictions = getPredictions(result);
    // Get the highest confidence prediction if any predictions exist
    if (!predictions.isEmpty()) {
      return predictions.get(0);
    }
    return null;
  }

  /**
   * Get a list of predictions from a classification.
   * 
   * @param result Grouped result from classification
   * @return List of immutable pairs with classifications in the left and classification strengths
   *         in the right. Sorted in descending order by the classifications strength.
   */
  public static List<Pair<String, Double>> getPredictions(ClassificationResult<String> result) {
    // Create a list of predictions from classification results
    List<Pair<String, Double>> predictions = new ArrayList<>();
    for (String group : result.getPredictedClasses()) {
      predictions.add(new ImmutablePair<>(group, result.getConfidence(group)));
    }

    // Sort into descending order of classification confidence
    Collections.sort(predictions, new Comparator<Pair<String, Double>>() {
      @Override
      public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
        return o2.getValue().compareTo(o1.getValue());
      }
    });

    return predictions;
  }

}
