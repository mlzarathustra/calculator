//  evaluator 
//
//  2017 port from java
//  (c) miles zarathustra
//

function isValue(obj) {
    return (obj.isA()=='Noun' || obj.isA()=='Expression');
}


class Noun {
    constructor(v) { this.value = parseFloat(v); }
    toString() { return "Noun: { value="+this.value+" } "; }
    isA() { return 'Noun'; }
    getValue() { return this.value; }
}

var ParseAs ={
    // left-to-right operator, right-to-left operator, 
    //      function, chain, noun
    LEFT:1, RIGHT:2, FUNCTION:3, NOUN: 4
}

class Verb {
    constructor(s,p,a) {
        this.string = s;
        this.parseMode = p;
        this.act = a;

        this.precedence=0; // set later
    }

    toString() {
        return "Verb: {\""+this.string+"\" precedence "+this.precedence+
            "; parse mode: "+ ["left","right","function","chain"][this.parseMode-1]+"}";
    }
    isA() { return 'Verb'; }
 }

 class Pren {
     constructor(which) { this.which = (which=='(') ? '(' : ')'; }
     isOpen() { return this.which=='('; }
     isClose() { return this.which==')'; }
     toString() { return "Pren: "+this.which; }
     isA() { return 'Pren'; }
}

/*
    Expression.cmp (components) are in one of these forms:

        Value               rightArg
        Verb Value          action rightArg
        Value Verb Value    leftArg action rightArg

    Value is either an Expression or a Noun, as determined by the 
    isValue() fuction;

    look at Expression.isValue() to see how they are used.

*/

class Expression {

    // parse formula into an array of Noun, Verb, and Pren tokens
    //
    tokenize(s) {
        let rs=[];
        while (s!=null && s.length > 0) {
            let pm=/^([\(\)])(.*)/.exec(s); 
            if (pm != null) {
                rs.push(new Pren(pm[1]));
                s=pm[2];
            }
            else {
                let nm=/^([0-9\.]+)(.*)/.exec(s);
                if (nm != null) {
                    rs.push(new Noun(nm[1]));
                    s=nm[2];
                }
                else {
                    let m=/^([A-Za-z]+|.)(.*)/.exec(s);
                    let sym=m[1].trim();
                    if (sym.length > 0) {
                        let v=Symbols[sym];
                        if (v == undefined) this.ErrorList.push('Undefined: '+m[1]);
                        rs.push(v);
                    }
                    s=m[2];
                }
            }
        }
        return rs;
    }

    // returns a number
    getValue() {
        if (action == null) return rightArg.getValue();
        if (leftArg == null) return action(rightArg);
        return action(leftArg,rightArg);
    }

    collapsePrens(tokens) {
        let rs=[];
        for (let idx=0; idx<tokens.length; ++idx) {   
            let token=tokens[idx];                

            if (token.isA() == 'Pren') { 
                if (token.isClose()) {
                    this.ErrorList.push("Unexpected ) token="+idx);
                }
                else {
                    let nest=1;
                    for (let j=idx+1; j<tokens.length; ++j) {
                        if (tokens[j].isA() == 'Pren') {
                            if (tokens[j].isOpen()) ++nest;
                            else {
                                --nest;
                                if (nest == 0) {
                                    let subExpr=new Expression(tokens.slice(idx+1,j));
                                    if (subExpr.ErrorList.length>0) {
                                        subExpr.ErrorList.forEach( 
                                            function(err) { this.ErrorList.push(err); } 
                                        );
                                        return [];
                                    }

                                    rs.push(subExpr);

                                    tokens.splice(idx,1+j-idx);
                                    --idx; 
                                    break;
                                }
                            }
                        }
                    }

                }
            }
            else {
                rs.push(tokens[idx]);
            }
        }
        return rs;
    }
/*
    getSymbolWithMaxPrecedence(tokens) {
        let rs=null;
        for (let token of tokens) {
            if (token.isA() == 'Verb') {
                if (rs == null || token.precedence > rs.precedence) rs=token;
            }
        }
        return rs;
    }
*/    
    getSymbolWithMinPrecedence(tokens) {
        let rs=null;
        for (let token of tokens) {
            if (token.isA() == 'Verb') {
                if (rs == null || token.precedence < rs.precedence) rs=token;
            }
        }
        return rs;
    }

