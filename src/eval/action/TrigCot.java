package eval.action;

public class TrigCot implements Action {
  public double value(double inputs[]) { return 1.0 / Math.tan(inputs[0]); }
}
