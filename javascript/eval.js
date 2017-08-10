//  evaluator 
//
//  [ported from java]
//  (c) 2017 miles zarathustra
//

var EVAL_DEBUG=false;

function isValue(obj) {
    return (obj.isA()=='Noun' || obj.isA()=='Expression');
}


// either a number or a reference into a Noun in Symbols.
// we resolve late to facilitate graphing.
//
class Noun {
    constructor(v) { 
        if ( typeof(v)=='string' && v.match(/^[a-zA-Z_]/) ) {
            this.string = v;
            this.isPointer=true;
        }
        else {
            this.value = parseFloat(v); 
            this.isPointer=false;
        }
    }
    toString() { return "Noun: { value="+this.getValue()+" } "; }
    isA() { return 'Noun'; }
    getValue() { 
        if (this.isPointer) return Symbols[string].value;
        else return this.value; 
    }
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
                continue;
            }
            let nm=/^(-[0-9\.]+)(.*)/.exec(s);  // prefix - to a number?
            if (nm != null && (rs.length==0 || rs[rs.length-1].isA() == 'Verb')) {
                rs.push(new Noun(nm[1]));
                s=nm[2];
                continue;
            }

            nm=/^([0-9\.]+)(.*)/.exec(s);
            if (nm != null) {
                rs.push(new Noun(nm[1]));
                s=nm[2];
                continue;
            }

            let m=/^([A-Za-z]+|.)(.*)/.exec(s);
            let sym=m[1].trim();
            if (sym.length > 0) {
                let v=Symbols[sym];
                if (v == undefined) this.ErrorList.push('Undefined: '+m[1]);
                rs.push(v);
            }
            s=m[2];
        }
        return rs;
    }

    // crude rounding, e.g. so sin(pi) returns 0.
    // "14" was arrived at by empirical observation.
    // for display only. best not to use for intermediate values
    //
    fudge() { return 1 * this.getValue().toFixed(14); }

    // returns a number
    getValue() {
        if (this.action == null) return this.rightArg.getValue();
        if (this.leftArg == null) return this.action.act(this.rightArg.getValue() );
        return this.action.act(this.leftArg.getValue(), this.rightArg.getValue());
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
            if ( (rs.length>0 && isValue(rs[rs.length-1])) && (
                    isValue(t) ||
                    (t.isA()=='Verb' && t.parseMode==ParseAs.FUNCTION) 
                )
            ) {
                rs.push(Symbols['*']);
            }
            rs.push(t);
        }
        return rs;
    }

    //  from idx (a verb) grab to the right until we find a verb with 
    //  equal or lower precedence (or the end)
    //  return: the index of what we found (which should work for slice())
    //  e.g. - x ^ 3
    //
    grabRight(tokens, idx) {
        if (tokens[idx].isA() != 'Verb') {
            ErrrorList.push('Verb expected');
            return;
        }

        let prec=tokens[idx].precedence;
        let nextVerb=idx+1;

        while (nextVerb < tokens.length) {
            if (tokens[nextVerb].isA()=='Verb' && tokens[nextVerb].precedence <= prec) return nextVerb;
            ++nextVerb;
        }
        return tokens.length;
    }


    // for each verb with no left argument,
    // replace from it up to the next noun with expression
    // e.g. sin x + 3
    //
    //
    collapseUnaryChains(tokens) {
        let rs=[];
        for (let idx=0; idx<tokens.length; ++idx) {
            if (tokens[idx].isA()=='Verb' && (idx==0 || tokens[idx-1].isA()=='Verb')) {
                let nextNoun=idx+1;
                while (nextNoun < tokens.length && !isValue(tokens[nextNoun])) ++nextNoun;
                if (nextNoun == idx) { // we're at the end
                    ErrorList.push('Verb missing right argument: '+tokens[idx].string);
                    return;
                }
                // to be thorough, step back through the chain, 
                // grabbing right each time to form an expression
                // however, for the common case (-x^2) it's enough to grab just for
                // the last Verb. Between - and /,* the order doesn't matter

                let nextVerb=this.grabRight(tokens,nextNoun-1);
                rs.push( new Expression( [].concat(
                    tokens[idx], 
                    new Expression( tokens.slice(idx+1, nextVerb))
                )));

                idx=nextVerb;
                if (idx<tokens.length) rs.push(tokens[idx]);
            }
            else rs.push(tokens[idx]);
        }

        return rs;
    }

    findTreeTopIdx(tokens) {
        let minPreced = this.getSymbolWithMinPrecedence(tokens);
        if (EVAL_DEBUG) console.log('min precedence is '+minPreced);
        if (minPreced == null) {
            this.ErrorList.push('No verb found in phrase');
            return;
        }

        // we're picking the verb to execute LAST
        //
        if (minPreced.parseMode == ParseAs.LEFT) {
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

        TODO - report more errors
    */
    isBasicCase(tokens) {
        switch (tokens.length) {
            case 1: 
                if (isValue(tokens[0])) {
                    this.rightArg=tokens[0]; 
                    return true;
                }
                else ErrorList.push('no argument found');
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

        let tokens;         // overload c'tor: string or array
        if (typeof(arg) == 'string') {
            tokens=this.tokenize(arg);
            if (this.ErrorList.length > 0) return; 
        }
        else tokens=arg;

        tokens = this.collapsePrens(tokens);
        if (EVAL_DEBUG) console.log('tokens, after collapsePrens: '+tokens);

        tokens = this.addImpliedMul(tokens);
        if (EVAL_DEBUG) console.log('tokens, after addImpliedMul: '+tokens);

        // need to check here or collapseUnaryChains will recurse infinitely
        if (this.isBasicCase(tokens)) return;

        tokens = this.collapseUnaryChains(tokens);
        if (EVAL_DEBUG) console.log('tokens after collapseUnaryChains: '+tokens);

        if (this.isBasicCase(tokens)) return;

        //
        //  not a basic case: form tree

        let treeTopIdx = this.findTreeTopIdx(tokens);
        if (EVAL_DEBUG) console.log('treeTopIdx is '+treeTopIdx);

        if (treeTopIdx > 0) this.leftArg = new Expression(tokens.slice(0,treeTopIdx));
        this.action = tokens[treeTopIdx];
        this.rightArg = new Expression( tokens.slice(treeTopIdx+1) );
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
        e:  new Noun( Math.E )
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



function calcResult(id) {
    let el=document.getElementById(id);
    let s=el.value;
    if (EVAL_DEBUG) console.log('s is '+s);
    let expr=new Expression(s);
    if (EVAL_DEBUG) console.log(expr);

    let errs=document.getElementById('errors');
    if (expr.ErrorList.length>0) {
        if (EVAL_DEBUG) {
            console.log('ERROR(S)');
            expr.ErrorList.forEach( function(msg) { console.log(msg); });
        }

        errs.innerHTML=expr.ErrorList.join('<br/>')
        errs.style.display='block';
    }
    else {
        errs.style.display='none';
        document.getElementById('result').innerHTML = ''+expr.fudge();
    }
}

//  unused
//
function traceInitialization() {
    console.log('verbs: '+_verbs);
    console.log('Symbols: '+Symbols);
    Object.keys(Symbols).forEach(
        function(key) { console.log(key+': '+Symbols[key]); } 
    );    
}