    addImpliedMul(tokens) {
        let rs=[];
        for (let t of tokens) {
            if (isValue(t) && rs.length>0 && isValue(rs[rs.length-1])) {
                rs.push(Symbols['*']);
            }
            rs.push(t);
        }
        return rs;
    }

    // immediately to the left of a value, the - has high precedence: -1 + 2 = 1
    // same with +, but it is a no-op.
    //
    // collapseNegative(tokens) {
    //     for (let idx=1; idx<tokens.length - 1; ++idx) {
    //         if (tokens[idx].isA()=='Verb' && isValue(tokens[idx+1]) &&
    //             tokens[idx-1].isA()=='Verb' &&
    //             tokens[idx].string.match(/[+-]/) ) {

    //             switch(tokens[idx].string) {
    //                 case '+': console.log('unary plus'); 
    //                     //  no-op (the + gets removed below)
    //                     break;

    //                 case '-': console.log('unary minus'); 
    //                     tokens[idx+1] = new Expression( [tokens[idx],tokens[idx+1]] )
    //                     break;
    //             }
    //             tokens.splice(idx,1);
                
    //         }
    //     }

    //     return tokens;
    // }

    //  allowing any operation to be monadic quickly becomes convoluted
    //  as you may have to parse in both directions at once.
    //  but it would be a nice feature. 
    
    //  (it's easier in APL because there is no precedence)
    //
    // TODO - the amount to grab
    // grabMonadic(tokens, vIdx) {
    //     let preced = tokens[vIdx].precedence;
    //     while (vIdx < tokens.length-1) { 
    //         if (tokens[vIdx].isA()=='Verb' && tokens[vIdx]) )


    //         ++vIdx;
    //     }
    //     return vIdx;
    // }
    //     //   is the token to the left a verb? if so, we are monadic 

    //     //   3 - 2  

    //     // expression { 3 * expression { / expression { - 2 }}
    //


    // for each verb with no left argument,
    // replace from it up to the next noun with expression
    // TODO - implement precedence


    collapseUnaryChains(tokens) {
        let rs=[];
        for (let first=0; first<tokens.length; first++) {
            if (tokens[first].isA() == 'Verb') {
                let last=first;
                while (tokens.length>last+1 && tokens[last+1].isA()=='Verb') ++last;
                for (let idx=last; idx>=first; --idx) {
                     let grabRight=grabMonadic(tokens, last); 
 
                }                
            }
        }
        return tokens;
    }


    findTreeTopIdx(tokens) {
        let minPreced = this.getSymbolWithMinPrecedence(tokens);
        console.log('min precedence is '+minPreced);

        if (minPreced.parseMode == ParseAs.RIGHT) {
            for (let idx=tokens.length-1; idx>=0; --idx) {
                if (tokens[idx].isA()=='Verb' && tokens[idx].precedence == minPreced.precedence) {
                    return idx;
                }
            }
        }
        else {
            for (let idx=0; idx<tokens.length; ++idx) {
                if (tokens[idx].isA()=='Verb' && tokens[idx].precedence == minPreced.precedence) {
                    return idx;
                }
            }
        }



        // not reached
    }

    /*
        return: If this is one of the basic cases, assign our members as 
        shown below. The three basic cases: 

        Value               rightArg
        Verb Value          action rightArg
        Value Verb Value    leftArg action rightArg
    */
    isBasicCase(tokens) {
        switch (tokens.length) {
            case 1: 
                if (isValue(tokens[0])) {
                    this.rightArg=tokens[0]; 
                    return true;
                }
                break;

            case 2: 
                if (tokens[0].isA()=='Verb' && isValue(tokens[1])) {
                    this.action=tokens[0];
                    this.rightArg=tokens[1];
                    return true;
                }
                break;

            case 3: 
                if (isValue(tokens[0]) && tokens[1].isA()=='Verb' && isValue(tokens[2])) {
                    this.leftArg=tokens[0];
                    this.action=tokens[1];
                    this.rightArg=tokens[2];
                    return true;
                }
                break;

        }
        return false;





    }


