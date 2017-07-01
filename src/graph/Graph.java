package graph;

import java.awt.*;
import java.awt.event.*;

import form.util.*;

/**  
 *   The basic grapher element.
 * 
 *   <p>
 *   draws axes, grid.  implements scale, and origin offset.
 *   catches mouse events to redraw at offset.
 */
public class Graph extends Canvas {

  private Point origin, oldOrigin;
  private int width,height;
  private boolean useDimensionsGiven, firstDraw=true;
  
  double scale, gridIncrement;
  Color axisColor, gridColor, bgColor;

  private boolean dragging;
  Point dragPoint;

  public static final int 
    DEFAULT_SCALE=10,
    DEFAULT_GRIDINCREMENT=1;
  
  public Graph() { this(0,0); }
  public Graph(int w, int h) {
    useDimensionsGiven=(w != 0 || h != 0);
    if (useDimensionsGiven) { width=w; height=h;
    }
    else { width=10; height=10; // else it won't get drawn.
    }
    resetOrigin();
    
    scale = DEFAULT_SCALE;
    gridIncrement = DEFAULT_GRIDINCREMENT;
    
    axisColor = Color.black;
      //(Color.green).darker();
    gridColor = Color.lightGray;

    bgColor = Color.white;

    setBackground(bgColor);
    dragging = false;
    
    addMouseListener(new MouseAdapter() {
      Cursor cursorWas;
      
      public void mouseEntered(MouseEvent evt) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
      
      public void mousePressed(MouseEvent evt) {
        //p.l("mouse down: " + evt.getPoint());
        dragging=true;
        dragPoint=evt.getPoint();
        oldOrigin=new Point(origin.x, origin.y);
        cursorWas=getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      }
      public void mouseReleased(MouseEvent evt) {
        //p.l("mouse up: " + evt.getPoint());
        dragging=false;
        dragPoint=null;
        oldOrigin=null;
        if (contains(evt.getPoint())) setCursor(cursorWas);
        else setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    });
    
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent evt) {
        //db.pf("mouse drag: " + evt.getPoint());
        int xOffset=evt.getX() - dragPoint.x,
            yOffset=evt.getY() - dragPoint.y;

        origin.x=oldOrigin.x + xOffset;
        origin.y=oldOrigin.y + yOffset;

        repaint();
      }
    });
  }
  
  double getScale() { return scale;   }
  void   setScale(double s) { scale=s;  }

  double getGridIncrement() { return gridIncrement; }
  void   setGridIncrement(double g) { gridIncrement=g; }
  
  public int getWidth() { 
    if (useDimensionsGiven) return width;
    return getSize().width;
  }
  public int getHeight() {
    if (useDimensionsGiven) return height;
    return getSize().height;
  }

  void resetOrigin() { origin=new Point(getWidth()/2, getHeight()/2); }

  /**  
   *    translations: 
  	logical to physical, & vice versa
  
  	y is upside down, on the physical level
  	origin, min, and max are in physical coords
  */	
  int log2physX(double x) {
    x *= scale; x += origin.x; 
    return Util.doubleToInteger(x);
  }
  int log2physY(double y) {
    y *= -scale; y += origin.y;
    return Util.doubleToInteger(y);
  }
  Point log2phys(Point p) { return new Point(log2physX(p.x),log2physY(p.y)); }

  double phys2logX(int x) {
    double dx=x;
    dx -= origin.x; 
    if (scale != 0) dx /= scale;
    return dx;
  }
  double phys2logY(int y) {
    double dy=y;
    dy -= origin.y; 
    if (scale != 0) dy /= -scale;
    return dy;
  }

  Point phys2log(Point p) {
    return new Point(
      Util.doubleToInteger(phys2logX(p.x)), 
      Util.doubleToInteger(phys2logY(p.y)) );
  }
  
  public Dimension getPreferredSize() { return new Dimension(width,height);  }
  public Dimension getMinimumSize()   {   return getPreferredSize();   }

  /**  draws a line between the 2 points given, 
   *   using logical coordinates.
   */
  void drawLine(Graphics g, Point a, Point b) {
    a=log2phys(a); b=log2phys(b);
    g.drawLine(a.x,a.y , b.x,b.y);
  }
  void drawPhysLine(Graphics g, Point a, Point b) {
    g.drawLine(a.x,a.y , b.x,b.y);
  }

  void drawAxes(Graphics g) {
    Color colorWas=g.getColor();
    g.setColor(axisColor);
    Point end0, end1;

    //	draw x axis
    end0=new Point(0,origin.y); end1=new Point(getWidth(),origin.y);
    drawPhysLine(g,end0,end1);

    //  draw y axis
    end0=new Point(origin.x,0); end1=new Point(origin.x,getHeight());
    drawPhysLine(g,end0,end1);

    g.setColor(colorWas);
  }

  void drawHorzGrids(Graphics g) {
    Point 
      end0=new Point(0,0),
      end1=new Point(getWidth(),0);

    //  remember that it's upside down:
    //  below axis
    double limit=phys2logY(getHeight());
    for (double y=0; y>limit; y -= gridIncrement) {
      end0.y=log2physY(y); 
      end1.y=end0.y; 
      drawPhysLine(g,end0,end1);
    }
    //  above axis
    limit=phys2logY(0);
    for (double y=0; y<limit; y += gridIncrement) {
      end0.y=log2physY(y); 
      end1.y=end0.y; 
      drawPhysLine(g,end0,end1);
    }
  }

  void drawVertGrids(Graphics g) {
    Point 
      end0=new Point(0,0),
      end1=new Point(0,getHeight());

    //  left of axis
    double limit=phys2logX(0);
    for (double x=0; x>limit; x -= gridIncrement) {
      end0.x=log2physX(x); 
      end1.x=end0.x; 
      drawPhysLine(g,end0,end1);
    }
    //  right of axis
    limit=phys2logX(getWidth());
    for (double x=0; x<limit; x += gridIncrement) {
      end0.x=log2physX(x); 
      end1.x=end0.x; 
      drawPhysLine(g,end0,end1);
    }
  }

  /*  XXX -- the grids and the axes will get drawn outside
   *   the width x height area, if the layout manager resizes
   *   us larger than width x height and either axis has been dragged
   *   outside. 
   * 
   *   <p>
   *   Probably it's best to just leave it -- the boundary checks 
   *   would cost more CPU cycles than they're probably worth.
   *   
   */
  void drawGrids(Graphics g) {
    Color colorWas=g.getColor();
    g.setColor(gridColor);

    drawHorzGrids(g); 
    drawVertGrids(g);

    g.setColor(colorWas);
  }
  
  /*  XXX -- double buffer
   */
  public void paint(Graphics g) {
    if (firstDraw) {
      resetOrigin();
      firstDraw=false;
    }
    
    drawGrids(g);
    drawAxes(g);
  }
}

