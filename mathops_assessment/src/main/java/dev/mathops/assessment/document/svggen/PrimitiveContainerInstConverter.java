package dev.mathops.assessment.document.svggen;

import dev.mathops.assessment.document.BoundingRect;
import dev.mathops.assessment.document.EArcFillStyle;
import dev.mathops.assessment.document.EStrokeCap;
import dev.mathops.assessment.document.EStrokeJoin;
import dev.mathops.assessment.document.FillStyle;
import dev.mathops.assessment.document.StrokeStyle;
import dev.mathops.assessment.document.inst.AbstractPrimitiveInst;
import dev.mathops.assessment.document.inst.DocDrawingInst;
import dev.mathops.assessment.document.inst.DocGraphXYInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.document.inst.DocPrimitiveArcInst;
import dev.mathops.assessment.document.inst.DocPrimitiveFormulaInst;
import dev.mathops.assessment.document.inst.DocPrimitiveLineInst;
import dev.mathops.assessment.document.inst.DocPrimitiveOvalInst;
import dev.mathops.assessment.document.inst.DocPrimitivePolygonInst;
import dev.mathops.assessment.document.inst.DocPrimitivePolylineInst;
import dev.mathops.assessment.document.inst.DocPrimitiveRasterInst;
import dev.mathops.assessment.document.inst.DocPrimitiveRectangleInst;
import dev.mathops.assessment.document.inst.DocPrimitiveSpanInst;
import dev.mathops.assessment.document.inst.DocPrimitiveTextInst;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.xml.XmlEscaper;

/**
 * Converts a subclasses of {@code AbstractPrimitiveContainerInst} into SVG.  "span" content of such a container is
 * not permitted to include inputs.
 */
public enum PrimitiveContainerInstConverter {
    ;

    /**
     * Given a {@code DocDrawingInst}, generates the corresponding SVG.
     *
     * @param drawing the {@code DocDrawingInst}
     * @param ambientStyle the style (font settings) of surrounding HTML, to use as a default if the drawing does not
     *                     specify its own style
     * @return the generated SVG
     */
    public static String convertDocDrawingInst(final DocDrawingInst drawing, final DocObjectInstStyle ambientStyle) {

        final HtmlBuilder svg = new HtmlBuilder(1000);

        final int width = Math.max(0, drawing.getWidth());
        final int height = Math.max(0, drawing.getHeight());

        svg.add("<svg width='", Integer.toString(width), "' height='", Integer.toString(height), "'>");

        // If there is a background color, draw that first
        final String bgColor = drawing.getBgColorName();
        if (bgColor != null) {
            svg.add("<rect x='0' y='0' width='", Integer.toString(width), "' height='", Integer.toString(height),
                    "' fill='", bgColor, "'/>");
        }

        for (final AbstractPrimitiveInst primitive : drawing.getPrimitives()) {

            if (primitive instanceof final DocPrimitiveArcInst arc) {
                emitArc(svg, arc);
            } else if (primitive instanceof final DocPrimitiveLineInst line) {
                emitLine(svg, line);
            } else if (primitive instanceof final DocPrimitiveOvalInst oval) {
                emitOval(svg, oval);
            } else if (primitive instanceof final DocPrimitivePolygonInst polygon) {
                emitPolygon(svg, polygon);
            } else if (primitive instanceof final DocPrimitivePolylineInst polyline) {
                emitPolyline(svg, polyline);
            } else if (primitive instanceof final DocPrimitiveRasterInst raster) {
                emitRaster(svg, raster);
            } else if (primitive instanceof final DocPrimitiveRectangleInst rect) {
                emitRectangle(svg, rect);
            } else if (primitive instanceof final DocPrimitiveSpanInst span) {
                emitSpan(svg, span);
            } else if (primitive instanceof final DocPrimitiveTextInst text) {
                emitText(svg, text);
            }
        }

        // If there is a border, draw it on top of all primitives
        final StrokeStyle border = drawing.getBorder();
        if (border != null) {
            drawBorder(svg, width, height, border);
        }

        svg.add("</svg>");

        return svg.toString();
    }

