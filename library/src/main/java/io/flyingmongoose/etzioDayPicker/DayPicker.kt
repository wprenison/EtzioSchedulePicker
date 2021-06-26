package io.flyingmongoose.etzioDayPicker

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import io.flyingmongoose.etzioDayPicker.DayVModel.*
import io.flyingmongoose.etzioTimePicker.library.R
import io.flyingmongoose.exception.LifecycleOwnerNotFoundException
import io.flyingmongoose.exception.ViewModelStoreOwnerNotFoundException
import kotlinx.android.synthetic.main.day_picker.view.*

class DayPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr)
{
    private var dayFocusedListener: DayFocusedListener? = null

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var dayVModel: DayVModel

    init
    {
        View.inflate(context, R.layout.day_picker, this)
        initViewModels(attrs)
        initObservers()

        fabMonday.setOnClickListener { dayVModel.handleDayClicked(Weekdays.MONDAY, dayFocusedListener) }
        fabTuesday.setOnClickListener { dayVModel.handleDayClicked(Weekdays.TUESDAY, dayFocusedListener) }
        fabWednesday.setOnClickListener { dayVModel.handleDayClicked(Weekdays.WEDNESDAY, dayFocusedListener) }
        fabThursday.setOnClickListener { dayVModel.handleDayClicked(Weekdays.THURSDAY, dayFocusedListener) }
        fabFriday.setOnClickListener { dayVModel.handleDayClicked(Weekdays.FRIDAY, dayFocusedListener) }
        fabSaterday.setOnClickListener { dayVModel.handleDayClicked(Weekdays.SATURDAY, dayFocusedListener) }
        fabSunday.setOnClickListener { dayVModel.handleDayClicked(Weekdays.SUNDAY, dayFocusedListener) }

        if (dayVModel.includeWeekends) {
            fabSaterday.visibility = View.VISIBLE
            fabSunday.visibility = View.VISIBLE
        }
        else {
            fabSaterday.visibility = View.GONE
            fabSunday.visibility = View.GONE
        }
    }

    private fun initViewModels(attrs: AttributeSet?)
    {
        lifecycleOwner = context as? LifecycleOwner ?: throw LifecycleOwnerNotFoundException()
        val viewModelStoreOwner = context as? ViewModelStoreOwner ?: throw ViewModelStoreOwnerNotFoundException()
        dayVModel = ViewModelProvider(viewModelStoreOwner).get(DayVModel::class.java)
        loadAttributes(attrs, dayVModel)
    }

    private fun initObservers()
    {
        dayVModel.selectedDays.observe(lifecycleOwner) { selectedDays ->
            updateTravelDaySelector(selectedDays)
        }
    }

    /**
     * Sets default theme attributes for picker
     * These will be used if picker's attributes aren't set
     */
    @SuppressLint("ResourceType")
    private fun loadAppThemeDefaults()
    {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(
            typedValue.data, intArrayOf(
                android.R.attr.textColorPrimaryInverse,
                android.R.attr.textColorPrimary,
                android.R.attr.colorControlNormal
            )
        )
        typedArray.recycle()
    }

    /**
     * Sets picker's attributes from xml file
     * @param attrs
     */
    private fun loadAttributes(attrs: AttributeSet?, dayVModel: DayVModel)
    {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DayPicker)
        dayVModel.includeWeekends = typedArray.getBoolean(R.styleable.DayPicker_includeWeekends, false)
        typedArray.recycle()
    }

    private fun updateTravelDaySelector(selectedDays: List<Weekdays>) {
        val daysToUnselect = mutableListOf(Weekdays.MONDAY, Weekdays.TUESDAY, Weekdays.WEDNESDAY, Weekdays.THURSDAY, Weekdays.FRIDAY, Weekdays.SATURDAY, Weekdays.SUNDAY)
        //Perform selection
        selectedDays.forEach {day ->
            when (day) {
                Weekdays.MONDAY -> fabMonday.toggleSelectedState = true
                Weekdays.TUESDAY -> fabTuesday.toggleSelectedState = true
                Weekdays.WEDNESDAY -> fabWednesday.toggleSelectedState = true
                Weekdays.THURSDAY -> fabThursday.toggleSelectedState = true
                Weekdays.FRIDAY -> fabFriday.toggleSelectedState = true
                Weekdays.SATURDAY -> fabSaterday?.toggleSelectedState = true
                Weekdays.SUNDAY -> fabSunday?.toggleSelectedState = true
            }
            daysToUnselect.remove(day)
        }

        //Perform deselect
        daysToUnselect.forEach { day ->
            when (day) {
                Weekdays.MONDAY -> fabMonday.toggleSelectedState = false
                Weekdays.TUESDAY -> fabTuesday.toggleSelectedState = false
                Weekdays.WEDNESDAY -> fabWednesday.toggleSelectedState = false
                Weekdays.THURSDAY -> fabThursday.toggleSelectedState = false
                Weekdays.FRIDAY -> fabFriday.toggleSelectedState = false
                Weekdays.SATURDAY -> fabSaterday?.toggleSelectedState = false
                Weekdays.SUNDAY -> fabSunday?.toggleSelectedState = false
            }
        }
    }

    fun setDayFocusedListener(dayFocusedListener: DayFocusedListener) {
        this.dayFocusedListener = dayFocusedListener
    }
}