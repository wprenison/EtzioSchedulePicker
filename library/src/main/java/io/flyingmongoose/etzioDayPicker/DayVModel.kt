package io.flyingmongoose.etzioDayPicker

import androidx.lifecycle.ViewModel
import io.flyingmongoose.data.MutableLiveList

class DayVModel : ViewModel()
{
    enum class Weekdays
    {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY
    }

    //********View Attributes**********
    var includeWeekends = false

    var prevDayFocused: Weekdays? = null
    var selectedDays: MutableLiveList<Weekdays> = MutableLiveList()

    fun getFirstSelectedDay(): Weekdays?
    {
        return if (selectedDays.size > 0) selectedDays[0] else null
    }

    fun selectWeekday(weekday: Weekdays)
    {
        selectedDays.addDistinct(weekday)
    }

    fun isDaySelected(weekday: Weekdays): Boolean
    {
        return selectedDays.contains(weekday)
    }

    fun handleDayClicked(weekdayClicked: Weekdays, dayFocusedListener: DayFocusedListener?)
    {
        var dayFocused: Weekdays? = weekdayClicked
        when (isDaySelected(weekdayClicked))
        {
            false ->
                selectWeekday(weekdayClicked)
            true ->
                if (prevDayFocused == weekdayClicked)
                {
                    selectedDays.remove(weekdayClicked)
                    dayFocused = getFirstSelectedDay()
                }
        }

        prevDayFocused = dayFocused
        dayFocusedListener?.dayFocused(dayFocused)
    }
}