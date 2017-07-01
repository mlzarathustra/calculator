package eval.action;

/**   value() will perform its action on the list of doubles.
 *    this model allows for monadic, dyadic, and chained definitions
 *    of operators such as + - ^ * /   &c.
 *    <p>
 *    it should really be static, but java won't allow that.
*/
public interface Action {
  double value(double[] inputs);
}
