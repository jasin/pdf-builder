package timrs2998.pdfbuilder

import org.apache.pdfbox.pdmodel.PDDocument

class TableElement(override val parent: Document) : Element(parent) {

    /**
     * A sticky header that will be repeated on top of each page.
     */
    var header: RowElement? = null

    val rows = mutableListOf<RowElement>()

    override fun instanceHeight(width: Float, startY: Float): Float {
        // TODO: use 'startY' parameter to determine actual height, including sticky headers
        return (header?.height(width, startY) ?: 0f) + rows.fold(0.0f, { sum, row -> sum + row.height(width, startY) })
    }

    // Since table may span multiple pages, breaking on rows, renderInstance() must handle border, background, ..
    override fun render(
            pdDocument: PDDocument,
            startX: Float,
            endX: Float,
            startY: Float,
            minHeight: Float) {
        renderInstance(pdDocument, startX, endX, startY, minHeight)
    }

    override fun renderInstance(
            pdDocument: PDDocument,
            startX: Float,
            endX: Float,
            startY: Float,
            minHeight: Float) {
        var currentY = startY
        // Shadow existing list with a list containing sticky headers
        var rows: MutableList<RowElement> = if (header == null) rows else (mutableListOf(header!!) + rows).toMutableList()
        var i = 0
        while (i < rows.size) {
            var row = rows[i]
            var height = row.height(endX - startX, currentY)
            val adjustedStartY = parent.adjustStartYForPaging(currentY, currentY + height)

            // Add sticky header to top of page, if necessary
            if (currentY != adjustedStartY && header != null) {
                row = header!!
                height = row.height(endX - startY, adjustedStartY)
                i -= 1
            }

            row.render(pdDocument, startX, endX, adjustedStartY)

            // Draw bottom border before advancing to next page and draw top border of next page
            if (currentY != adjustedStartY) {
                drawLine(document, pdDocument, startX, adjustedStartY, endX, adjustedStartY, border.top, border.topColor)
                drawLine(document, pdDocument, startX, currentY, endX, currentY, border.bottom, border.bottomColor)
            }

            currentY = adjustedStartY + height

            // Draw left and right borders
            drawLine(document, pdDocument, startX, adjustedStartY, startX, currentY, border.left, border.leftColor)
            drawLine(document, pdDocument, endX, adjustedStartY, endX, currentY, border.right, border.rightColor)

            i += 1
        }

        // Draw first top border and last bottom border
        drawLine(document, pdDocument, startX, startY, endX, startY, border.top, border.topColor)
        drawLine(document, pdDocument, startX, currentY, endX, currentY, border.bottom, border.bottomColor)

        // TODO: background, margins, and padding
    }

}