package com.extractpdf4j.helpers;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.global.opencv_imgcodecs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * ImagePdfUtils
 *
 * <p>Utility helpers for working with PDF pages as images and converting between
 * AWT {@link BufferedImage} and OpenCV {@link Mat} types. These functions are
 * used by lattice/OCR flows where pages are rasterized and processed with OpenCV.</p>
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Rendering is delegated to PDFBox's {@link PDFRenderer}.</li>
 *   <li>Conversion to {@link Mat} is implemented via a temporary PNG round‑trip for
 *       broad compatibility with image types; this keeps logic simple and avoids
 *       assumptions about pixel formats.</li>
 *   <li>Binarization uses OpenCV adaptive thresholding tuned for line detection
 *       (inverted binary for morphological line ops).</li>
 * </ul>
 *
 * <p>This class is non‑instantiable.</p>
 *
 * @author Mehuli Mukherjee
 * @since 2025
 */
public final class ImagePdfUtils {
    private ImagePdfUtils() { }

    /**
     * Renders a single PDF page to a {@link BufferedImage} at the requested DPI.
     *
     * <p>The output is rendered as {@link ImageType#BINARY}, which is generally
     * suitable for downstream line/edge detection. If grayscale or RGB is needed,
     * change the {@code ImageType} accordingly in the implementation.</p>
     *
     * @param doc            an open {@link PDDocument}
     * @param pageIndexZero  zero‑based page index (0 = first page)
     * @param dpi            dots per inch to render at (e.g., 300f for scans)
     * @return a binary {@link BufferedImage} of the page
     * @throws IOException if rendering fails
     */
    public static BufferedImage renderPage(PDDocument doc, int pageIndexZero, float dpi) throws IOException {
        PDFRenderer renderer = new PDFRenderer(doc);
        return renderer.renderImageWithDPI(pageIndexZero, dpi, ImageType.BINARY);
    }

    /**
     * Converts a {@link BufferedImage} to an OpenCV {@link Mat} (grayscale) via a
     * temporary PNG encode/decode round‑trip.
     *
     * <p>This method favors robustness over raw speed; it handles arbitrary
     * {@link BufferedImage} formats without manual channel/buffer shuffling.
     * The temporary file is deleted on success; on failure, a {@link RuntimeException}
     * is thrown wrapping the {@link IOException}.</p>
     *
     * @param img source buffered image (any color model)
     * @return an 8‑bit single‑channel {@link Mat} ({@code CV_8UC1})
     */
    public static Mat bufferedToMat(BufferedImage img) {
        try {
            File tmp = File.createTempFile("extractpdf4j_", ".png");
            javax.imageio.ImageIO.write(img, "png", tmp);
            Mat m = opencv_imgcodecs.imread(tmp.getAbsolutePath(), opencv_imgcodecs.IMREAD_GRAYSCALE);
            // Best‑effort cleanup of the temporary file.
            tmp.delete();
            return m;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Binarizes a grayscale image for line detection using adaptive thresholding.
     *
     * <p>Applies {@code adaptiveThreshold} with {@code ADAPTIVE_THRESH_MEAN_C} and
     * {@code THRESH_BINARY_INV} to emphasize lines/edges against background noise.
     * The block size ({@code 15}) and constant ({@code 10}) are conservative defaults
     * for document scans; adjust as needed for different lighting/contrast.</p>
     *
     * @param gray input grayscale image ({@code CV_8UC1})
     * @return a binary (inverted) {@link Mat} suitable for morphological line ops
     */
    public static Mat binarizeForLines(Mat gray) {
        Mat bw = new Mat();
        adaptiveThreshold(gray, bw, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 15, 10);
        return bw;
    }
}
