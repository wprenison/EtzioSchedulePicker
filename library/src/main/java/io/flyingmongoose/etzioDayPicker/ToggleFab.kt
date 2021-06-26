package io.flyingmongoose.etzioDayPicker

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.flyingmongoose.etzioTimePicker.library.R

class ToggleFab(context: Context, attr: AttributeSet?) : FloatingActionButton(context, attr) {
    init {
        context.theme.obtainStyledAttributes(attr, R.styleable.ToggleFab, 0, 0)
            .apply {
                try {
                    activeColor = ColorStateList.valueOf(getColor(R.styleable.ToggleFab_toggleActiveColor, 0))
                    inactiveColor = ColorStateList.valueOf(getColor(R.styleable.ToggleFab_toggleInactiveColor, 0))
                    activeIcon = getDrawable(R.styleable.ToggleFab_toggleActiveIcon)
                    inactiveIcon = getDrawable(R.styleable.ToggleFab_toggleInactiveIcon)
                } finally {
                    recycle()
                }
            }
    }

    var activeColor: ColorStateList?
    var inactiveColor: ColorStateList?
    var activeIcon: Drawable?
    var inactiveIcon: Drawable?

    var toggleSelectedState = false
        set(value) {
            if(field != value) {
                field = value
                if (field) {
                    backgroundTintList = activeColor
                    setImageDrawable(activeIcon)
                } else {
                    backgroundTintList = inactiveColor
                    setImageDrawable(inactiveIcon)
                }
            }
        }
}