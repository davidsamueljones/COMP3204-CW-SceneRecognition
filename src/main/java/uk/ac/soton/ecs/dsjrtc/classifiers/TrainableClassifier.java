package uk.ac.soton.ecs.dsjrtc.classifiers;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.Classifier;

/**
 * Interface describing a classifier that can be trained. See {@link Classifier}.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 *
 * @param <C> Type of classes
 * @param <O> Type of objects/instances
 */
public interface TrainableClassifier<C, O> extends Classifier<C, O> {

  /**
   * Train the classifier with the provided training set.
   * 
   * @param training A set of instances grouped under their respective classes. 
   */
  void train(GroupedDataset<C, ListDataset<O>, O> trainingSet);

}
