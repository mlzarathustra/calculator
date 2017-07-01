package eval.action;

public class Plus implements Action {
  public double 
  value(double[] inputs) {
    switch (inputs.length) {
      case 0: return 0; // identity
      case 1: return inputs[0];
      default:
        double R=0;
        for (int i=0; i<inputs.length; ++i)
          R += inputs[i];
        return R;
    }
  }
}
