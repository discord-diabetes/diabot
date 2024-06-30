package com.dongtronic.diabot.graph.theme

import com.dongtronic.diabot.graph.BgGraph
import org.knowm.xchart.internal.chartpart.Plot_
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.font.TextLayout
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.*
import kotlin.random.Random

class DiabotAnimalsTheme : DiabotTheme(true) {
    companion object {
        private val credits = buildMap {
            this["cat-5746771_960_720_webp_trimmed.png"] = "nekomachines @ pixabay.com"
            this["cat-5746875_960_720_trimmed.png"] = "nekomachines @ pixabay.com"
            this["cat-5755899_960_720_trimmed.png"] = "nekomachines @ pixabay.com"
            this["cat-5968859_960_720_trimmed.png"] = "nekomachines @ pixabay.com"
            this["cat-6005847_960_720_trimmed.png"] = "nekomachines @ pixabay.com"
            this["cat-6012322_960_720_trimmed.png"] = "nekomachines @ pixabay.com"
            this["cat-7307184_1280_trimmed.png"] = "nekomachines @ pixabay.com"
            this["dog-6012750_960_720_trimmed.png"] = "nekomachines @ pixabay.com"
            this["fluffy-6140194_960_720_trimmed.png"] = "nekomachines @ pixabay.com"
            this["rat_20240625_720_720.png"] = "Luxlyn"
            this["dog-5188108_960_720_trimmed.png"] = "Edurs34 @ pixabay.com"
            this["cat-4475583_960_720_trimmed.png"] = "LimoncitoSketching @ pixabay.com"
            this["Project_20230323020209_trimmed.png"] = "nads"
            this["raccoon-dropped-their-pump-20240627-824x818.png"] = "DJ"
        }

        private const val CREDITS_PREFIX = "Image Credits: "
    }

    override fun getChartBackgroundColor(): Color {
        // transparent so we can manually draw the background, along with the animals
        return Color(0, 0, 0, 0)
    }

    private fun getRealChartBackgroundColor(): Color {
        return Color(32, 32, 32)
    }

    override fun getChartFontColor(): Color {
        return Color.WHITE
    }

    override fun getAxisTickLabelsColor(): Color {
        return chartFontColor
    }

    override fun getPlotGridLinesColor(): Color {
        return Color(32, 32, 32, 192)
    }

    override fun isAxisTicksMarksVisible(): Boolean {
        return false
    }

    override fun getAxisTickMarksColor(): Color {
        return Color(255, 0, 255, 0)
    }

    override fun getPlotBorderColor(): Color {
        return getRealPlotBackgroundColor()
    }

    override fun getPlotBackgroundColor(): Color {
        // half opacity so the animals are still visible, but aren't too bright
        return Color(25, 25, 25, 128)
    }

    private fun getRealPlotBackgroundColor(): Color {
        return Color(25, 25, 25)
    }

    private fun getCreditsFontColor(): Color {
        return Color(166, 166, 166)
    }

    override fun getImageHeight(chart: BgGraph): Int {
        val textLayout = TextLayout(
            CREDITS_PREFIX,
            chart.styler.chartTitleFont,
            FontRenderContext(null, true, true)
        )

        return (super.getImageHeight(chart) + textLayout.bounds.height * 2).roundToInt()
    }

    override fun customPaint(g: Graphics2D, chart: BgGraph, plot: Plot_<*, *>) {
        // this is unfortunately needed to calculate the plot boundaries for rendering.
        // we are technically wasting performance here (because we're painting the whole plot twice) but it's the simplest way.
        // otherwise, we would need to use reflection or reimplement some rendering prep ourselves.
        // to explain: the plot's `bounds` variable is not initialized until `plot.paint()` is called,
        // but calling `plot.paint()` requires all the `XYSeries` objects in the chart to have a non-null rendering style, which is handled
        // in `chart.paint()`.
        chart.paint(g, chart.width, chart.height)

        // draw background with real color (thus painting over the entire graph that was just drawn)
        g.color = this.getRealChartBackgroundColor()
        g.fill(Rectangle2D.Double(0.0, 0.0, chart.width.toDouble(), chart.height.toDouble() + 40))

        // draw plot background in real color
        g.color = this.getRealPlotBackgroundColor()
        g.fill(plot.bounds)

        val numAnimals = 2
        val bounds: List<Rectangle> = splitBounds(plot.bounds.bounds)
        val images = credits.toMutableMap()
        val usedCredits = mutableSetOf<String>()

        for (i in 1..numAnimals) {
            val img = images.entries.random()
            // Remove cat from list to avoid duplicates
            images.remove(img.key)
            usedCredits.add(img.value)

            var cat = ImageIO.read(this.javaClass.classLoader.getResourceAsStream("af_images/${img.key}"))
            val angle = (-30..30).random()
            cat = rotate(cat, angle.toDouble())
            // The boundaries of the smaller split rectangle in the plot
            val plotBounds: Rectangle = bounds[i - 1]

            // The randomly scaled+positioned boundaries for the image
            val imgBounds: Rectangle = calculateImageBounds(cat.width, cat.height, plotBounds)

            // Bilinear to smooth the image scaling
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

            g.drawImage(cat, imgBounds.x, imgBounds.y, imgBounds.width, imgBounds.height, null)
        }

        val credits = usedCredits.joinToString(separator = " | ", prefix = CREDITS_PREFIX)

        // Draw credits
        g.font = chart.styler.chartTitleFont
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val textLayout = TextLayout(credits, chart.styler.chartTitleFont, g.fontRenderContext)
        val textBounds = textLayout.bounds

        g.color = getCreditsFontColor()
        g.drawString(credits, 5, chart.height + textBounds.height.toInt())

        // re-paint the graph, filling in the rest of the data.
        chart.paint(g, chart.width, chart.height)
    }

