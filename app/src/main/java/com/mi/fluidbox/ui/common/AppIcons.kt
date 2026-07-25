package com.mi.fluidbox.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val Battery: ImageVector by lazy {
        ImageVector.Builder(
            name = "Battery",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 3f)
                horizontalLineTo(15f)
                curveTo(16.1f, 3f, 17f, 3.9f, 17f, 5f)
                verticalLineTo(6f)
                horizontalLineTo(18f)
                curveTo(18.55f, 6f, 19f, 6.45f, 19f, 7f)
                verticalLineTo(11f)
                curveTo(19f, 11.55f, 18.55f, 12f, 18f, 12f)
                horizontalLineTo(17f)
                verticalLineTo(19f)
                curveTo(17f, 20.1f, 16.1f, 21f, 15f, 21f)
                horizontalLineTo(7f)
                curveTo(5.9f, 21f, 5f, 20.1f, 5f, 19f)
                verticalLineTo(5f)
                curveTo(5f, 3.9f, 5.9f, 3f, 7f, 3f)
                close()

                moveTo(10f, 7f)
                lineTo(8f, 13f)
                horizontalLineTo(11f)
                lineTo(10f, 18f)
                lineTo(15f, 10f)
                horizontalLineTo(12f)
                lineTo(13f, 7f)
                horizontalLineTo(10f)
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
}
