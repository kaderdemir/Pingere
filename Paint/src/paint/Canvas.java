/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package paint;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

/**
 *
 * @author Krietallo
 */
//The canvas used to draw
public class Canvas extends JComponent {

    // Tracking each shape in the project
    //ArrayList<MyShape> shapeList = new ArrayList<MyShape>();
    public ShapeContainer shapes = new ShapeContainer();
    public CareTaker careTaker = new CareTaker();
    private static Canvas instance = new Canvas();

    // To find the coordinates of the mouse from the begining and endng of the dragging
    Point startPoint, endPoint, endPointMove;
    public static MyShape selectedShape = null;
    Graphics2D g2;

    // Tracking index of next shape
    int nextIndex = 0;

    // This is the Stroke for the helper (dashed) rectangles when we select something
    float dash1[] = {10.0f};
    BasicStroke dashed = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

    //We use the MouseListener to determine when the mouse is pressed and released to store the start and end point of the coordinates of the shape
    public Canvas() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (Paint.selectedBut == null) {
                    return;
                }

                startPoint = e.getPoint();

                shapes.removeStrokedShape();
                repaint();

                if (Paint.selectedBut.equals("select")) {
                    selectedShape = getSelectedShape(startPoint);
                    if (selectedShape != null) {
                        shapes.bringFront(selectedShape);
                        // Unselect already selected shape, if any:
                        // Remove the dashed rectangle from shapeList
                        shapes.removeStrokedShape();
                        shapes.shapeList.add(selectedShape.drawSelectRectangle());
                        repaint();
                    }
                }

                if (Paint.selectedBut.equals("fill")) {
                    selectedShape = getSelectedShape(startPoint);
                    // User didn't press an empty space
                    if (selectedShape != null) {
                        //change in state
                        careTaker.change(shapes.saveStateToMemento(), "fill " + selectedShape.getType());
                        selectedShape.color(Paint.fillColor);
                        repaint();
                    }
                }

                if (Paint.selectedBut.equals("move")) {
                    selectedShape = getSelectedShape(startPoint);
                    if (selectedShape != null) {
                        shapes.bringFront(selectedShape);
                        //change in state
                        careTaker.change(shapes.saveStateToMemento(), "move " + selectedShape.getType());
                        // Unselect already selected shape, if any:
                        // Remove the dashed rectangle from shapeList
                        shapes.removeStrokedShape();
                        shapes.shapeList.add(selectedShape.drawSelectRectangle());
                        repaint();
                    }
                }
                if (Paint.selectedBut.equals("copy")) {
                    selectedShape = getSelectedShape(startPoint);
                    if (selectedShape == null) {
                        return;
                    }
                    shapes.bringFront(selectedShape);
                    // Unselect already selected shape, if any:
                    // Remove the dashed rectangle from shapeList
                    shapes.removeStrokedShape();
                    //change in state
                    careTaker.change(shapes.saveStateToMemento(), "copy " + selectedShape.getType());
                    MyShape shapeClone = selectedShape.copy();
                    shapes.shapeList.add(shapeClone);
                    repaint();
                }
                if (Paint.selectedBut.equals("delete")) {
                    selectedShape = getSelectedShape(startPoint);
                    if (selectedShape != null) {
                        //change in state
                        careTaker.change(shapes.saveStateToMemento(), "delete " + selectedShape.getType());
                        shapes.bringFront(selectedShape);
                        shapes.shapeList.remove(selectedShape);
                    }
                    repaint();
                }
                if (Paint.selectedBut.equals("rotate")) {
                    selectedShape = getSelectedShape(startPoint);
                    if (selectedShape == null) {
                        return;
                    }
                    //change in state
                    careTaker.change(shapes.saveStateToMemento(), "rotate " + selectedShape.getType());
                    Rotation rot = new Rotation();
                    ActionListener AL = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //selectedShape.resize(r.getW(), r.getH());
                            repaint();
                        }
                    };
                    rot.addButtonAL(AL);

                    repaint();
                }
                if (Paint.selectedBut.equals("resize")) {
                    selectedShape = getSelectedShape(startPoint);
                    if (selectedShape == null) {
                        return;
                    }
                    //change in state
                    careTaker.change(shapes.saveStateToMemento(), "resize " + selectedShape.getType());
                    Resize r = new Resize(selectedShape.getType());
                    System.out.println(r.getW() + " the dimensions " + r.getH());
                    ActionListener AL = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //selectedShape.resize(r.getW(), r.getH());
                            repaint();
                        }
                    };
                    r.addButtonAL(AL);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (Paint.selectedBut == null) {
                    return;
                }
                // We need the start and end point only if we are drawing a shape
                endPoint = e.getPoint();
                if (Paint.selectedBut.equals("move") && selectedShape != null) {
                    selectedShape.move(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
                    shapes.removeStrokedShape();
                    shapes.shapeList.add(selectedShape.drawSelectRectangle());
                    repaint();
                }
                if (!isShape(Paint.selectedBut)) {
                    endPoint = null;
                    return;
                } else {
                    //change in state
                    careTaker.change(shapes.saveStateToMemento(), "draw " + Paint.selectedBut);
                }

                repaint();

            }

        });

    }

    // Singleton design
    public static Canvas getInstance() {
        if (instance == null) {
            instance = new Canvas();
        }
        return instance;
    }

    // Overriden paint() method from JComponent
    @Override
    public void paint(Graphics g) {
        g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4));
        if (startPoint != null && endPoint != null) {
            // Create new shape from the shape cache
            ShapeCache sf = new ShapeCache();
            MyShape aShape = sf.getShape(Paint.selectedBut, startPoint.x, startPoint.y, endPoint.x, endPoint.y);

            // if a shape is not selected, go away, else...
            if (aShape != null) {
                aShape.setStrokeColor(Paint.strokeColor);
                shapes.shapeList.add(aShape);
            }

            // to avoid drawing extra stuff we will draw the shape once 
            startPoint = endPoint = null;
        }
        Color tempPaint = (Color) g2.getPaint();
        Stroke tempStroke = g2.getStroke();
        MyShape sel = null;
        for (Iterator it = shapes.getIterator(); it.hasNext();) {
            MyShape s = (MyShape) it.next();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (s.getStroke().equals(dashed) && !Paint.selectedBut.equals("select") && !Paint.selectedBut.equals("move") && !Paint.selectedBut.equals("copy")) {
                sel = s;
                continue;
            }
            s.draw(g2);
        }
        shapes.shapeList.remove(sel);

        // Because the draw(g2) methods will change the paint and stroke into the shape's fill & stroke colors
        g2.setPaint(tempPaint);
        g2.setStroke(tempStroke);
        System.out.println();
    }

    // Utility methods
    boolean isShape(String s) {
        return s.equals("triangle") || s.equals("rectangle") || s.equals("square") || s.equals("circle") || s.equals("ellipse") || s.equals("line");
    }

    // We could get rid of the index parameter at this point ... But might need it for later.
    MyShape getSelectedShape(Point p) {
        MyShape ret = null;
        for (Iterator it = shapes.getIterator(); it.hasNext();) {
            MyShape sh = (MyShape) it.next();
            if (sh.getShape().contains(p)) {
                if (sh.getStroke().equals(dashed)) {
                    continue;
                }
                ret = sh;
            }
        }
        return ret;
    }
}