    /**
     * Given a {@code DocGraphXYInst}, generates the corresponding SVG.
     *
     * @param graph the {@code DocGraphXYInst}
     * @param ambientStyle the style (font settings) of surrounding HTML, to use as a default if the drawing does not
     *                     specify its own style
     * @return the generated SVG
     */
    public static String convertDocGraphXYInst(final DocGraphXYInst graph, final DocObjectInstStyle ambientStyle) {

        final HtmlBuilder svg = new HtmlBuilder(1000);

        final int width = graph.getWidth();
        final int height = graph.getHeight();

        svg.add("<svg width='", Integer.toString(width), "' height='", Integer.toString(height), "'>");

        // If there is a background color, draw that first
        final String bgColor = graph.getBgColorName();
        if (bgColor != null) {
            svg.add("<rect x='0' y='0' width='", Integer.toString(width), "' height='", Integer.toString(height),
                    "' fill='", bgColor, "'/>");
        }

        // TODO: Draw axes beneath primitives (which will include the function primitives)

        for (final AbstractPrimitiveInst primitive : graph.getPrimitives()) {

            if (primitive instanceof final DocPrimitiveArcInst arc) {
                emitArc(svg, arc);
            } else if (primitive instanceof final DocPrimitiveLineInst line) {
                emitLine(svg, line);
            } else if (primitive instanceof final DocPrimitiveOvalInst oval) {
                emitOval(svg, oval);
            } else if (primitive instanceof final DocPrimitivePolygonInst polygon) {
                emitPolygon(svg, polygon);
            } else if (primitive instanceof final DocPrimitivePolylineInst polyline) {
                emitPolyline(svg, polyline);
            } else if (primitive instanceof final DocPrimitiveRasterInst raster) {
                emitRaster(svg, raster);
            } else if (primitive instanceof final DocPrimitiveRectangleInst rect) {
                emitRectangle(svg, rect);
            } else if (primitive instanceof final DocPrimitiveSpanInst span) {
                emitSpan(svg, span);
            } else if (primitive instanceof final DocPrimitiveTextInst text) {
                emitText(svg, text);
            } else if (primitive instanceof final DocPrimitiveFormulaInst formula) {
                emitFormula(svg, formula);
            }
        }

        // If there is a border, draw it on top of all primitives
        final StrokeStyle border = graph.getBorder();
        if (border != null) {
            drawBorder(svg, width, height, border);
        }

        svg.add("</svg>");

        return svg.toString();
    }

    /**
     * Draws a border using a specified style.  The rectangle is calculated so the border will fall on the edges of the
     * drawing.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param width the drawing width, in pixels
     * @param height the drawing height, in pixels
     * @param border the border stroke style
     */
    private static void drawBorder(final HtmlBuilder svg, final int width, final int height, final StrokeStyle border) {

        final double strokeWidth = border.getStrokeWidth();
        final double xy = strokeWidth * 0.5;
        final double w = (double)width - strokeWidth;
        final double h = (double)height - strokeWidth;

        drawRect(svg, xy, xy, w, h, border, null);
    }

