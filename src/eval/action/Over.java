package eval.action;

public class Over implements Action { // divide by
  public double 
  value(double[] inputs) {
    switch (inputs.length) {
      case 0: return 1;             // identity
      case 1: return 1/inputs[0];   // invert
      default:                      // "l" -> "r"
        double R=inputs[0];  
        for (int i=1; i<inputs.length; ++i) 
          R /= inputs[i];
        return R;
    }
  }
}
