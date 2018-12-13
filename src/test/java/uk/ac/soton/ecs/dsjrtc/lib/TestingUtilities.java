package uk.ac.soton.ecs.dsjrtc.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.vfs2.FileObject;
import org.openimaj.data.dataset.GroupedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.data.dataset.VFSListDataset;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.ClassificationResultUtils;
import org.openimaj.experiment.evaluation.classification.Classifier;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.confusionmatrix.CMResult;
import org.openimaj.image.FImage;

/**
 * General testing utilities.
 *
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class TestingUtilities {

  /**
   * Convert a relative path of a resource in the project resource folder to an absolute path.
   * 
   * @param relativePath Path from resource folder root
   * @return Absolute path to resource, null if resource is not found
   */
  public static String getResourcePath(String relativePath) {
    // Find the resource as a URL
    URL datasetURL = TestingUtilities.class.getResource("/" + relativePath);
    if (datasetURL == null) {
      System.err.println(String.format("Resource not found: '%s'", relativePath));
    } else {
      try {
        // Remove any URL artifacts
        URI datasetURI = new URI(datasetURL.toString());
        return datasetURI.toString();
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  
  /**
   * Print the detailed report of a classification evaluation.
   * 
   * @param classifier The classifier to test
   * @param testset A labeled testset to use for classifier assessment.
   * @return The created classification evaluator
   */
  public static ClassificationEvaluator<CMResult<String>, String, FImage> evaluateClassifier(
      Classifier<String, FImage> classifier,
      GroupedDataset<String, ListDataset<FImage>, FImage> testset) {
    // Create evaluator
    CMAnalyser<FImage, String> analyser = new CMAnalyser<>(CMAnalyser.Strategy.SINGLE);
    ClassificationEvaluator<CMResult<String>, String, FImage> evaluator =
        new ClassificationEvaluator<>(classifier, testset, analyser);
    // Generate and print report
    CMResult<String> result = evaluator.analyse(evaluator.evaluate());
    System.out.println(result.getDetailReport());
    return evaluator;
  }


  /**
   * Run a classifier on an unlabeled dataset. Optionally, can export classifications to file.
   * 
   * @param dataset Dataset to classify
   * @param classifier Classifier to use on dataset
   * @param sort Whether to sort in ascending order by file number
   * @param exportPath Path to export classifications to, will not export if null
   * @return Classifications
   */
  public static List<Pair<FileObject, ClassificationResult<String>>> classifyDataset(
      VFSListDataset<FImage> dataset, Classifier<String, FImage> classifier, boolean sort,
      String exportPath) {

    // Run dataset through classifier
    final List<Pair<FileObject, ClassificationResult<String>>> results =
        new ArrayList<>(dataset.numInstances());
    for (int i = 0; i < dataset.numInstances(); i++) {
      final FileObject file = dataset.getFileObject(i);
      final ClassificationResult<String> result = classifier.classify(dataset.get(i));
      results.add(new ImmutablePair<>(file, result));
    }
    if (sort) {
      inplaceSortByFileNum(results);
    }
    if (exportPath != null) {
      TestingUtilities.exportClassifications(exportPath, results);
    }
    return results;
  }


  /**
   * Export a list of classifications to file.
   * 
   * @param outputPath Name of output file (or path)
   * @param results List of results to corresponding files, do not need to be sorted
   * @return Whether export was successful
   */
  public static boolean exportClassifications(String outputPath,
      List<Pair<FileObject, ClassificationResult<String>>> results) {
    // Sort by file name
    inplaceSortByFileNum(results);

    // Export the results to file
    File file = new File(outputPath);
    FileWriter writer = null;
    try {
      writer = new FileWriter(file);
      for (Pair<FileObject, ClassificationResult<String>> result : results) {
        String clazz = ClassificationResultUtils.getHighestConfidenceClass(result.getValue());
        writer.write(String.format("%s %s\r\n", getFilename(result.getKey()), clazz));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
    return true;
  }

  /**
   * Sort the given list of pairs where the key is a file object. File objects should be
   * interpretable as some numeric value for sorting. Will be sorted in ascending order based on the
   * numeric value, that is: 1...9...15...100...
   * 
   * @param pairs Pairs to sort
   */
  public static <T> void inplaceSortByFileNum(List<Pair<FileObject, T>> pairs) {
    // Sort list by file number
    Collections.sort(pairs, new Comparator<Pair<FileObject, ?>>() {

      @Override
      public int compare(Pair<FileObject, ?> o1, Pair<FileObject, ?> o2) {
        return Integer.compare(getFileNumber(o1.getKey()), getFileNumber(o2.getKey()));
      }

      /**
       * Extract the file number from the given file object.
       * 
       * @param file File object to convert
       * @return The file as just it's number components
       */
      private Integer getFileNumber(FileObject file) {
        try {
          final String strNum = getFilename(file).replaceAll("\\D+", "");
          return Integer.valueOf(strNum);
        } catch (Exception e) {
          return null;
        }
      }

    });
  }

  /**
   * @param obj File object
   * @return Just the filename of the file object
   */
  public static String getFilename(FileObject obj) {
    return obj.getName().getBaseName();
  }

}
