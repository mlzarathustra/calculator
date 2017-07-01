//
//
// Analyze
//
//
package form.renderer;

import java.util.*;

import form.util.*;
/**
 *    a bunch of utilities for analyzing formulae with.
 * 
 *    making the functions final lets the compiler optimize
 *    (theoretically) by inlining them.
 * 
 *    
 */
public class Analyze {

  public static final int 
    _SQRT_VALUE=0x221a,
    _SIGMA_VALUE=0x3a3, 
    _INTEGRAL_VALUE=0x222b,
    _PLUSMINUS_VALUE=0x00b1
  ;

  public final static boolean
  isAlpha(char c) {
    c=Character.toLowerCase(c);
    if (c>='a' && c<='z') return true;
    return false;
  }
  public final static boolean
  isNumeric(char c) {
    if (c>='0' && c<='9') return true;
    if (c == '.') return true;
    return false;
  }
  public final static boolean
  isSymbol(char c) {
    return c>0xff;
  }
  /**   this is mostly to avoid the deprecation warning....
   *    the recommended Character.isWhitespace consumes much
   *    time with a lot of checks we don't need.
   */
  public final static boolean
  isSpace(char c) {
    return c==' ' || c=='\t' || c=='\r' || c=='\n';
  }
  /** 
        note: ',' is also an "operator."  
  */
  public final static boolean
  isStructural(char c) {
    return (isBracket(c) || isEncloser(c) || c==',')  
      ? true : false;
  }
  public final static boolean 
  isSpecial(char c) {
    return (special.indexOf(c)<0)?false:true;
  }
  /**
   	Is this dash the sign of a number?
        look at it in context to find out.
   */
  public final static boolean
  isSign(String s, int i) {
    try {
      if (s.charAt(i) != '-') return false;		             // is a dash
      if (!isNumeric(s.charAt(nextNonBlank(s,i+1)))) return false;   // followed by digit
      int previous=prevNonBlank(s,i-1);
      if (previous<0) return true;                                   // @ beginning

      char c=s.charAt(previous);
      if (isRightBracket(c)) return false;            // not preceded by right bracket 
      if (!isAlpha(c) && !isNumeric(c)) return true;  // or other type of value
    }      
    catch (StringIndexOutOfBoundsException e) {
      return false;
    }
    return false;
  }

  /**
       a period is a . but not a decimal.
       NB. According to comments in the source code, 
       Character.isDigit might not work for the 
       Tibetan alphabet.    :-)
  */
  public final static boolean
  isPeriod(String s, int i) {
    try {
      int prev=prevNonBlank(s,i-1),
          next=nextNonBlank(s,i+1);

      if (s.charAt(i)!='.') return false;
      if (prev>= 0 && Character.isDigit(s.charAt(prev)) || 
          next>= 0 && Character.isDigit(s.charAt(next))   
      )
        return false;
    }
    catch (StringIndexOutOfBoundsException e) {
    }
    return true;
  }
  /**
  	here, isOperator catches either - as either a sign or operator.
  	So, we could just use a char arg.
  */
  public final static boolean
  isNothing(String s, int i) {
    char c=s.charAt(i);			
    return !(isNumeric(c) || isAlpha(c) || isOperator(c) || 
      isStructural(c) || isSpecial(c) || isSymbol(c));
  }
  /**
        brackets are in pairs (eg. {}}.  
        enclosers are single. (eg. ||)
                                                              <p>
        ### encloser parsing is tricky (since it needs to account for 
        bracket parsing and possibly \ ) so hold off for now.
                                                              <p>
        ### theoretically, brackets could be more than 1 char eg 
            c-style comments
  */
  public static final String[] brackets={"()", "[]", "{}"};
  /**
        brackets are in pairs (eg. {}}.  
        enclosers are single. (eg. ||)
                                                              <p>
        ### the quote signs may be used in the sense of 'prime'  (eg d')
        so they are't enclosers.
  */
  public static final String[] enclosers={"|"/*,"'", "\""*/};

