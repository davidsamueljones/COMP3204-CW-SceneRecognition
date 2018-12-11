package uk.ac.soton.ecs.dsjrtc.features;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.LocalFeatureImpl;
import org.openimaj.feature.local.SpatialLocation;
import org.openimaj.image.FImage;
import org.openimaj.image.pixel.sampling.RectangleSampler;
import org.openimaj.math.geometry.shape.Rectangle;
import uk.ac.soton.ecs.dsjrtc.lib.FeatureUtilities;

/**
 * Patches generator class that directly implements the feature extractor class for generic
 * classifiers. <br>
 * 
 * Static feature generation can be achieved using the:
 * {@link #getPatches(FImage img, int stepX, int stepY, Dimension window)} method.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class PatchesFeature
    implements FeatureExtractor<List<LocalFeature<SpatialLocation, FloatFV>>, FImage> {
  public static int DEFAULT_STEP_X = 4;
  public static int DEFAULT_STEP_Y = 4;
  public static Dimension DEFAULT_WINDOW = new Dimension(8, 8);

  private final Dimension window;
  private final int stepX;
  private final int stepY;

  /**
   * Instantiate a patches feature extractor using all class defaults.
   */
  public PatchesFeature() {
    this(DEFAULT_WINDOW);
  }

  /**
   * Instantiate a patches feature extractor with default step values.
   * 
   * @param window Window to use as patch size
   */
  public PatchesFeature(Dimension window) {
    this(window, DEFAULT_STEP_X, DEFAULT_STEP_Y);
  }

  /**
   * Instantiate a patches feature extractor.
   * 
   * @param window Window to use as patch size
   * @param stepX Step size in the x direction
   * @param stepY Step size in the y direction
   */
  public PatchesFeature(Dimension window, int stepX, int stepY) {
    this.window = window;
    this.stepX = stepX;
    this.stepY = stepY;
  }

  @Override
  public List<LocalFeature<SpatialLocation, FloatFV>> extractFeature(FImage object) {
    return getPatches(object, window, stepX, stepY);
  }

  /**
   * Generate a list of normalised patches from the input image. The number of patches returned will
   * be dependent on window size and step size.
   * 
   * @param img The image to extract patches from
   * @param window The size of patches
   * @param stepX Step size in the X direction between patches
   * @param stepY Step size in the Y direction between patches
   * @return A list of patches as positioned feature vectors
   */
  public static List<LocalFeature<SpatialLocation, FloatFV>> getPatches(FImage img,
      Dimension window, int stepX, int stepY) {
    List<LocalFeature<SpatialLocation, FloatFV>> patches = new ArrayList<>();

    // Generate a list of rectangles to extract as patches
    RectangleSampler rs = new RectangleSampler(img, stepX, stepY, window.width, window.height);
    for (Rectangle r : rs.allRectangles()) {
      // Extract patch as feature
      FImage extracted = img.extractROI(r);
      FeatureUtilities.inplaceNormalise(extracted);
      FloatFV feature = new FloatFV(extracted.getFloatPixelVector());
      SpatialLocation location = new SpatialLocation(r.x, r.y);
      // Record feature with location
      patches.add(new LocalFeatureImpl<SpatialLocation, FloatFV>(location, feature));
    }
    return patches;
  }

}