    /**
     * Draws a rectangle using a specified stroke style.  Coordinates and width/height define an outline on which
     * the stroke is centered.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param x the x coordinate of the top left corner
     * @param y the y coordinate of the top left corner
     * @param width the rectangle width, in pixels
     * @param height the rectangle height, in pixels
     * @param stroke the stroke style
     * @param fill the fill style
     */
    private static void drawRect(final HtmlBuilder svg, final double x, final double y, final double width,
                                 final double height, final StrokeStyle stroke, final FillStyle fill) {

        final double strokeWidth = stroke.getStrokeWidth();
        final double alpha = stroke.getAlpha();

        if (strokeWidth > 0.0 && alpha > 0.0) {
            svg.add("<rect");
            svg.addAttribute("x", Double.toString(x), 0);
            svg.addAttribute("y", Double.toString(y), 0);
            svg.addAttribute("width", Double.toString(width), 0);
            svg.addAttribute("height", Double.toString(height), 0);

            if (fill != null) {
                final double fillAlpha = fill.getAlpha();

                if (fillAlpha > 0.0) {
                    svg.addAttribute("fill", fill.getFillColorName(), 0);
                    if (fillAlpha < 0.99) {
                        svg.addAttribute("fill-opacity", Double.toString(fillAlpha), 0);
                    }
                }
            }

            svg.addAttribute("stroke", stroke.getStrokeColorName(), 0);
            svg.addAttribute("stroke-width", Double.toString(strokeWidth), 0);

            final EStrokeCap cap = stroke.getCap();
            if (cap== EStrokeCap.ROUND) {
                svg.addAttribute("stroke-linecap", "round", 0);
            } else if (cap == EStrokeCap.SQUARE) {
                svg.addAttribute("stroke-linecap", "square", 0);
            }

            final EStrokeJoin join = stroke.getJoin();
            if (join == EStrokeJoin.ROUND) {
                svg.addAttribute("stroke-linejoin", "round", 0);
            } else if (join == EStrokeJoin.BEVEL) {
                svg.addAttribute("stroke-linejoin", "bevel", 0);
            }

            // NOTE: For square corners, miter limit won't matter unless it's less than sqrt(2)
            final float miterlimit = stroke.getMiterLimit();
            if (miterlimit < 1.414f) {
                svg.addAttribute("stroke-miterlimit", Float.toString(miterlimit), 0);
            }

            if (alpha < 0.99) {
                svg.addAttribute("stroke-opacity", Double.toString(alpha), 0);
            }

            final float[] dash = stroke.getDash();
            final int len = dash.length;
            if (len > 0) {
                final HtmlBuilder pattern = new HtmlBuilder(20);
                pattern.add(dash[0]);
                for (int i = 1; i < len; ++i) {
                    pattern.add(',');
                    pattern.add(dash[i]);
                }
                svg.addAttribute("stroke-dasharray", pattern.toString(), 0);
            }

            svg.add("/>");
        }
    }

    /**
     * Draws an ellipse using a specified stroke style.  Coordinates and width/height define an outline on which
     * the stroke is centered.
     *
     * @param svg    the {@code HtmlBuilder} to which to append
     * @param x      the x coordinate of the top left corner
     * @param y      the y coordinate of the top left corner
     * @param width  the rectangle width, in pixels
     * @param height the rectangle height, in pixels
     * @param stroke the stroke style
     * @param fill   the fill style
     */
    private static void drawEllipse(final HtmlBuilder svg, final double x, final double y, final double width,
                                    final double height, final StrokeStyle stroke, final FillStyle fill) {

        final double strokeWidth = stroke.getStrokeWidth();
        final double alpha = stroke.getAlpha();

        if (strokeWidth > 0.0 && alpha > 0.0) {
            svg.add("<ellipse");
            svg.addAttribute("x", Double.toString(x), 0);
            svg.addAttribute("y", Double.toString(y), 0);
            svg.addAttribute("width", Double.toString(width), 0);
            svg.addAttribute("height", Double.toString(height), 0);

            if (fill != null) {
                final double fillAlpha = fill.getAlpha();

                if (fillAlpha > 0.0) {
                    svg.addAttribute("fill", fill.getFillColorName(), 0);
                    if (fillAlpha < 0.99) {
                        svg.addAttribute("fill-opacity", Double.toString(fillAlpha), 0);
                    }
                }
            }

            svg.addAttribute("stroke", stroke.getStrokeColorName(), 0);
            svg.addAttribute("stroke-width", Double.toString(strokeWidth), 0);

            final EStrokeCap cap = stroke.getCap();
            if (cap== EStrokeCap.ROUND) {
                svg.addAttribute("stroke-linecap", "round", 0);
            } else if (cap == EStrokeCap.SQUARE) {
                svg.addAttribute("stroke-linecap", "square", 0);
            }

            final EStrokeJoin join = stroke.getJoin();
            if (join == EStrokeJoin.ROUND) {
                svg.addAttribute("stroke-linejoin", "round", 0);
            } else if (join == EStrokeJoin.BEVEL) {
                svg.addAttribute("stroke-linejoin", "bevel", 0);
            }

            // NOTE: For square corners, miter limit won't matter unless it's less than sqrt(2)
            final float miterlimit = stroke.getMiterLimit();
            if (miterlimit < 1.414f) {
                svg.addAttribute("stroke-miterlimit", Float.toString(miterlimit), 0);
            }

            if (alpha < 0.99) {
                svg.addAttribute("stroke-opacity", Double.toString(alpha), 0);
            }

            final float[] dash = stroke.getDash();
            final int len = dash.length;
            if (len > 0) {
                final HtmlBuilder pattern = new HtmlBuilder(20);
                pattern.add(dash[0]);
                for (int i = 1; i < len; ++i) {
                    pattern.add(',');
                    pattern.add(dash[i]);
                }
                svg.addAttribute("stroke-dasharray", pattern.toString(), 0);
            }

            svg.add("/>");
        }
    }