  /**
      these characters end OPERATOR concat
  
  */
  public static final String special="^_*&\"" + (char)_SQRT_VALUE + 
    (char)_INTEGRAL_VALUE + (char)_SIGMA_VALUE; 


  // in order of precedence, highest first
  // ### these may want to be more than 1 char eg. \sqrt
  // if not, the effect will be to make \sqrt have 
  // very low precedence.
  //
  //  the backslash by itself gets swallowed.
  //  being the lowest possible precedence, it always breaks
  //  a right or left grab.
  //

  public static final String comparators="<=>\u2265\u2264\u2260\u2245\u2261";

  //  the below want to have spaces on either side.
  //                        
  public static final String miscOprs=
    "\u00ac\u00b1\u00f7\u00d7\u2208\u2209\u220d\u2227\u2228\u222b\u222c\u2234"+
    "\u00ac\u2282\u2283\u2284\u2286\u2287";

  /*
         adding brackets & enclosers to operators
         fixes "|a/b|" 
         but breaks a^2/b
         for some reason, just adding enclosers works. (!)
         search for isOperator().

  */
  public static final String[] operators={ 
      "^_" ,"*/","+-&!", comparators,miscOprs, 
        ",", /*Util.join(brackets) + */ Util.join(enclosers), "\\\""};

  
  public static int                               
  nextRightPren(String s, int i) {
    if (s.charAt(i) != '(') return -1;
    int nesting=1;
    try {
      while (nesting>0) { ++i;
        if (s.charAt(i)=='(') ++nesting;
        if (s.charAt(i)==')') --nesting;
      }
      return i;
    }
    catch (StringIndexOutOfBoundsException e) {
      // no need for errors -- just give the end of string.
      return s.length();
    }
  }

  /**
      index of next matching "
      if none, then s.length()
   */
  public static int 
  nextQuote(String s, int idx) {
    for (;;) {
      try {
        idx=s.indexOf('"',idx+1);
        if (s.charAt(idx-1) != '\\') return idx;
      }
      catch (Exception e) {
        return s.length();
      }
    }
  }

  /**  
     match left or right bracket from list 'brackets.'
     Unlike matchingEncloser, this function assumes integrity 
     of nesting with brackets of other types, and does not
     return an error in those cases.

     @return: There are 3 possible return cases

     <OL>
       <LI> match: returns the match
       <LI> no match: returns...
         <UL>
            <LI> left bracket: str.length()
            <LI> right bracket: 0
         <UL>

       <LI> error: returns -1
     </OL>

     Brackets match in pairs -- eg () {} [] 

  */
  public static int                               
  matchingBracket(String s, int idx) {
    boolean left=false;
    int whichBracket=0;

    for (whichBracket=0; whichBracket<brackets.length; ++whichBracket) {
      if (s.charAt(idx)==brackets[whichBracket].charAt(0)) {
        left=true; break;
      } 
      else if (s.charAt(idx)==brackets[whichBracket].charAt(1)) {
        left=false; break;
      }
    }
    if (whichBracket>=brackets.length) return -1; // not a bracket

    int nesting=1;
    if (left) {
      try {
        while (nesting>0) { ++idx;
          if (s.charAt(idx)==brackets[whichBracket].charAt(0)) ++nesting;
          if (s.charAt(idx)==brackets[whichBracket].charAt(1)) --nesting;
        }
        return idx;
      }
      catch (StringIndexOutOfBoundsException e) { return s.length();
      }
    }
    else {// right
      try {
        while (nesting>0) { --idx;
          if (s.charAt(idx)==brackets[whichBracket].charAt(1)) ++nesting;
          if (s.charAt(idx)==brackets[whichBracket].charAt(0)) --nesting;
        }
        return idx;
      }
      catch (StringIndexOutOfBoundsException e) { return 0;
      }
    }
  }

