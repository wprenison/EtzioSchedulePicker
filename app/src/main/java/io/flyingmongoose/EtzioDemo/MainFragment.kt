package io.flyingmongoose.EtzioDemo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import io.flyingmongoose.etzioDayPicker.DayFocusedListener
import io.flyingmongoose.etzioDayPicker.DayPicker
import io.flyingmongoose.etzioDayPicker.DayVModel
import io.flyingmongoose.etzioTimePicker.TimePicker
import io.flyingmongoose.etzioTimePicker.TimeChangedListener
import kotlinx.android.synthetic.main.ampm_picker.*
import java.util.*

class MainFragment : Fragment(), TimeChangedListener
{

    companion object {
        const val idKey = "layoutID"
        fun newInstance(layoutID: Int): MainFragment
        {
            val fragment = MainFragment()
            fragment.layoutID = layoutID
            return fragment
        }
    }

    private var layoutID = 0

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt(idKey, layoutID)
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (bundle?.getInt(idKey) != null) {
            layoutID = bundle.getInt(idKey)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(layoutID, container, false)
        if (layoutID == R.layout.ampm_picker) {
            with(v.findViewById<View>(R.id.amPicker) as TimePicker) {
                setClockColor(resources.getColor(R.color.clockColor))
                setDialColor(resources.getColor(R.color.dialColor))
                setTime(12, 45, io.flyingmongoose.etzioTimePicker.TimePicker.AM)
                setTrackSize(20)
                setDialRadiusDP(60)
                val checkBox = v.findViewById<View>(R.id.checkbox) as CheckBox
                this.isEnabled = checkBox.isChecked
                checkBox.setOnCheckedChangeListener { buttonView, isChecked -> this.isEnabled = isChecked }
            }

            with(v.findViewById<View>(R.id.dayPicker) as DayPicker) {
                setDayFocusedListener(object : DayFocusedListener
                {
                    override fun dayFocused(dayFocused: DayVModel.Weekdays?)
                    {
                        if(dayFocused == null)
                        {
                            amPicker.visibility = View.INVISIBLE
                            return
                        }


                        amPicker.visibility = View.VISIBLE
                        amPicker.setDayText(dayFocused.name)
                    }
                })
            }
        } else {
            val picker = v.findViewById<View>(R.id.picker) as TimePicker
            picker.setTimeChangedListener(this)
            val et = v.findViewById<View>(R.id.et) as TextView
            val btn = v.findViewById<View>(R.id.btn) as Button
            btn.setOnClickListener {
                var minute = picker.currentMin.toString()
                if (picker.currentMin < 10) {
                    minute = "0$minute"
                }
                et.text = "It's " + picker.currentHour + ":" + minute
            }
        }
        return v
    }

    override fun timeChanged(date: Date?) {
        Log.i("Time changed: ", date.toString())
    }
}
