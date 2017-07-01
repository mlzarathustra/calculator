package eval;

public class ExpressionSyntaxException extends Exception {
  String msg; int position;
  
  ExpressionSyntaxException(String m) { this(m,-1); }
  ExpressionSyntaxException(String m, int p) {
    msg=m; position=p;
  }
  
  public String toString() {
    return msg + ((position < 0) ? "":(" at position "+position));
  }
}
