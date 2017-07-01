package graph;

import java.util.*;
import java.awt.*;

import form.util.*;
import eval.*;


/** 
 *    An extension of Graph that knows about an EquationPanel
 *    which contains a list of Equations.
 * <p>
 *    When asked to repaint, EvalGraph graphs them all.
 *
 * <p>
 *    Handles double buffering.
 * 
 * @see Equation
 * @see EquationPanel
 */
public class EvalGraph extends Graph {
  EquationPanel panel;
  double increment=1.0;
  
  EvalGraph() {
    eval.Globals.symbolTree.add("x", new Noun(0.0));
    eval.Globals.symbolTree.add("theta", new Noun(0.0));
  }
  
  void setEquationPanel(EquationPanel p) { panel=p; }
  
  // double buffering code
  //
  Image offScreen; 
  public void invalidate() { super.invalidate(); offScreen=null;   }
  public void update(Graphics g) { paint(g); }
  public void paint(Graphics g) {
    if (offScreen==null) 
      offScreen=createImage(getSize().width,getSize().height);
    Graphics offGraphics=offScreen.getGraphics();
    drawIt(offGraphics);
    g.drawImage(offScreen, 0,0,null);
    offGraphics.dispose();
  }
  //
  //  end double buffer
  
/**
 *   the gnarly stuff: here we pull out the equations and draw the curves.
 *   Yes, it's long.  But probably best to leave it that way for speed,
 *   since it will get called for each mouse-drag event.
 * 
 */   
 public void drawIt(Graphics g) {
    
    //  clear background (because we're double-buffering)
    Color colorWas=g.getColor();
    g.setColor(getBackground());
    Dimension d=getSize();
    g.fillRect(0,0,d.width,d.height);
    g.setColor(colorWas);
    
    //  paint grid, axes
    super.paint(g);
    
    //  ok, now the gnarly stuff.  First, get the equations.
    //
    Component[] compList=panel.getComponents();
    
    //  save some CPU overhead, by making the equation lists into arrays 
    //  rather than vectors.
    //
    //  count them
    int nCartesian=0, nPolar=0;
    for (int i=0; i<compList.length; ++i) {
      if (compList[i] instanceof Equation) {
        Equation eq=(Equation)compList[i];
        if (eq.noun == null) continue;
        if (eq.isPolar()) nPolar++;
        else nCartesian++;
      }
    }
    Noun []cartesian=new Noun[nCartesian],
           polar    =new Noun[nPolar];
    //p.l("nCartesian="+nCartesian+"; nPolar="+nPolar);
    
    //  now create the arrays & fill them
    int polarIdx=0, cartesianIdx=0;
    for (int i=0; i<compList.length; ++i) 
      if (compList[i] instanceof Equation) {
        Equation eq=(Equation)compList[i];
        if (eq.noun == null) continue;
        if (eq.isPolar()) polar[polarIdx++]=eq.noun;
        else cartesian[cartesianIdx++]=eq.noun;
      }
    
    //  plot the curves
    //
    g.setColor(Color.red);
    int height=getHeight(), width=getWidth();

    //     plot cartesian curves
    //
    if (nCartesian>0) {
      //  xPhys, yPhys == 'physical' (pixel) coordinates
      //  xLog, yLog == 'logical' coordinates
      //
      //  we draw a point from 'previous' to (xPhys,yPhys)
      //  and (generally) set previous to the current point.
      //
      double []yLog=new double[nCartesian], lastYlog=new double[nCartesian];
      int yPhys;
      Point[] previous=new Point[nCartesian];
      Noun xRef=(Noun)Globals.symbolTree.find("x").token;
      for (int xPhys=0; xPhys<=width; xPhys+=increment) {
        double xLog=phys2logX(xPhys); 
        xRef.setNumber(xLog);          // set symbol 'x' to logical x
        for (int curve=0; curve<nCartesian; ++curve) {
          
          lastYlog[curve]=yLog[curve];
          yLog[curve]=cartesian[curve].value();
          yPhys=log2physY(yLog[curve]);
          
          if (Double.isNaN(yLog[curve])) {
            previous[curve]=null;
            continue;
          }
          
          if (false) { // debug
            p.l("logical : "+xLog+","+yLog);
            p.l("physical: "+xPhys+","+yPhys);
            p.l("");
          }
        
          // clip: is current y is out of bounds?
          // if so, draw a line from previous point to the upper or lower edge
          //
          // Not mathematically correct, but good enough to look right.
          //
          if (yPhys>height) {
	    if (previous[curve] != null) {
	      drawPhysLine(g,previous[curve], new Point(xPhys,height));
	      previous[curve]=null;
	    }
	    continue;
          }
          else if (yPhys<0) {
	    if (previous[curve] != null) {
	      drawPhysLine(g,previous[curve], new Point(xPhys,0));
	      previous[curve]=null;
	    }
	    continue;
          }

          if (previous[curve]==null) {
            
            //  The previous point on this curve was out of bounds:
            //  connect this one to the upper or lower edge, 
            //  according to the last physical Y.
            //
            //  Not mathematically correct, but good enough to look right.
            // 
	    previous[curve]=new Point(xPhys,yPhys);

            //  note that the NaN case doesn't connect.  
            //  (eg. a circle whose radius is not a perfect square)
	    if (xPhys>0 && !Double.isNaN(lastYlog[curve])) {
              int lastYphys=log2physY(lastYlog[curve]);
	      if (lastYphys<=0) lastYphys=0;
	      if (lastYphys>=height) lastYphys=height;
	      drawPhysLine(g,new Point(xPhys-1, lastYphys),
	        previous[curve]);
	    }

          }
          else {
            
            //  the typical case:   previous is defined, 
            //  and is the previous in-bounds point
            //
	    Point current=new Point(xPhys,yPhys);
	    drawPhysLine(g,previous[curve],current);
	    previous[curve]=current;
          }
        
        }
        
      }
    }
    
    //
    //
    //  plot polar curves
    
    //

    if (nPolar>0) {
      panel.setPolarParameters();
      double polarMin=Math.min(panel.polarMin, panel.polarMax),
        polarMax=Math.max(panel.polarMin, panel.polarMax),
        polarIncrement=Math.abs(panel.polarIncrement);
      if (polarIncrement == 0) polarIncrement=0.1;

      double []lastXlog=new double[nPolar], lastYlog=new double[nPolar];
      Point[] previous=new Point[nPolar];
      Noun thetaRef=(Noun)Globals.symbolTree.find("theta").token;
      
      for (double theta=polarMin; theta<=polarMax; theta += polarIncrement) {
        //p.l("theta="+theta);
        thetaRef.setNumber(theta);
        for (int curve=0; curve<nPolar; ++curve) {
          double r=polar[curve].value();
          double xLog=r * Math.cos(theta), yLog = r * Math.sin(theta);
          Point current=new Point(log2physX(xLog),log2physY(yLog));

          if (Double.isNaN(xLog) || Double.isNaN(yLog) || 
            current.x < 0 || current.x > width || 
            current.y < 0 || current.y > height)  {
            
              previous[curve]=null;
              continue;
          }
            
          if (previous[curve]==null) { previous[curve]=current; continue; }
          
          drawPhysLine(g, previous[curve], current);
          previous[curve]=current;
            
          
          //  XXX For polar, the edge-connects need to be mathematically correct.
          //  The algorithm is a bit tricky, so leave that for next time. 
          
          //  need to check clipping & NaN in both X & Y
          
          //  Plus, it's alREADY slow!
          
        }
      }
    }
                        
  }
  
  
}
