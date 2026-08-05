package com.mi.fluidbox.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val HomeFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "HomeFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(3f, 10.8f)
                lineTo(12f, 3f)
                lineTo(21f, 10.8f)
                lineTo(19.6f, 12.42f)
                lineTo(18f, 11.05f)
                verticalLineTo(20f)
                curveTo(18f, 20.55f, 17.55f, 21f, 17f, 21f)
                horizontalLineTo(14f)
                verticalLineTo(15f)
                horizontalLineTo(10f)
                verticalLineTo(21f)
                horizontalLineTo(7f)
                curveTo(6.45f, 21f, 6f, 20.55f, 6f, 20f)
                verticalLineTo(11.05f)
                lineTo(4.4f, 12.42f)
                close()
            }
        }.build()
    }

    val HomeOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "HomeOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.15f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(4f, 10.8f)
                lineTo(12f, 4f)
                lineTo(20f, 10.8f)
                moveTo(6.5f, 9.8f)
                verticalLineTo(20f)
                horizontalLineTo(10f)
                verticalLineTo(14f)
                horizontalLineTo(14f)
                verticalLineTo(20f)
                horizontalLineTo(17.5f)
                verticalLineTo(9.8f)
            }
        }.build()
    }

    val BlurDots: ImageVector by lazy {
        ImageVector.Builder(
            name = "BlurDots",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                val dots = listOf(
                    12f to 4f,
                    8f to 6f, 12f to 6f, 16f to 6f,
                    6f to 10f, 10f to 10f, 14f to 10f, 18f to 10f,
                    4f to 14f, 8f to 14f, 12f to 14f, 16f to 14f, 20f to 14f,
                    6f to 18f, 10f to 18f, 14f to 18f, 18f to 18f,
                    8f to 22f, 12f to 22f, 16f to 22f,
                )
                dots.forEach { (x, y) ->
                    moveTo(x + 1.1f, y)
                    arcToRelative(1.1f, 1.1f, 0f, true, true, -2.2f, 0f)
                    arcToRelative(1.1f, 1.1f, 0f, true, true, 2.2f, 0f)
                    close()
                }
            }
        }.build()
    }

    val FloatingBar: ImageVector by lazy {
        ImageVector.Builder(
            name = "FloatingBar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(4f, 5f)
                horizontalLineTo(20f)
                curveTo(21.1f, 5f, 22f, 5.9f, 22f, 7f)
                verticalLineTo(17f)
                curveTo(22f, 18.1f, 21.1f, 19f, 20f, 19f)
                horizontalLineTo(4f)
                curveTo(2.9f, 19f, 2f, 18.1f, 2f, 17f)
                verticalLineTo(7f)
                curveTo(2f, 5.9f, 2.9f, 5f, 4f, 5f)
                close()

                moveTo(5f, 15f)
                horizontalLineTo(19f)
                curveTo(19.55f, 15f, 20f, 15.45f, 20f, 16f)
                reflectiveCurveTo(19.55f, 17f, 19f, 17f)
                horizontalLineTo(5f)
                curveTo(4.45f, 17f, 4f, 16.55f, 4f, 16f)
                reflectiveCurveTo(4.45f, 15f, 5f, 15f)
                close()
            }
        }.build()
    }

    val Droplet: ImageVector by lazy {
        ImageVector.Builder(
            name = "Droplet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 2f)
                curveTo(8.4f, 6.2f, 5f, 10.1f, 5f, 14f)
                curveTo(5f, 18.42f, 8.13f, 22f, 12f, 22f)
                reflectiveCurveTo(19f, 18.42f, 19f, 14f)
                curveTo(19f, 10.1f, 15.6f, 6.2f, 12f, 2f)
                close()

                moveTo(10.2f, 17.3f)
                curveTo(8.95f, 16.8f, 8f, 15.45f, 8f, 14f)
                curveTo(8f, 13.45f, 8.45f, 13f, 9f, 13f)
                reflectiveCurveTo(10f, 13.45f, 10f, 14f)
                curveTo(10f, 14.65f, 10.42f, 15.25f, 11f, 15.5f)
                curveTo(11.5f, 15.72f, 11.74f, 16.3f, 11.52f, 16.82f)
                curveTo(11.3f, 17.32f, 10.72f, 17.54f, 10.2f, 17.3f)
                close()
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "Check",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(9f, 16.17f)
                lineTo(4.83f, 12f)
                lineTo(3.41f, 13.41f)
                lineTo(9f, 19f)
                lineTo(21f, 7f)
                lineTo(19.59f, 5.59f)
                close()
            }
        }.build()
    }

    val Widgets: ImageVector by lazy {
        ImageVector.Builder(
            name = "Widgets",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(3f, 3f)
                lineTo(10f, 3f)
                lineTo(10f, 10f)
                lineTo(3f, 10f)
                close()

                moveTo(14f, 3f)
                lineTo(21f, 3f)
                lineTo(21f, 10f)
                lineTo(14f, 10f)
                close()

                moveTo(3f, 14f)
                lineTo(10f, 14f)
                lineTo(10f, 21f)
                lineTo(3f, 21f)
                close()

                moveTo(14f, 14f)
                lineTo(21f, 14f)
                lineTo(21f, 21f)
                lineTo(14f, 21f)
                close()
            }
        }.build()
    }

    val WidgetsOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "WidgetsOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(4f, 4f)
                horizontalLineTo(10f)
                verticalLineTo(10f)
                horizontalLineTo(4f)
                close()

                moveTo(14f, 4f)
                horizontalLineTo(20f)
                verticalLineTo(10f)
                horizontalLineTo(14f)
                close()

                moveTo(4f, 14f)
                horizontalLineTo(10f)
                verticalLineTo(20f)
                horizontalLineTo(4f)
                close()

                moveTo(14f, 14f)
                horizontalLineTo(20f)
                verticalLineTo(20f)
                horizontalLineTo(14f)
                close()
            }
        }.build()
    }

    val Event: ImageVector by lazy {
        ImageVector.Builder(
            name = "Event",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 2f)
                lineTo(9f, 2f)
                lineTo(9f, 6f)
                lineTo(7f, 6f)
                close()

                moveTo(15f, 2f)
                lineTo(17f, 2f)
                lineTo(17f, 6f)
                lineTo(15f, 6f)
                close()

                moveTo(3f, 4f)
                lineTo(21f, 4f)
                lineTo(21f, 21f)
                lineTo(3f, 21f)
                close()

                moveTo(3f, 8f)
                lineTo(21f, 8f)
                lineTo(21f, 10f)
                lineTo(3f, 10f)
                close()

                moveTo(7f, 13f)
                lineTo(10f, 13f)
                lineTo(10f, 16f)
                lineTo(7f, 16f)
                close()

                moveTo(12f, 13f)
                lineTo(15f, 13f)
                lineTo(15f, 16f)
                lineTo(12f, 16f)
                close()
            }
        }.build()
    }

    val Extension: ImageVector by lazy {
        ImageVector.Builder(
            name = "Extension",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(20.5f, 11.0f)
                horizontalLineTo(19.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.5f)
                curveTo(13.0f, 2.12f, 11.88f, 1.0f, 10.5f, 1.0f)
                reflectiveCurveTo(8.0f, 2.12f, 8.0f, 3.5f)
                verticalLineTo(5.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
                verticalLineToRelative(3.8f)
                horizontalLineTo(3.5f)
                curveToRelative(1.49f, 0.0f, 2.7f, 1.21f, 2.7f, 2.7f)
                reflectiveCurveToRelative(-1.21f, 2.7f, -2.7f, 2.7f)
                horizontalLineTo(2.0f)
                verticalLineTo(20.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(3.8f)
                verticalLineToRelative(-1.5f)
                curveToRelative(0.0f, -1.49f, 1.21f, -2.7f, 2.7f, -2.7f)
                reflectiveCurveToRelative(2.7f, 1.21f, 2.7f, 2.7f)
                verticalLineTo(22.0f)
                horizontalLineTo(17.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineToRelative(1.5f)
                curveToRelative(1.38f, 0.0f, 2.5f, -1.12f, 2.5f, -2.5f)
                reflectiveCurveTo(21.88f, 11.0f, 20.5f, 11.0f)
                close()
            }
        }.build()
    }

    val Tune: ImageVector by lazy {
        ImageVector.Builder(
            name = "Tune",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5f, 4f)
                lineTo(7f, 4f)
                lineTo(7f, 20f)
                lineTo(5f, 20f)
                close()

                moveTo(11f, 4f)
                lineTo(13f, 4f)
                lineTo(13f, 20f)
                lineTo(11f, 20f)
                close()

                moveTo(17f, 4f)
                lineTo(19f, 4f)
                lineTo(19f, 20f)
                lineTo(17f, 20f)
                close()

                moveTo(3f, 7f)
                lineTo(9f, 7f)
                lineTo(9f, 9f)
                lineTo(3f, 9f)
                close()

                moveTo(9f, 13f)
                lineTo(15f, 13f)
                lineTo(15f, 15f)
                lineTo(9f, 15f)
                close()

                moveTo(15f, 9f)
                lineTo(21f, 9f)
                lineTo(21f, 11f)
                lineTo(15f, 11f)
                close()
            }
        }.build()
    }

    val Filter: ImageVector by lazy {
        ImageVector.Builder(
            name = "Filter",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(10f, 18f)
                lineTo(14f, 18f)
                lineTo(14f, 16f)
                lineTo(10f, 16f)
                close()

                moveTo(3f, 12f)
                lineTo(21f, 12f)
                lineTo(21f, 10f)
                lineTo(3f, 10f)
                close()

                moveTo(6f, 6f)
                lineTo(18f, 6f)
                lineTo(18f, 4f)
                lineTo(6f, 4f)
                close()
            }
        }.build()
    }

    val Save: ImageVector by lazy {
        ImageVector.Builder(
            name = "Save",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(5f, 3f)
                horizontalLineTo(17f)
                lineTo(21f, 7f)
                verticalLineTo(19f)
                curveTo(21f, 20.1f, 20.1f, 21f, 19f, 21f)
                horizontalLineTo(5f)
                curveTo(3.9f, 21f, 3f, 20.1f, 3f, 19f)
                verticalLineTo(5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                close()

                moveTo(12f, 19f)
                curveTo(13.66f, 19f, 15f, 17.66f, 15f, 16f)
                curveTo(15f, 14.34f, 13.66f, 13f, 12f, 13f)
                curveTo(10.34f, 13f, 9f, 14.34f, 9f, 16f)
                curveTo(9f, 17.66f, 10.34f, 19f, 12f, 19f)
                close()

                moveTo(15f, 9f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(9f)
                horizontalLineTo(15f)
                close()
            }
        }.build()
    }

    val LightMode: ImageVector by lazy {
        ImageVector.Builder(
            name = "LightMode",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12.0f, 7.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
                reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
                reflectiveCurveToRelative(5.0f, -2.24f, 5.0f, -5.0f)
                reflectiveCurveTo(14.76f, 7.0f, 12.0f, 7.0f)
                lineTo(12.0f, 7.0f)
                close()

                moveTo(2.0f, 13.0f)
                lineToRelative(2.0f, 0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineToRelative(-2.0f, 0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveTo(1.45f, 13.0f, 2.0f, 13.0f)
                close()

                moveTo(20.0f, 13.0f)
                lineToRelative(2.0f, 0.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                lineToRelative(-2.0f, 0.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveTo(19.45f, 13.0f, 20.0f, 13.0f)
                close()

                moveTo(11.0f, 2.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveTo(11.0f, 1.45f, 11.0f, 2.0f)
                close()

                moveTo(11.0f, 20.0f)
                verticalLineToRelative(2.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                reflectiveCurveToRelative(1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                curveTo(11.45f, 19.0f, 11.0f, 19.45f, 11.0f, 20.0f)
                close()

                moveTo(5.99f, 4.58f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0.0f, 1.41f)
                lineToRelative(1.06f, 1.06f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.41f, 0.0f)
                reflectiveCurveToRelative(0.39f, -1.03f, 0.0f, -1.41f)
                lineTo(5.99f, 4.58f)
                close()

                moveTo(18.36f, 16.95f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.41f, 0.0f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0.0f, 1.41f)
                lineToRelative(1.06f, 1.06f)
                curveToRelative(0.39f, 0.39f, 1.03f, 0.39f, 1.41f, 0.0f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0.0f, -1.41f)
                lineTo(18.36f, 16.95f)
                close()

                moveTo(19.42f, 5.99f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0.0f, -1.41f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-1.06f, 1.06f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0.0f, 1.41f)
                reflectiveCurveToRelative(1.03f, 0.39f, 1.41f, 0.0f)
                lineTo(19.42f, 5.99f)
                close()

                moveTo(7.05f, 18.36f)
                curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0.0f, -1.41f)
                curveToRelative(-0.39f, -0.39f, -1.03f, -0.39f, -1.41f, 0.0f)
                lineToRelative(-1.06f, 1.06f)
                curveToRelative(-0.39f, 0.39f, -0.39f, 1.03f, 0.0f, 1.41f)
                reflectiveCurveToRelative(1.03f, 0.39f, 1.41f, 0.0f)
                lineTo(7.05f, 18.36f)
                close()
            }
        }.build()
    }

    val Palette: ImageVector by lazy {
        ImageVector.Builder(
            name = "Palette",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 2f)
                curveTo(6.49f, 2f, 2f, 6.49f, 2f, 12f)
                reflectiveCurveToRelative(4.49f, 10f, 10f, 10f)
                curveToRelative(1.38f, 0f, 2.5f, -1.12f, 2.5f, -2.5f)
                curveToRelative(0f, -0.61f, -0.23f, -1.2f, -0.64f, -1.67f)
                curveToRelative(-0.08f, -0.1f, -0.13f, -0.21f, -0.13f, -0.33f)
                curveToRelative(0f, -0.28f, 0.22f, -0.5f, 0.5f, -0.5f)
                horizontalLineTo(16f)
                curveToRelative(3.31f, 0f, 6f, -2.69f, 6f, -6f)
                curveTo(22f, 6.04f, 17.51f, 2f, 12f, 2f)
                close()

                moveTo(17.5f, 13f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                curveTo(19f, 12.33f, 18.33f, 13f, 17.5f, 13f)
                close()

                moveTo(14.5f, 9f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()

                moveTo(6.5f, 13f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()

                moveTo(9.5f, 9f)
                curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
            }
        }.build()
    }

    val Phone: ImageVector by lazy {
        ImageVector.Builder(
            name = "Phone",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 2f)
                horizontalLineTo(17f)
                curveTo(18.1f, 2f, 19f, 2.9f, 19f, 4f)
                verticalLineTo(20f)
                curveTo(19f, 21.1f, 18.1f, 22f, 17f, 22f)
                horizontalLineTo(7f)
                curveTo(5.9f, 22f, 5f, 21.1f, 5f, 20f)
                verticalLineTo(4f)
                curveTo(5f, 2.9f, 5.9f, 2f, 7f, 2f)
                close()

                moveTo(8f, 5f)
                horizontalLineTo(16f)
                verticalLineTo(17f)
                horizontalLineTo(8f)
                close()

                moveTo(12f, 20f)
                curveTo(12.55f, 20f, 13f, 19.55f, 13f, 19f)
                curveTo(13f, 18.45f, 12.55f, 18f, 12f, 18f)
                curveTo(11.45f, 18f, 11f, 18.45f, 11f, 19f)
                curveTo(11f, 19.55f, 11.45f, 20f, 12f, 20f)
                close()
            }
        }.build()
    }

    val EventOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "EventOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(7f, 3f)
                verticalLineTo(6f)
                moveTo(17f, 3f)
                verticalLineTo(6f)
                moveTo(5f, 5f)
                horizontalLineTo(19f)
                verticalLineTo(21f)
                horizontalLineTo(5f)
                close()
                moveTo(5f, 9f)
                horizontalLineTo(19f)
                moveTo(8f, 13f)
                horizontalLineTo(10f)
                moveTo(14f, 13f)
                horizontalLineTo(16f)
                moveTo(8f, 17f)
                horizontalLineTo(10f)
                moveTo(14f, 17f)
                horizontalLineTo(16f)
            }
        }.build()
    }

    val Heart: ImageVector by lazy {
        ImageVector.Builder(
            name = "Heart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.08f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                close()
            }
        }.build()
    }

    val BookOpen: ImageVector by lazy {
        ImageVector.Builder(
            name = "BookOpen",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(4f, 4.5f)
                curveTo(4f, 3.67f, 4.67f, 3f, 5.5f, 3f)
                horizontalLineTo(10f)
                curveTo(11.2f, 3f, 12.24f, 3.52f, 13f, 4.24f)
                curveTo(13.76f, 3.52f, 14.8f, 3f, 16f, 3f)
                horizontalLineTo(20.5f)
                curveTo(21.33f, 3f, 22f, 3.67f, 22f, 4.5f)
                verticalLineTo(18.5f)
                curveTo(22f, 19.33f, 21.33f, 20f, 20.5f, 20f)
                horizontalLineTo(16.2f)
                curveTo(15.13f, 20f, 14.12f, 20.42f, 13.36f, 21.18f)
                curveTo(13.16f, 21.38f, 12.84f, 21.38f, 12.64f, 21.18f)
                curveTo(11.88f, 20.42f, 10.87f, 20f, 9.8f, 20f)
                horizontalLineTo(5.5f)
                curveTo(4.67f, 20f, 4f, 19.33f, 4f, 18.5f)
                close()

                moveTo(6f, 5f)
                verticalLineTo(18f)
                horizontalLineTo(9.8f)
                curveTo(10.56f, 18f, 11.3f, 18.17f, 12f, 18.48f)
                verticalLineTo(6.2f)
                curveTo(11.55f, 5.47f, 10.78f, 5f, 10f, 5f)
                close()

                moveTo(14f, 6.2f)
                verticalLineTo(18.48f)
                curveTo(14.7f, 18.17f, 15.44f, 18f, 16.2f, 18f)
                horizontalLineTo(20f)
                verticalLineTo(5f)
                horizontalLineTo(16f)
                curveTo(15.22f, 5f, 14.45f, 5.47f, 14f, 6.2f)
                close()
            }
        }.build()
    }

    val InfoFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "InfoFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.EvenOdd,
            ) {
                moveTo(12f, 2f)
                arcToRelative(10f, 10f, 0f, true, true, 0f, 20f)
                arcToRelative(10f, 10f, 0f, true, true, 0f, -20f)
                close()

                moveTo(11f, 10f)
                horizontalLineTo(13f)
                verticalLineTo(17f)
                horizontalLineTo(11f)
                close()

                moveTo(11f, 6f)
                horizontalLineTo(13f)
                verticalLineTo(8f)
                horizontalLineTo(11f)
                close()
            }
        }.build()
    }

    val InfoOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "InfoOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 2.8f)
                arcToRelative(9.2f, 9.2f, 0f, true, true, 0f, 18.4f)
                arcToRelative(9.2f, 9.2f, 0f, true, true, 0f, -18.4f)
                close()
            }
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(11f, 10f)
                horizontalLineTo(13f)
                verticalLineTo(17f)
                horizontalLineTo(11f)
                close()

                moveTo(11f, 6f)
                horizontalLineTo(13f)
                verticalLineTo(8f)
                horizontalLineTo(11f)
                close()
            }
        }.build()
    }

    val Search: ImageVector by lazy {
        ImageVector.Builder(
            name = "Search",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(10.5f, 5f)
                arcToRelative(5.5f, 5.5f, 0f, true, true, 0f, 11f)
                arcToRelative(5.5f, 5.5f, 0f, true, true, 0f, -11f)
                moveTo(15f, 15f)
                lineTo(20f, 20f)
            }
        }.build()
    }

    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "Refresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(20f, 6f)
                verticalLineTo(11f)
                horizontalLineTo(15f)
                moveTo(4f, 18f)
                verticalLineTo(13f)
                horizontalLineTo(9f)
                moveTo(6.1f, 9f)
                curveTo(7.1f, 6.7f, 9.4f, 5f, 12.1f, 5f)
                curveTo(15.4f, 5f, 18.1f, 7.1f, 19.1f, 10f)
                moveTo(17.9f, 15f)
                curveTo(16.9f, 17.3f, 14.6f, 19f, 11.9f, 19f)
                curveTo(8.6f, 19f, 5.9f, 16.9f, 4.9f, 14f)
            }
        }.build()
    }

    val Share: ImageVector by lazy {
        ImageVector.Builder(
            name = "Share",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(8.5f, 11f)
                lineTo(15.5f, 7f)
                moveTo(8.5f, 13f)
                lineTo(15.5f, 17f)
                moveTo(6f, 9f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, 6f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, -6f)
                moveTo(18f, 4f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, 6f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, -6f)
                moveTo(18f, 14f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, 6f)
                arcToRelative(3f, 3f, 0f, true, true, 0f, -6f)
            }
        }.build()
    }

    val Trash: ImageVector by lazy {
        ImageVector.Builder(
            name = "Trash",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(4f, 7f)
                horizontalLineTo(20f)
                moveTo(10f, 11f)
                verticalLineTo(17f)
                moveTo(14f, 11f)
                verticalLineTo(17f)
                moveTo(6f, 7f)
                lineTo(7f, 20f)
                horizontalLineTo(17f)
                lineTo(18f, 7f)
                moveTo(9f, 7f)
                verticalLineTo(4f)
                horizontalLineTo(15f)
                verticalLineTo(7f)
            }
        }.build()
    }
}
