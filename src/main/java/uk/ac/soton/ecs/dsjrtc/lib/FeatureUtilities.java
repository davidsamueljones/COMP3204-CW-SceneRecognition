package uk.ac.soton.ecs.dsjrtc.lib;

import org.openimaj.image.FImage;

/**
 * Utilities directly applicable to feature generation.
 *
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class FeatureUtilities {

  /**
   * Normalise a 1D array of floats. See {@link #normalise(float[][])}.
   * @param img Image to normalise in place
   * @return Reference to the input pixels
   */
  public static float[] inplaceNormalise(float[] pixels) {
    float[][] pixels2D = {pixels};
    inplaceNormalise(pixels2D);
    return pixels;
  }
  
  /**
   * Normalise an image's pixels. See {@link #normalise(float[][])}.
   * @param img Image to normalise in place
   * @return Reference to the input pixels
   */
  public static FImage inplaceNormalise(FImage img) {
    inplaceNormalise(img.pixels);
    return img;
  }
  
  /**
   * Normalise a 2D array of floats by dividing by their variance and subtracting their mean.
   * 
   * @param pixels Pixels to normalise in place
   * @return Reference to the input array
   */
  public static float[][] inplaceNormalise(float[][] pixels) {
    final int height = pixels.length;
    final int width = pixels[0].length;

    // Calculate mean and variance of the image
    float sum = 0;
    float sumsq = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        final float val = pixels[y][x];
        sum += val;
        sumsq += val * val;
      }
    }
    final int pixelCount = height * width;
    final float mean = sum / pixelCount;
    final float var = sumsq / pixelCount - mean * mean;

    // Remove the mean intensity and scale it by its intensity variance
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        pixels[y][x] = (pixels[y][x] - mean) / var;
      }
    }
    return pixels;
  }

}
