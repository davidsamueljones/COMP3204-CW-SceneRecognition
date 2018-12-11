package uk.ac.soton.ecs.dsjrtc.features;

import java.awt.Dimension;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;
import uk.ac.soton.ecs.dsjrtc.lib.FeatureUtilities;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FloatFV;

/**
 * Tiny image generator class that directly implements the feature extractor class for generic
 * classifiers. <br>
 * 
 * Static image generation can be achieved using the
 * {@link #makeTinyImage(FImage img, Dimension scale, boolean normalise)} method.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class TinyImageFeature implements FeatureExtractor<FloatFV, FImage> {
  public static final Dimension DEFAULT_SCALE = new Dimension(4, 4);
  public static final boolean DEFAULT_NORMALISE = true;

  private final Dimension scale;
  private final boolean normalise;

  /**
   * Instantiate a tiny image feature extractor using all class defaults.
   */
  public TinyImageFeature() {
    this(DEFAULT_SCALE, DEFAULT_NORMALISE);
  }

  /**
   * Instantiate a tiny image feature extractor.
   * 
   * @param scale Scale for feature extractor to use for tiny image generation
   * @param normalise Whether feature extractor should use normalisation in tiny image generation
   */
  public TinyImageFeature(Dimension scale, boolean normalise) {
    this.scale = scale;
    this.normalise = normalise;
  }

  @Override
  public FloatFV extractFeature(FImage img) {
    float[] feature = makeTinyImage(img, scale, normalise);
    return new FloatFV(feature);
  }

  /**
   * Create a tiny image vector from an input images to the provided configuration.
   * 
   * @param img Image to create the tiny image vector from
   * @param scale Size to resize the image to before packing
   * @param normalise Whether the image should be normalised
   * @return The packed tiny image vector, length will be scale.width * scale.height
   */
  public static float[] makeTinyImage(FImage img, Dimension scale, boolean normalise) {
    // Crop to square around the centre
    final int dim = Math.min(img.width, img.height);
    img = img.extractCenter(dim, dim);

    // Resize to tiny image scale
    img.processInplace(new ResizeProcessor(scale.width, scale.height));
    // Apply normalisation to image
    if (normalise) {
      FeatureUtilities.inplaceNormalise(img);
    }
    
    // Pack pixels into vector
    final float[] packed = img.getFloatPixelVector();
    return packed;
  }

}
