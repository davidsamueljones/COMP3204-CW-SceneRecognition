package uk.ac.soton.ecs.dsjrtc;

import org.openimaj.data.dataset.VFSGroupDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
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
  }



}
