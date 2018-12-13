package uk.ac.soton.ecs.dsjrtc.run3;

import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.image.FImage;
import uk.ac.soton.ecs.dsjrtc.lib.TrainableClassifier;

public class TensorFlowCNNWrapper  implements TrainableClassifier<String, FImage> {

  private boolean augmentTrainingData;
  
  
  @Override
  public ClassificationResult<String> classify(FImage object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void train(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    // Apply augmentation

  }

  @Override
  public void retrain(GroupedDataset<String, ListDataset<FImage>, FImage> trainingSet) {
    // TODO Auto-generated method stub
    
  }

}
