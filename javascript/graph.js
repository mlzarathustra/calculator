
class Point {
    constructor(x,y) {
        this.x=x;
        this.y=y;
    }
}
Util = {
    doubleToInteger: function(d) { return Math.round(d); }
}

class Graph {
    constructor(canvas) {
        this.canvas=canvas;
        this.scale=10;
        this.gridIncrement=1;
        this.axisColor='black';
        this.gridColor='lightGray';
        this.resetOrigin();
    }
    getWidth() { return this.canvas.width; }
    getHeight() { return this.canvas.height; }
    resetOrigin() { this.origin=new Point(this.getWidth()/2, this.getHeight()/2); }


    /**  
     *  translations: 
        logical to physical, & vice versa
    
        y is upside down, on the physical level
        origin, min, and max are in physical coords
    */	
    // log2phys - input: double; return: int
    log2physX(x) {
        x *= this.scale; x += this.origin.x; 
        return Util.doubleToInteger(x);
    }
    log2physY(y) {
        y *= -this.scale; y += this.origin.y;
        return Util.doubleToInteger(y);
    }
    // input and return: Point
    log2phys(p) { return new Point(log2physX(p.x),log2physY(p.y)); }

    // phys2log - input: int; return: double
    phys2logX(x) {
        let dx=x;
        dx -= this.origin.x; 
        if (this.scale != 0) dx /= this.scale;
        return dx;
    }
    phys2logY(y) {
        let dy=y;
        dy -= this.origin.y; 
        if (this.scale != 0) dy /= -this.scale;
        return dy;
    }

    // input and return: Point
    phys2log(p) {
        return new Point(
        Util.doubleToInteger(this.phys2logX(p.x)), 
        Util.doubleToInteger(this.phys2logY(p.y)) );
    }

    drawPhysLine(ctx, A, B) { 
        ctx.moveTo(A.x,A.y);
        ctx.lineTo(B.x,B.y);
    }


    drawHorzGrids(ctx) {  
        let end0=new Point(0,0),
            end1=new Point(this.getWidth(),0);

        //  remember that it's upside down:
        //  below axis
        let limit=this.phys2logY(this.getHeight());
        for (let y=0; y>limit; y -= this.gridIncrement) {
            end0.y=this.log2physY(y); 
            end1.y=end0.y; 
            this.drawPhysLine(ctx,end0,end1);
        }
        //  above axis
        limit=this.phys2logY(0);
                    // don't start on 0: avoid weird antialiasing by not redrawing axis
        for (let y=this.gridIncrement; y<limit; y += this.gridIncrement) {
            end0.y=this.log2physY(y); 
            end1.y=end0.y; 
            this.drawPhysLine(ctx,end0,end1);
        }
    }
    drawVertGrids(ctx) {
        let end0=new Point(0,0),
            end1=new Point(0,this.getHeight());

        //  left of axis
        let limit=this.phys2logX(0);
        for (let x=0; x>limit; x -= this.gridIncrement) {
            end0.x=this.log2physX(x); 
            end1.x=end0.x; 
            this.drawPhysLine(ctx,end0,end1);
        }
        //  right of axis
        limit=this.phys2logX(this.getWidth());
        for (let x=this.gridIncrement; x<limit; x += this.gridIncrement) {
            end0.x=this.log2physX(x); 
            end1.x=end0.x; 
            this.drawPhysLine(ctx,end0,end1);
        }
    }


    fillGrey() {
        let ctx = this.canvas.getContext('2d');
        ctx.fillStyle='white';//rgb(200,200,200)'; // of 255
        ctx.fillRect(0,0,this.canvas.width, this.canvas.height);
    }

    draw() {
        if (!this.canvas.getContext) return;

        this.fillGrey(); // todo - should clear instead

        let ctx=this.canvas.getContext('2d');

        console.log('lineWidth is '+ctx.lineWidth);
        ctx.beginPath();

        this.drawHorzGrids(ctx);
        this.drawVertGrids(ctx);

        ctx.stroke();
        ctx.closePath();

    }
}
  