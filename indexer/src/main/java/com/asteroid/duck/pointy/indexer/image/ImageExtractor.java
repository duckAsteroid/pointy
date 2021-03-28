package com.asteroid.duck.pointy.indexer.image;

import org.apache.poi.sl.draw.Drawable;
import org.apache.poi.sl.usermodel.Slide;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

/**
 * Able to render a slide to a raster image at a specific scale
 */
public class ImageExtractor {
    private final double scale;
    private final Dimension dimension;

    public ImageExtractor(Dimension dimension) {
        this( 1.0, dimension);
    }

    public ImageExtractor(double scale, Dimension dimension) {
        this.scale = scale;
        this.dimension = scale(dimension, scale);
    }

    private static Dimension scale(Dimension d, double scale) {
        int width = (int) (d.width * scale);
        int height = (int) (d.height * scale);
        return new Dimension(width, height);
    }

    public BufferedImage render(Slide<?,?> slide) {
        // Write image
        BufferedImage img = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        // default rendering options
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(Drawable.BUFFERED_IMAGE, new WeakReference<>(img));
        graphics.scale(scale, scale);
        // draw slide onto image
        slide.draw(graphics);
        // lose the graphics context
        graphics.dispose();
        return img;
    }
}
