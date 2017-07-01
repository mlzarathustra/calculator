package eval.action;

public class Times implements Action {
  public double 
  value(double[] inputs) {
    switch (inputs.length) {
      case 0: return 1; // identity
      case 1: // signum
              if (inputs[0]<0) return -1;
              if (inputs[0]>0) return +1;
              return 0;
              
      default:
        double R=1;
        for (int i=0; i<inputs.length; ++i) 
          R *= inputs[i];
        return R;
    }
  }
}