    /**
     * Emits the SVG for an erc.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param arc the arc
     */
    private static void emitArc(final HtmlBuilder svg, final DocPrimitiveArcInst arc) {

        final BoundingRect bounds = arc.getBounds();

        final double arcAngle = arc.getArcAngle();
        if (arcAngle < -360.0 || arcAngle > 360.0) {
            drawEllipse(svg, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), arc.getStrokeStyle(),
                    arc.getFillStyle());
        } else {
            // NOTE that "positive" angle is clockwise from horizontal in the SVG coordinate system.

            final double startAngle = arc.getStartAngle();

            final double cx = (bounds.getX() + bounds.getWidth()) * 0.5;
            final double cy = (bounds.getY() + bounds.getHeight()) * 0.5;
            final double rx = bounds.getWidth() * 0.5;
            final double ry = bounds.getHeight() * 0.5;

            final double startAngleRad = Math.toRadians(startAngle);
            final double endAngleRad = Math.toRadians(startAngle + arcAngle);
            final double startX = cx + rx * StrictMath.cos(startAngleRad);
            final double startY = cx + ry * StrictMath.sin(startAngleRad);
            final double endX = cx + rx * StrictMath.cos(endAngleRad);
            final double endY = cx + ry * StrictMath.sin(endAngleRad);

            // If "arc angle" is positive, the "sweep flag" is 0
            final String sweepFlag = arcAngle >= 0.0 ? "0" : "1";
            final String largeArc = (arcAngle > 180.0 || arcAngle < -180.0) ? "1" : "0";

            // If there is a fill, do the fill before the stroke
            final FillStyle fill = arc.getFillStyle();
            if (fill != null) {
                final double fillAlpha = fill.getAlpha();

                if (fillAlpha > 0.0) {
                    final EArcFillStyle arcFill = arc.getArcFill();

                    svg.add("<path d='M", Double.toString(startX), ",", Double.toString(startY), " A",
                            Double.toString(rx), ",", Double.toString(ry), ",0,", largeArc, ",", sweepFlag, ",",
                            Double.toString(endX), ",", Double.toString(endY));

                    if (arcFill == EArcFillStyle.PIE) {
                        svg.add(" L", Double.toString(cx), ",", Double.toString(cy), " Z");
                    } else if (arcFill == EArcFillStyle.CHORD) {
                        svg.add(" Z");
                    }
                    svg.add("' fill='", fill.getFillColorName(), "'");
                    if (fillAlpha < 0.99) {
                        svg.add("' fill-opacity='", Double.toString(fillAlpha), "'");
                    }
                    svg.add("/>");
                }
            }

            final StrokeStyle stroke = arc.getStrokeStyle();
            if (stroke != null) {
                final double strokeWidth = stroke.getStrokeWidth();
                final double alpha = stroke.getAlpha();

                if (strokeWidth > 0.0 && alpha > 0.0) {
                    svg.add("<path d='M", Double.toString(startX), ",", Double.toString(startY), " A",
                            Double.toString(rx), ",", Double.toString(ry), ",0,", largeArc, ",", sweepFlag, ",",
                            Double.toString(endX), ",", Double.toString(endY));

                    svg.addAttribute("stroke", stroke.getStrokeColorName(), 0);
                    svg.addAttribute("stroke-width", Double.toString(strokeWidth), 0);

                    final EStrokeCap cap = stroke.getCap();
                    if (cap == EStrokeCap.ROUND) {
                        svg.addAttribute("stroke-linecap", "round", 0);
                    } else if (cap == EStrokeCap.SQUARE) {
                        svg.addAttribute("stroke-linecap", "square", 0);
                    }

                    final EStrokeJoin join = stroke.getJoin();
                    if (join == EStrokeJoin.ROUND) {
                        svg.addAttribute("stroke-linejoin", "round", 0);
                    } else if (join == EStrokeJoin.BEVEL) {
                        svg.addAttribute("stroke-linejoin", "bevel", 0);
                    }

                    // NOTE: Miter limit is not needed for an arc

                    if (alpha < 0.99) {
                        svg.addAttribute("stroke-opacity", Double.toString(alpha), 0);
                    }

                    final float[] dash = stroke.getDash();
                    final int len = dash.length;
                    if (len > 0) {
                        final HtmlBuilder pattern = new HtmlBuilder(20);
                        pattern.add(dash[0]);
                        for (int i = 1; i < len; ++i) {
                            pattern.add(',');
                            pattern.add(dash[i]);
                        }
                        svg.addAttribute("stroke-dasharray", pattern.toString(), 0);
                    }
                    svg.add("/>");
                }
            }
        }
    }

    /**
     * Emits the SVG for a line.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param line the line
     */
    private static void emitLine(final HtmlBuilder svg, final DocPrimitiveLineInst line) {

        final StrokeStyle stroke = line.getStrokeStyle();
        if (stroke != null) {
            final double strokeWidth = stroke.getStrokeWidth();
            final double alpha = stroke.getAlpha();

            if (strokeWidth > 0.0 && alpha > 0.0) {

                final BoundingRect bounds = line.getBounds();
                final double x1 = bounds.getX();
                final double y1 = bounds.getY();
                final double x2 = x1 + bounds.getWidth();
                final double y2 = y1 + bounds.getHeight();

                svg.add("<line x1='", Double.toString(x1), "' y1='", Double.toString(y1), "' x2='",
                        Double.toString(x2), "' y2='", Double.toString(y2));

                svg.addAttribute("stroke", stroke.getStrokeColorName(), 0);
                svg.addAttribute("stroke-width", Double.toString(strokeWidth), 0);

                final EStrokeCap cap = stroke.getCap();
                if (cap == EStrokeCap.ROUND) {
                    svg.addAttribute("stroke-linecap", "round", 0);
                } else if (cap == EStrokeCap.SQUARE) {
                    svg.addAttribute("stroke-linecap", "square", 0);
                }

                final EStrokeJoin join = stroke.getJoin();
                if (join == EStrokeJoin.ROUND) {
                    svg.addAttribute("stroke-linejoin", "round", 0);
                } else if (join == EStrokeJoin.BEVEL) {
                    svg.addAttribute("stroke-linejoin", "bevel", 0);
                }

                // NOTE: Miter limit is not needed for an arc

                if (alpha < 0.99) {
                    svg.addAttribute("stroke-opacity", Double.toString(alpha), 0);
                }

                final float[] dash = stroke.getDash();
                final int len = dash.length;
                if (len > 0) {
                    final HtmlBuilder pattern = new HtmlBuilder(20);
                    pattern.add(dash[0]);
                    for (int i = 1; i < len; ++i) {
                        pattern.add(',');
                        pattern.add(dash[i]);
                    }
                    svg.addAttribute("stroke-dasharray", pattern.toString(), 0);
                }
                svg.add("/>");
            }
        }
    }

    /**
     * Emits the SVG for an oval.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param oval the oval
     */
    private static void emitOval(final HtmlBuilder svg, final DocPrimitiveOvalInst oval) {

        final BoundingRect bounds = oval.getBounds();
        final double x = bounds.getX();
        final double y = bounds.getY();
        final double w = bounds.getWidth();
        final double h = bounds.getHeight();

        final StrokeStyle stroke = oval.getStrokeStyle();
        final FillStyle fill = oval.getFillStyle();

        drawEllipse(svg, x, y, w, h, stroke, fill);
    }

    /**
     * Emits the SVG for a polygon.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param polygon the polygon
     */
    private static void emitPolygon(final HtmlBuilder svg, final DocPrimitivePolygonInst polygon) {

        final double[] x = polygon.getX();
        final double[] y = polygon.getY();
        final int count = x.length;

        final StrokeStyle stroke = polygon.getStrokeStyle();
        final FillStyle fill = polygon.getFillStyle();

        svg.add("<polygon points='", Double.toString(x[0]), ",", Double.toString(y[0]));
        for (int i = 1; i < count; ++i) {
            svg.add(" ", Double.toString(x[i]), ",", Double.toString(y[i]));
        }
        svg.add("'");

        if (fill != null) {
            final double fillAlpha = fill.getAlpha();

            if (fillAlpha > 0.0) {
                svg.addAttribute("fill", fill.getFillColorName(), 0);
                if (fillAlpha < 0.99) {
                    svg.addAttribute("fill-opacity", Double.toString(fillAlpha), 0);
                }
            }
        }

        if (stroke != null) {
            final double strokeWidth = stroke.getStrokeWidth();
            final double alpha = stroke.getAlpha();

            if (strokeWidth > 0.0 && alpha > 0.0) {
                svg.addAttribute("stroke", stroke.getStrokeColorName(), 0);
                svg.addAttribute("stroke-width", Double.toString(strokeWidth), 0);

                final EStrokeCap cap = stroke.getCap();
                if (cap == EStrokeCap.ROUND) {
                    svg.addAttribute("stroke-linecap", "round", 0);
                } else if (cap == EStrokeCap.SQUARE) {
                    svg.addAttribute("stroke-linecap", "square", 0);
                }

                final EStrokeJoin join = stroke.getJoin();
                if (join == EStrokeJoin.ROUND) {
                    svg.addAttribute("stroke-linejoin", "round", 0);
                } else if (join == EStrokeJoin.BEVEL) {
                    svg.addAttribute("stroke-linejoin", "bevel", 0);
                }

                // NOTE: For square corners, miter limit won't matter unless it's less than sqrt(2)
                final float miterlimit = stroke.getMiterLimit();
                if (miterlimit < 1.414f) {
                    svg.addAttribute("stroke-miterlimit", Float.toString(miterlimit), 0);
                }

                if (alpha < 0.99) {
                    svg.addAttribute("stroke-opacity", Double.toString(alpha), 0);
                }

                final float[] dash = stroke.getDash();
                final int len = dash.length;
                if (len > 0) {
                    final HtmlBuilder pattern = new HtmlBuilder(20);
                    pattern.add(dash[0]);
                    for (int i = 1; i < len; ++i) {
                        pattern.add(',');
                        pattern.add(dash[i]);
                    }
                    svg.addAttribute("stroke-dasharray", pattern.toString(), 0);
                }
            }
        }

        svg.add("/>");
    }

    /**
     * Emits the SVG for a polyline.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param polyline the polyline
     */
    private static void emitPolyline(final HtmlBuilder svg, final DocPrimitivePolylineInst polyline) {

        final double[] x = polyline.getX();
        final double[] y = polyline.getY();
        final int count = x.length;

        final StrokeStyle stroke = polyline.getStrokeStyle();

        svg.add("<polygon points='", Double.toString(x[0]), ",", Double.toString(y[0]));
        for (int i = 1; i < count; ++i) {
            svg.add(" ", Double.toString(x[i]), ",", Double.toString(y[i]));
        }
        svg.add("'");

        if (stroke != null) {
            final double strokeWidth = stroke.getStrokeWidth();
            final double alpha = stroke.getAlpha();

            if (strokeWidth > 0.0 && alpha > 0.0) {
                svg.addAttribute("stroke", stroke.getStrokeColorName(), 0);
                svg.addAttribute("stroke-width", Double.toString(strokeWidth), 0);

                final EStrokeCap cap = stroke.getCap();
                if (cap == EStrokeCap.ROUND) {
                    svg.addAttribute("stroke-linecap", "round", 0);
                } else if (cap == EStrokeCap.SQUARE) {
                    svg.addAttribute("stroke-linecap", "square", 0);
                }

                final EStrokeJoin join = stroke.getJoin();
                if (join == EStrokeJoin.ROUND) {
                    svg.addAttribute("stroke-linejoin", "round", 0);
                } else if (join == EStrokeJoin.BEVEL) {
                    svg.addAttribute("stroke-linejoin", "bevel", 0);
                }

                // NOTE: For square corners, miter limit won't matter unless it's less than sqrt(2)
                final float miterlimit = stroke.getMiterLimit();
                if (miterlimit < 1.414f) {
                    svg.addAttribute("stroke-miterlimit", Float.toString(miterlimit), 0);
                }

                if (alpha < 0.99) {
                    svg.addAttribute("stroke-opacity", Double.toString(alpha), 0);
                }

                final float[] dash = stroke.getDash();
                final int len = dash.length;
                if (len > 0) {
                    final HtmlBuilder pattern = new HtmlBuilder(20);
                    pattern.add(dash[0]);
                    for (int i = 1; i < len; ++i) {
                        pattern.add(',');
                        pattern.add(dash[i]);
                    }
                    svg.addAttribute("stroke-dasharray", pattern.toString(), 0);
                }
            }
        }

        svg.add("/>");
    }

    /**
     * Emits the SVG for a raster.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param raster the raster
     */
    private static void emitRaster(final HtmlBuilder svg, final DocPrimitiveRasterInst raster) {

        final BoundingRect bounds = raster.getBounds();
        final double x = bounds.getX();
        final double y = bounds.getY();
        final double w = bounds.getWidth();
        final double h = bounds.getHeight();

        svg.add("<image href='", raster.getSource(), "' x='", Double.toString(x), "' y='", Double.toString(y),
                "' width='", Double.toString(w), "' height='", Double.toString(h), "'/>");
    }

    /**
     * Emits the SVG for a rectangle.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param rect the rectangle
     */
    private static void emitRectangle(final HtmlBuilder svg, final DocPrimitiveRectangleInst rect) {

        final BoundingRect bounds = rect.getBounds();
        final double x = bounds.getX();
        final double y = bounds.getY();
        final double w = bounds.getWidth();
        final double h = bounds.getHeight();

        drawRect(svg, x, y, w, h, rect.getStrokeStyle(), rect.getFillStyle());
    }

    /**
     * Emits the SVG for a span.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param span the span
     */
    private static void emitSpan(final HtmlBuilder svg, final DocPrimitiveSpanInst span) {

        // TODO:
    }

    /**
     * Emits the SVG for a text.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param text the text
     */
    private static void emitText(final HtmlBuilder svg, final DocPrimitiveTextInst text) {

        final double alpha = text.getAlpha();
        final DocObjectInstStyle style = text.getStyle();
        final int fontStyle = style.fontStyle;

        final boolean isVisible = (fontStyle & DocObjectInstStyle.HIDDEN) != DocObjectInstStyle.HIDDEN;

        if (alpha > 0.0 && isVisible) {
            svg.add("<text x='", Double.toString(text.getX()), "' y='", Double.toString(text.getY()),
                    "' color='", style.colorName, "' font-family='", style.fontName, "' font-size='",
                    Float.toString(style.fontSize), "pt'");

            final boolean isItalic = (fontStyle & DocObjectInstStyle.ITALIC) == DocObjectInstStyle.ITALIC;
            if (isItalic) {
                svg.add(" font-style='italic'");
            }

            final boolean isBold = (fontStyle & DocObjectInstStyle.BOLD) == DocObjectInstStyle.BOLD;
            if (isBold) {
                svg.add(" font-weight='bold'");
            }

            final boolean isUnderline = (fontStyle & DocObjectInstStyle.UNDERLINE) == DocObjectInstStyle.UNDERLINE;
            final boolean isOverline = (fontStyle & DocObjectInstStyle.OVERLINE) == DocObjectInstStyle.OVERLINE;
            final boolean isStrikethrough = (fontStyle & DocObjectInstStyle.STRIKETHROUGH)
                    == DocObjectInstStyle.STRIKETHROUGH;

            if (isUnderline || isOverline || isStrikethrough) {
                final HtmlBuilder decoration = new HtmlBuilder(40);
                if (isUnderline) {
                    decoration.add("underline ");
                }
                if (isOverline) {
                    decoration.add("overline ");
                }
                if (isStrikethrough) {
                    decoration.add("line-through");
                }
                svg.add(" text-decoration='", decoration.toString(), "'");
            }

            final boolean isBoxed = (fontStyle & DocObjectInstStyle.BOXED) == DocObjectInstStyle.BOXED;
            if (isBoxed) {
                svg.add(" outline='solid'");
            }

            svg.add(">", XmlEscaper.escape(text.getText()), "</text>");
        }
    }

    /**
     * Emits the SVG for a formula.
     *
     * @param svg the {@code HtmlBuilder} to which to append
     * @param formula the formula
     */
    private static void emitFormula(final HtmlBuilder svg, final DocPrimitiveFormulaInst formula) {

        // TODO:
    }
}
