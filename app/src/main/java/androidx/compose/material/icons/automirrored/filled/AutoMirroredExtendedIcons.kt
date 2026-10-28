package androidx.compose.material.icons.automirrored.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.AutoMirrored.Filled.NavigateBefore: ImageVector
    get() {
        if (_navigateBefore != null) return _navigateBefore!!
        _navigateBefore = materialIcon(name = "AutoMirrored.Filled.NavigateBefore", autoMirror = true) {
            materialPath {
                moveTo(15.41f, 7.41f)
                lineTo(14.0f, 6.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(6.0f, 6.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(10.83f, 12.0f)
                close()
            }
        }
        return _navigateBefore!!
    }
private var _navigateBefore: ImageVector? = null

val Icons.AutoMirrored.Filled.NavigateNext: ImageVector
    get() {
        if (_navigateNext != null) return _navigateNext!!
        _navigateNext = materialIcon(name = "AutoMirrored.Filled.NavigateNext", autoMirror = true) {
            materialPath {
                moveTo(10.0f, 6.0f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12.0f)
                lineToRelative(-4.58f, 4.59f)
                lineTo(10.0f, 18.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
        return _navigateNext!!
    }
private var _navigateNext: ImageVector? = null