  /**
     Find the matching encloser: Enclosers use the same character 
     for right and left eg. | " '                                 <P>

     If a left bracket is found, skip to the matching 
     right bracket.

     @return: There are 3 possible return cases: 

     <OL>
       <LI> match: returns the match
       <LI> no match: returns str.length()
       <LI> right encloser first (== error): returns -1
     </OL>

    @see Analyze#matchingBracket



  */
  public static int
  matchingEncloser(String str, int idx) {
    try {
      char e=str.charAt(idx);
      while (++idx<str.length()) {  // skip bracketed stuff
        if (isLeftBracket(str.charAt(idx))) {
          idx=matchingBracket(str,idx);
          if (idx<0) return -1;     // error
          continue;
        }
        else if (isRightBracket(str.charAt(idx))) {
          return -1;                // error
        }
        else if (str.charAt(idx) == e) return idx;
      }
    }
    catch (StringIndexOutOfBoundsException e) {
      return str.length();          // no match
    }
    return str.length();  // idx may be this + 1
  }


  /**

      Splits a string into 'count' arguments, separated by 'delim.'  
      If they supplied extra arguments, we just tack them all on
      as the last (extra) argument of the result.

      @param separators = argument separator(s)
      @param n = how many arguments expected
      @param str = argument list

  */
  public static String[] 
  splitArgs(int count, String str) {
    final char delim=',';
    Vector args=new Vector();
    int here,last;
    
    for (here=0, last=0; here<str.length(); ++here) {
      int nextBracket=matchingBracket(str,here);
      if (nextBracket >= 0) {
        here=nextBracket; continue;
      }
      if (delim == str.charAt(here)) {
        args.addElement(str.substring(last,here));
        last=here+1;
        if (--count <= 0) {
          here=str.length();
          break;
        }
      }
    }
    args.addElement(
      str.substring(last,Math.min( str.length(), here )) );

    String [] r=new String[args.size()];
    for (int i=0; i<args.size(); ++i) r[i]=(String)(args.elementAt(i));
    
    //db.pf("splitArgs: result=");
    //for (int i=0; i<r.length; ++i) db.pf("["+i+"] = "+r[i]);
    
    return r;
  }
  
  public static boolean 
  isInList(char c, String[] list) { 
    for (int i=0; i<list.length; ++i) {
      if (list[i].indexOf(c)>=0) return true;
    }
    return false;
  }
  public static boolean isBracket(char c) { return isInList(c,brackets); }
  public static boolean isEncloser(char c) { return isInList(c,enclosers); }
  public static boolean isCompare(char c) { return comparators.indexOf(c)>=0; }
  public static boolean isOperator(char c) { return isInList(c,operators); }
  public static boolean isOperator(String s, int i) {
    return isOperator(s.charAt(i)) && !isSign(s,i);
  }
  public static boolean 
  isLeftBracket(char c) { 
    for (int i=0; i<brackets.length; ++i) {
      if (brackets[i].charAt(0) == c) return true;
    }
    return false;
  }
  public static boolean 
  isRightBracket(char c) { 
    for (int i=0; i<brackets.length; ++i) {
      if (brackets[i].charAt(1) == c) return true;
    }
    return false;
  }
  public static int 
  precedenceOf(char a) {
    for (int i=0; i<operators.length; ++i)
      if (operators[i].indexOf(a) >=0) return i;
    return operators.length;
  }
  /** 
      return == as strcmp
   */
  public static int
  comparePrecedence(char a, char b) {
    int pa=precedenceOf(a),pb=precedenceOf(b);
    if (pa==pb) return 0;
    else return pa<pb?-1:1;
  }

  public static int
  nextNonBlank(String s, int i) {
    try {
      while (isSpace(s.charAt(i))) ++i;
    }
    catch (Exception e) { return -1; }
    return i;
  }
  public static int
  prevNonBlank(String s, int i) {
    try {
      while (isSpace(s.charAt(i))) --i;
    }
    catch (Exception e) { return -1; }
    return i;
  }

}