    /**
     * Calculate a random position and scale for an image within a bounding box.
     *
     * This function does two things to provide variety to the images:
     * - scales the image down to fit the [bounds], then scales it down further by a random amount.
     * - randomly positioning the image within the [bounds].
     *
     * @param width Image width
     * @param height Image height
     * @param bounds Bounding box to fit the image within
     * @return Rectangle of where the image should be drawn
     */
    private fun calculateImageBounds(width: Int, height: Int, bounds: Rectangle): Rectangle {
        // Randomly scale image within plot size
        // calculate the smallest scaling value to fit the image into the `bounds` while maintaining aspect ratio
        var scale = min(bounds.width.toDouble() / width, bounds.height.toDouble() / height).coerceAtMost(1.0)
        // scale it down further by a random amount between 50% and 100%, so they're not always perfectly fit to the bounds
        scale *= Random.nextDouble(0.5, 1.0)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        // Randomly position image within plot bounds
        val xOff = (0..bounds.width - newWidth).random()
        val yOff = (0..bounds.height - newHeight).random()
        // calculate the final x and y positions
        val xPos = xOff + bounds.x
        val yPos = yOff + bounds.y
        // return the rectangle of where the image should be drawn
        return Rectangle(xPos, yPos, newWidth, newHeight)
    }

    /**
     * Rotate a [BufferedImage] by an angle in degrees.
     *
     * Credits to Vinz on StackOverflow: https://stackoverflow.com/a/68926993
     * Slightly modified from the original answer to clean up variable names.
     *
     * @param image The image to rotate
     * @param angle The angle in degrees to rotate the image by
     * @return The input [image], rotated by [angle] degrees
     */
    private fun rotate(image: BufferedImage, angle: Double): BufferedImage {
        val angRad = Math.toRadians(angle)
        val sin = abs(sin(angRad))
        val cos = abs(cos(angRad))

        val w = image.width
        val h = image.height
        val newW = floor(w * cos + h * sin).toInt()
        val newH = floor(h * cos + w * sin).toInt()

        val rotated = BufferedImage(newW, newH, image.type)
        val graphic = rotated.createGraphics()
        graphic.translate((newW - w) / 2, (newH - h) / 2)
        graphic.rotate(angRad, (w / 2).toDouble(), (h / 2).toDouble())
        graphic.drawRenderedImage(image, null)
        graphic.dispose()
        return rotated
    }

    /**
     * Split a large rectangle into two equally-sized smaller rectangles along a random axis.
     *
     * The output rectangles both fit inside the input rectangle.
     *
     * @param bounds The large rectangle to split
     * @return List of two equally-sized smaller rectangles which fit inside the input rectangle
     */
    private fun splitBounds(bounds: Rectangle): List<Rectangle> {
        val horizontal = Random.nextBoolean()
        return if (horizontal) {
            val splitHeight = bounds.height / 2
            val one = Rectangle(bounds.x, bounds.y, bounds.width, splitHeight)
            val two = Rectangle(bounds.x, bounds.y + splitHeight, bounds.width, splitHeight)
            listOf(one, two)
        } else {
            val splitWidth = bounds.width / 2
            val one = Rectangle(bounds.x, bounds.y, splitWidth, bounds.height)
            val two = Rectangle(bounds.x + splitWidth, bounds.y, splitWidth, bounds.height)
            listOf(one, two)
        }
    }
}
