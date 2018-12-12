package uk.ac.soton.ecs.dsjrtc.lib;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.MeanCenter;

/**
 * Utilities directly applicable to feature generation.
 *
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class FeatureUtilities {

	/**
	 * Normalise a 1D array of floats. See {@link #normalise(float[][])}.
	 * 
	 * @param img Image to normalise in place
	 * @return Reference to the input pixels
	 */
	public static float[] inplaceNormalise(float[] pixels) {
		float[][] pixels2D = { pixels };
		inplaceNormalise(new FImage(pixels2D));
		return pixels;
	}

	/**
	 * Normalise a 2D array of floats by dividing by their sum and then mean
	 * centring.
	 * 
	 * @param pixels Pixels to normalise in place
	 * @return Reference to the input array
	 */
	public static float[][] inplaceNormalise(float[][] pixels) {
		inplaceNormalise(new FImage(pixels));
		return pixels;
	}
	
	/**
	 * Normalise an image's pixels. See {@link #normalise(float[][])}.
	 * 
	 * @param img Image to normalise in place
	 * @return Reference to the input pixels
	 */
	public static FImage inplaceNormalise(FImage img) {
		img.normalise();
		img.processInplace(new MeanCenter());
		return img;
	}

}
