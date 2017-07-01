package eval.action;

public class Sqrt implements Action {
  public double value(double[] values) { return Math.sqrt(values[0]); }
}
