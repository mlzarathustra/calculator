package eval.action;

public class Power implements Action { 
  public double 
  value(double[] inputs) {
    switch (inputs.length) {
      case 0: return 1; // identity
      case 1: return Math.exp(inputs[0]); // e^x
              
      default: // only works for 2 args
               //  XXX -- work out the chaining case (R->L)
        return Math.pow(inputs[0],inputs[1]);      
    }
  }
}
