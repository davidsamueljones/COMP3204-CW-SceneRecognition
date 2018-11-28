package uk.ac.soton.ecs.dsjrtc.features;

import java.awt.Dimension;
import org.openimaj.image.FImage;
import org.openimaj.image.processing.resize.ResizeProcessor;

public class TinyImage {

  public static float[] makeTinyImage(FImage img, Dimension scale) {
    // Crop to square around the centre
    final int dim = Math.min(img.width, img.height);
    final int fullPixels = dim * dim;
    img = img.extractCenter(dim, dim);

    // Calculate zero-mean and unit length the image
    float sum = 0;
    float sumsq = 0;
    for (int y = 0; y < img.height; y++) {
      for (int x = 0; x < img.width; x++) {
        final float val = img.pixels[y][x];
        sum += val;
        sumsq += val * val;
      }
    }
    float mean = sum / fullPixels;
    float var = sumsq / fullPixels - mean * mean;
    // Apply normalisation
    for (int y = 0; y < img.height; y++) {
      for (int x = 0; x < img.width; x++) {
        img.pixels[y][x] = (img.pixels[y][x] - mean) / var;
      }
    }

    // Resize to tiny image scale
    img.processInplace(new ResizeProcessor(scale.width, scale.height));

    // Pack pixels into vector
    final float[] packed = img.getFloatPixelVector();
    return packed;
  }
}
