package uk.ac.soton.ecs.dsjrtc.lib;

import org.joda.time.DateTime;

/**
 * Very simple static wrapper for sending print statements that can be toggled off globally.
 * 
 * @author David Jones (dsj1n15@soton.ac.uk)
 * @author Richard Crosland (rtc1g16@soton.ac.uk)
 */
public class Debugger {
  /** Whether output is enabled for any debug reports */
  private static boolean outputEnabled = true;
  /** Whether time should be prefixed to outputs */
  private static boolean timeOutput = true;

  /**
   * This class should not be instantiated.
   */
  private Debugger() {}

  /**
   * @param outputEnabled Whether debug output should be enabled globally
   */
  public static void setOutputEnabled(boolean outputEnabled) {
    Debugger.outputEnabled = outputEnabled;
  }

  /**
   * @return Whether global debug output is enabled
   */
  public static boolean isOutputEnabled() {
    return Debugger.outputEnabled;
  }

  /**
   * @param timeOutput Whether time should be prefixed to output
   */
  public static void setTimeOutput(boolean timeOutput) {
    Debugger.timeOutput = timeOutput;
  }

  /**
   * @return Whether time will be prefixed to output
   */
  public static boolean isTimeOutput() {
    return Debugger.timeOutput;
  }


  /**
   * Print with no linefeed if output is enabled. The current time will be prefixed if it is enabled
   * globally.<br>
   * See {@link System.out#print()}.
   * 
   * @param obj Object to print
   */
  public static void print(Object obj) {
    if (Debugger.isOutputEnabled()) {
      if (Debugger.isTimeOutput()) {
        System.out.print(String.format("[%s] ", Debugger.getTime()));
      }
      System.out.print(obj.toString());
    }
  }

  /**
   * Debug print with linefeed if output is enabled.<br>
   * See {@link Debugger#print(Object obj)}.
   * 
   * @param obj Object to print
   */
  public static void println(Object obj) {
    if (Debugger.isOutputEnabled()) {
      Debugger.print(obj);
      System.out.println();
    }
  }

  /**
   * @return The current time as a string
   */
  public static String getTime() {
    return DateTime.now().toString("HH:mm:ss");
  }

}
