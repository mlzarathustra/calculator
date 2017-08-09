//  grapher 
//
//  [ported from java]
//  Copyright (c) 2017 miles zarathustra
//
let MOUSE_DEBUG=false;
let DEFAULT_SCALE=10; // 10 pixels per unit graphed
let DEFAULT_GRID_INCREMENT=1; // one grid line per unit


class Point {
    constructor(x,y) {
        this.x=x;
        this.y=y;
    }
    toString() { return this.x+','+this.y; }
}
Util = {
    doubleToInteger: function(d) { return Math.round(d); }
}

class Graph {
    constructor(canvas) {
        this.canvas=canvas;
        this.reset();
        this.axisColor='black';
        this.gridColor='#ddd';
        this.resetOrigin();
        this.dragListen();
    }
    getWidth() { return this.canvas.width; }
    getHeight() { return this.canvas.height; }


    reset() {
        this.scale=DEFAULT_SCALE;
        this.gridIncrement=DEFAULT_GRID_INCREMENT;
        this.resetOrigin();
    }

    resetOrigin() { this.origin=new Point(this.getWidth()/2, this.getHeight()/2); }

    dragListen() {
        this.canvas.onmousedown=this.onMouseDown;
        this.canvas.onmousemove=this.onMouseMove;
        this.canvas.onmouseup=this.onMouseUp;
        this.canvas.onwheel=this.onWheel;
        // they can lift their finger from the mouse outside of the window, 
        // but without this we'll think it's still down.
        this.canvas.onmouseleave=this.onMouseLeave; 
        this.canvas.graph=this;
    }

    //  inside these functions, 'this' is the canvas element.
    //
    onMouseDown(evt) { 
        if (MOUSE_DEBUG) console.log('mouseDown: '+evt.screenX+','+evt.screenY); 
        this.dragStart=new Point(evt.screenX, evt.screenY);
        this.origStart=new Point(this.graph.origin.x, this.graph.origin.y);
        this.isMouseDown=true;
    }
    onMouseUp(evt) { 
        if (MOUSE_DEBUG) console.log('mouseUP: '+evt.screenX+','+evt.screenY); 
        this.isMouseDown=false;
    }
    onMouseLeave(evt) { 
        // like "up" but we don't move the origin.
        if (MOUSE_DEBUG) console.log('mouseLeave: '+evt.screenX+','+evt.screenY); 
        this.isMouseDown=false;
    }

    onMouseMove(evt) { 
        if (this.isMouseDown) {
            if (MOUSE_DEBUG) console.log('mouseMove: '+evt.screenX+','+evt.screenY); 
            
            this.graph.origin=new Point(
                this.origStart.x + (evt.screenX - this.dragStart.x), 
                this.origStart.y - (this.dragStart.y - evt.screenY));
            if (MOUSE_DEBUG) console.log('this.origin is '+this.graph.origin);
            this.graph.draw(); 

        }
    }

    onWheel(evt) {
        if (MOUSE_DEBUG) console.log(evt);
        if (evt.deltaY < 0) this.graph.scale += 1;
        else {
            if (this.graph.scale > 1) this.graph.scale -= 1;
        }
        if (MOUSE_DEBUG) console.log('scale is '+this.graph.scale);
        this.graph.draw();
    }
    //
    //

    // these don't work the way you might think.
    // onDragStart(evt) { console.log('dragStart: '+evt); }
    // onDrag(evt) { console.log('drag: '+evt); }
    // onDragEnd(evt) { console.log('dragEnd: '+evt); }

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
        //console.log('drawPhysLine('+A+' - '+B+')');
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
    drawAxes(ctx) {
        let end0, end1;

        //	draw x axis
        end0=new Point(0,this.origin.y); 
        end1=new Point(this.getWidth(),this.origin.y);
        this.drawPhysLine(ctx,end0,end1);

        //  draw y axis
        end0=new Point(this.origin.x,0); 
        end1=new Point(this.origin.x,this.getHeight());
        this.drawPhysLine(ctx,end0,end1);
    }

    drawGrid(ctx) {
        ctx.beginPath();
        ctx.strokeStyle=this.gridColor;
        this.drawHorzGrids(ctx);
        this.drawVertGrids(ctx);
        ctx.stroke();
        ctx.closePath();

        ctx.beginPath();
        ctx.strokeStyle=this.axisColor;
        this.drawAxes(ctx);
        ctx.stroke();
        ctx.closePath();
    }


    clear(ctx) {
        ctx.fillStyle='white';//rgb(200,200,200)'; // of 255
        ctx.fillRect(0,0,this.canvas.width, this.canvas.height);
    }

    draw() {
        if (!this.canvas.getContext) return;

        let ctx=this.canvas.getContext('2d');
        this.clear(ctx);

        ctx.lineWidth=1; 
        //console.log('lineWidth is '+ctx.lineWidth);

        this.drawGrid(ctx);


    }
}
  