    // construct Expression from string or token list 
    //
    constructor(arg) { 
        this.ErrorList=[];
        this.leftArg=null;  // see comment above class decl
        this.rightArg=null;
        this.action=null;

        let tokens;
        if (typeof(arg) == 'string') {
            tokens=this.tokenize(arg);
            if (this.ErrorList.length > 0) return; 
        }
        else tokens=arg;

        tokens = this.collapsePrens(tokens);
        console.log('tokens, after collapsePrens: '+tokens);

        tokens = this.addImpliedMul(tokens);
        console.log('tokens, after addImpliedMul: '+tokens);

        tokens = this.collapseUnaryChains(tokens);
        console.log('tokens after collapseNegative: '+tokens);

        if (this.isBasicCase(tokens)) return;




        let treeTopIdx = this.findTreeTopIdx(tokens);
        console.log('treeTopIdx is '+treeTopIdx);




        /* 
                    TODO

                    i think all that's left is to make a tree with the 
                    new Expression(left tokens) action new Expression(right tokens) 
                    
                    the RIGHT/LEFT parsing should be correct.
        
        
        */


    }


    toString() { return "Expression: { "+
        ( this.leftArg == null ? '' : this.leftArg+' ' )
        +( this.action == null ? '' : (this.action.string+' ') ) +
        this.rightArg +
    "}"; }
    isA() { return 'Expression'; }


}


// \\// \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\// \\ // \\


let _verbs = // outer dimension is precedence
[
    [
        new Verb('+',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 0; // identity
                    case 1: return a;
                    case 2: return a+b;
                }
                return 0;
            }
        ),
        new Verb('-',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 0; // identity
                    case 1: return -a;
                    case 2: return a-b;
                }
                return 0;
            }
        )
    ],    
    [
    new Verb('/',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 1; // identity
                    case 1: return 1/a;
                    case 2: return a/b;
                }
                return 0;
            }
        )
        ,
        new Verb('*',ParseAs.LEFT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 1; // identity
                    case 1: return Math.sign(a);
                    case 2: return a*b;
                }
                return 0;
            }
        )
    ],
    [
        new Verb('^',ParseAs.RIGHT, 
            function(a,b) { 
                switch(arguments.length) {
                    case 0: return 1; // identity
                    case 1: return Math.exp(a);
                    case 2: return Math.pow(a,b);
                }
                return 0;
            }
        )
    ],
    [   
        new Verb( 'sqrt', ParseAs.FUNCTION, function(n) { return Math.sqrt(n); } ),
        new Verb( 'sin', ParseAs.FUNCTION, function(n) { return Math.sin(n); } ),
        new Verb( 'cos', ParseAs.FUNCTION, function(n) { return Math.cos(n); } ),
        new Verb( 'tan', ParseAs.FUNCTION, function(n) { return Math.tan(n); } ),
        new Verb( 'cot', ParseAs.FUNCTION, function(n) { return Math.cot(n); } )
    ]

]

var Symbols = {
        pi: new Noun( Math.PI ),
        e: new Noun(Math.E)
};

function initSymbols() {
    for (prec=0; prec<_verbs.length; ++prec) {
        for (let v of _verbs[prec]) {
            v.precedence = prec;
            Symbols[v.string] = v;
        }
    }
}

initSymbols();





function showTokens(id) {
    let el=document.getElementById(id);
    let s=el.value;
    console.log('s is '+s);
    let expr=new Expression(s);
    console.log(expr);

    if (expr.ErrorList.length>0) {
        console.log('ERROR(S)');
        expr.ErrorList.forEach( function(msg) { console.log(msg); });
    }
}






