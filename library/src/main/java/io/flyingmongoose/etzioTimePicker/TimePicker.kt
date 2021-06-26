package io.flyingmongoose.etzioTimePicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import io.flyingmongoose.etzioTimePicker.library.R
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Custom time picker which extends Android's View class
 * @author Ugur Tekbas on 10.05.2015 (Original Fork: https://github.com/ugurtekbas/dialTimePicker)
 * @author Weylin Renison on 18.06.2021
 */
class TimePicker @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr)
{
    private val paint: Paint
    private val rectF: RectF
    private val dialXferMode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    private var min = 0f
    private var radius = 0f
    private var dialRadius = 0f
    private var offset = 0f
    private var slopX = 0f
    private var slopY = 0f
    private var dialX = 0f
    private var dialY = 0f
    private var hour = 0
    var currentMin = 0
        private set
    private var previousHour = 0

    //********TEXT PROPERTIES****************
    private var textColor = Color.WHITE
    private var prefixTextSizeInSp = 0
    private var prefixTextSizeInPixels = 0f
    private var prefixText = "Arrive"
    private var dayTextSizeInSp = 0
    private var dayTextSizeInPixels = 0f
    private var dayText = "MONDAY"
    private var suffixTextSizeInSp = 0
    private var suffixTextSizeInPixels = 0f
    private var suffixText = "At"
    private var timeTextSizeInSP = 0
    private var timeTextSizeInPixels = 0f
    private var amPmTextSizeInSp = 0
    private var amPmTextSizeInPixels = 0f

    private var clockColor = Color.parseColor("#54A0FF")
    private var clockFaceColor = Color.parseColor("#80222f3e")
    private var dialColor = Color.parseColor("#2E86DE")
    private var dialShadowColor = Color.parseColor("#48000000")
    private var canvasColor = Color.TRANSPARENT
    private var trackSize = 35
    private var dialRadiusDP = -1
    private var angle: Double
    private var degrees = 0.0
    private var isMoving = false
    private var amPm: Boolean
    private var disableTouch = false
    private var hourFormat: Boolean
    private var firstRun = true
    private var manualAdjust = false
    private var hStr: String? = null
    private var mStr: String? = null
    var amPM: String? = null
        private set
    private var timeListener: TimeChangedListener? = null

    //***************DEFAULT FACTORS********************************
    private val prefixTextSizeFactor = 18
    private val dayTextSizeFactor = 12
    private val suffixTextSizeFactor = 18
    private val timeTextSizeFactor = 6
    private val amPmTextSizeFactor = 12

    companion object
    {
        private const val AN_HOUR_AS_MINUTES = 60
        private const val A_DAY_AS_HOURS = 24
        private const val HALF_DAY_AS_HOURS = 12
        const val AM = true
        const val PM = false
    }

    init
    {
        if (Build.VERSION.SDK_INT >= 11)
        {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
        paint = Paint()
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        paint.textAlign = Paint.Align.CENTER
        rectF = RectF()
        angle = -Math.PI / 2 + .001
        hourFormat = DateFormat.is24HourFormat(getContext())
        amPm = Calendar.getInstance()[Calendar.AM_PM] == 0
        loadAppThemeDefaults()
        loadAttributes(attrs)
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
    private fun loadAttributes(attrs: AttributeSet?)
    {
        if (attrs != null)
        {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Picker)
            textColor = typedArray.getColor(R.styleable.Picker_textColor, textColor)
            dayTextSizeInSp = typedArray.getDimensionPixelSize(R.styleable.Picker_dayTextSize, dayTextSizeInSp)
            timeTextSizeInSP = typedArray.getDimensionPixelSize(R.styleable.Picker_timeTextSize, timeTextSizeInSP)
            amPmTextSizeInSp = typedArray.getDimensionPixelSize(R.styleable.Picker_amPmTextSize, amPmTextSizeInSp)
            dialColor = typedArray.getColor(R.styleable.Picker_dialColor, dialColor)
            clockColor = typedArray.getColor(R.styleable.Picker_clockColor, clockColor)
            clockFaceColor =
                typedArray.getColor(R.styleable.Picker_clockFaceColor, clockFaceColor)
            canvasColor = typedArray.getColor(R.styleable.Picker_canvasColor, canvasColor)
            hourFormat = typedArray.getBoolean(R.styleable.Picker_hourFormat, hourFormat)
            trackSize =
                typedArray.getDimensionPixelSize(R.styleable.Picker_trackSize, trackSize)
            dialRadiusDP =
                typedArray.getDimensionPixelSize(R.styleable.Picker_dialRadius, dialRadiusDP)
            typedArray.recycle()
        }
    }

    override fun setEnabled(enabled: Boolean)
    {
        super.setEnabled(enabled)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        val width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        min = min(width, height)
        setMeasuredDimension(min.toInt(), min.toInt())
        offset = min * 0.5f
        val padding = min / 20
        radius = min / 2 - padding * 2
        dialRadius = if (dialRadiusDP != -1) dialRadiusDP.toFloat() else radius / 7
        rectF[-radius, -radius, radius] = radius

        //Set defaults that require measure
        prefixTextSizeInPixels = when (prefixTextSizeInSp) {
            0 -> min / prefixTextSizeFactor
            else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, prefixTextSizeInSp.toFloat(), resources.displayMetrics)
        }

        dayTextSizeInPixels = when (dayTextSizeInSp)
        {
            0 -> min / dayTextSizeFactor
            else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dayTextSizeInSp.toFloat(), resources.displayMetrics)
        }

        suffixTextSizeInPixels = when (suffixTextSizeInSp)
        {
            0 -> min / suffixTextSizeFactor
            else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, suffixTextSizeInSp.toFloat(), resources.displayMetrics)
        }

        timeTextSizeInPixels = when (timeTextSizeInSP)
        {
            0 -> min / timeTextSizeFactor
            else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, timeTextSizeInSP.toFloat(), resources.displayMetrics)
        }

        amPmTextSizeInPixels = when (amPmTextSizeInSp)
        {
            0 -> min / amPmTextSizeFactor
            else -> TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, amPmTextSizeInSp.toFloat(), resources.displayMetrics)
        }
    }

    override fun onDraw(canvas: Canvas)
    {
        super.onDraw(canvas)
        canvas.translate(offset, offset)
        canvas.drawColor(canvasColor)

        //clock face
        with(paint) {
            style = Paint.Style.FILL
            color = clockFaceColor
            canvas.drawCircle(0f, 0f, (radius - (trackSize / 2)), this)
        }

        if (firstRun)
        {
            val cal = Calendar.getInstance()
            currentMin = cal[Calendar.MINUTE]
            hour = cal[Calendar.HOUR_OF_DAY]
            initTime(hour, currentMin)
        } else
        {
            //Rad to Deg
            degrees = (Math.toDegrees(angle) + 90) % 360
            degrees = (degrees + 360) % 360

            //get AM/PM
            if (hourFormat)
            {
                hour = degrees.toInt() / 15 % A_DAY_AS_HOURS
                /**
                 * When minutes are set programmatically, because of rounding issues,
                 * new value of minutes might be different than the one is set.
                 * To avoid that if statement checks if time setting is done programmatically or
                 * by touch gestures.
                 */
                if (manualAdjust)
                {
                    currentMin = (degrees * 4).toInt() % AN_HOUR_AS_MINUTES
                    manualAdjust = false
                }
                mStr = if (currentMin < 10) "0$currentMin" else currentMin.toString() + ""
                amPM = ""
            } else
            {
                if (manualAdjust)
                {
                    //get Minutes
                    currentMin = (degrees * 2).toInt() % AN_HOUR_AS_MINUTES
                    manualAdjust = false
                }
                hour = degrees.toInt() / 30 % HALF_DAY_AS_HOURS
                if (hour == 0) hour = HALF_DAY_AS_HOURS
                mStr = if (currentMin < 10) "0$currentMin" else currentMin.toString()
                //AM-PM
                if (hour == 12 && previousHour == 11 || hour == 11 && previousHour == 12)
                {
                    amPm = !amPm
                }
                amPM = if (amPm) "AM" else "PM"
            }
        }
        previousHour = hour

        //Draw Texts
        with(paint) {
            style = Paint.Style.FILL
            color = textColor
            alpha = if (isEnabled) paint.alpha else 77

            val offsetY = radius / 8

            //Prefix Text
            textSize = prefixTextSizeInPixels
            canvas.drawText(prefixText, -radius / 2.5f, (-radius / 1.65f) + offsetY, paint)

            //Day Text
            textSize = dayTextSizeInPixels
            canvas.drawText(dayText, 0f, (paint.textSize * -2f) + offsetY, paint)

            //Suffix Text
            textSize = suffixTextSizeInPixels
            canvas.drawText(suffixText, radius / 2.2f, (-radius / 3.5f) + offsetY, paint)

            //Time Text
            textSize = timeTextSizeInPixels
            hStr = if (hour < 10) "0$hour" else hour.toString() + ""
            canvas.drawText("$hStr:$mStr", 0f, (paint.textSize / 4) + offsetY, paint)

            //AM / PM Text
            textSize = amPmTextSizeInPixels
            canvas.drawText(amPM!!, 0f, (paint.textSize * 2) + offsetY, paint)
        }

        //clocks dial
        with(paint) {
            style = Paint.Style.STROKE
            setShadowLayer(20f, 8f, 10f, dialShadowColor)
            strokeWidth = trackSize.toFloat()
            color = clockColor
//            alpha = if (isEnabled) paint.alpha else 77
            canvas.drawOval(rectF, paint)
            paint.clearShadowLayer()
        }

        //small circle to adjust time
        with(paint) {
            calculatePointerPosition(angle)
            style = Paint.Style.FILL
            alpha = 0
            xfermode = dialXferMode
            canvas.drawCircle(dialX, dialY, dialRadius, paint)
            color = dialColor
            alpha = if (isEnabled) paint.alpha else 77
            xfermode = null
            canvas.drawCircle(dialX, dialY, dialRadius, paint)

            //Stroke
            style = Paint.Style.STROKE
            color = clockColor
            strokeWidth = trackSize / 1.5f
            canvas.drawCircle(dialX, dialY, dialRadius, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        if (disableTouch || !isEnabled) return false
        manualAdjust = true
        parent.requestDisallowInterceptTouchEvent(true)
        val posX = event.x - offset
        val posY = event.y - offset
        when (event.action)
        {
            MotionEvent.ACTION_DOWN ->
            {
                calculatePointerPosition(angle)
                if (posX >= dialX - dialRadius && posX <= dialX + dialRadius && posY >= dialY - dialRadius && posY <= dialY + dialRadius)
                {
                    slopX = posX - dialX
                    slopY = posY - dialY
                    isMoving = true
                    invalidate()
                } else
                {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> if (isMoving)
            {
                angle = atan2(
                    (posY - slopY).toDouble(),
                    (posX - slopX).toDouble()
                )
                timeListener?.timeChanged(time)
                invalidate()
            } else
            {
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
            {
                isMoving = false
                invalidate()
            }
        }
        return true
    }

    private fun calculatePointerPosition(angle: Double)
    {
        dialX = (radius * cos(angle)).toFloat()
        dialY = (radius * sin(angle)).toFloat()
    }

    //PM
    val currentHour: Int
        get()
        {
            var currentHour = hour
            return if (hourFormat)
            {
                currentHour
            } else
            {
                if (amPm)
                {
                    if (currentHour == 12)
                    {
                        currentHour = 0
                    }
                } else
                {
                    //PM
                    if (currentHour < 12)
                    {
                        currentHour += 12
                    }
                }
                currentHour
            }
        }

    fun setTextColor(textColor: Int)
    {
        this.textColor = textColor
        invalidate()
    }

    fun setDayText(newDayText: String) {
        this.dayText = newDayText
        invalidate()
    }

    fun setClockColor(clockColor: Int)
    {
        this.clockColor = clockColor
        invalidate()
    }

    fun setClockFaceColor(clockFaceColor: Int)
    {
        this.clockFaceColor = clockFaceColor
        invalidate()
    }

    fun setDialColor(dialColor: Int)
    {
        this.dialColor = dialColor
        invalidate()
    }

    fun setCanvasColor(canvasColor: Int)
    {
        this.canvasColor = canvasColor
        invalidate()
    }

    /**
     * To set dial's size
     * @param trackSize
     */
    fun setTrackSize(trackSize: Int)
    {
        this.trackSize = if (trackSize != -1) trackSize else (min / 25).toInt()
        invalidate()
    }

    /**
     * To set adjuster's size
     * @param dialRadiusDP
     */
    fun setDialRadiusDP(dialRadiusDP: Int)
    {
        this.dialRadiusDP = dialRadiusDP
    }

    /**
     * To disable/enable the picker
     * @param disableTouch
     */
    fun disableTouch(disableTouch: Boolean)
    {
        this.disableTouch = disableTouch
    }

    fun setHourFormat(format: Boolean)
    {
        hourFormat = format
    }

    val time: Date
        get()
        {
            val calendar = Calendar.getInstance()
            var tmp = hour
            if (!amPm)
            {
                if (tmp < 12) tmp += 12
            } else
            {
                if (tmp == 12) tmp = 0
            }
            calendar[Calendar.HOUR_OF_DAY] = tmp
            calendar[Calendar.MINUTE] = currentMin
            return calendar.time
        }

    fun setTimeChangedListener(timeChangedListener: TimeChangedListener?)
    {
        timeListener = timeChangedListener
    }

    /**
     * This method is used to set picker's time
     * @param hour
     * @param minute
     */
    fun setTime(hour: Int, minute: Int)
    {
        check(
            isTimeValid(
                hour,
                minute,
                true
            )
        ) { resources.getString(R.string.outOfRangeExceptionMessage) }

        // To handle AM/PM decision when time is set
        if (amPm && hour > 11)
        {
            amPm = !amPm
        } else if (!amPm && (hour < HALF_DAY_AS_HOURS || hour == A_DAY_AS_HOURS))
        {
            amPm = !amPm
        }
        initTime(hour, minute)
        this.invalidate()
    }

    /**
     * This method is used to set picker's time with AM/PM value
     * @param hour
     * @param minute
     * @param midday
     */
    fun setTime(hour: Int, minute: Int, midday: Boolean)
    {
        check(
            isTimeValid(
                hour,
                minute,
                false
            )
        ) { resources.getString(R.string.outOfRangeExceptionMessage2) }
        setHourFormat(false)
        amPm = midday
        initTime(hour, minute)
        this.invalidate()
    }

    /***
     * This method is used to initialize picker's time
     * @param hour
     * @param minutes
     */
    private fun initTime(hour: Int, minutes: Int)
    {
        this.hour = hour
        currentMin = minutes
        firstRun = true
        mStr = if (currentMin < 10) "0$currentMin" else currentMin.toString() + ""
        if (hourFormat)
        {
            amPM = ""
            degrees = (this.hour % A_DAY_AS_HOURS * 15 + currentMin % AN_HOUR_AS_MINUTES / 4).toDouble()
        } else
        {
            if (this.hour == 0) this.hour = HALF_DAY_AS_HOURS
            if (this.hour == 12 && previousHour == 11 || this.hour == 11 && previousHour == 12)
            {
                amPm = !amPm
            }
            amPM = if (amPm) "AM" else "PM"
            degrees = (this.hour % HALF_DAY_AS_HOURS * 30 + currentMin % AN_HOUR_AS_MINUTES / 2).toDouble()
        }
        angle = Math.toRadians(degrees) - Math.PI / 2
        firstRun = false
    }

    /**
     * Checks if time values are between valid range
     * @param hour
     * @param minute
     * @param is24Hour if time is set as 24hour format
     * @return true if value is valid, false otherwise
     */
    private fun isTimeValid(hour: Int, minute: Int, is24Hour: Boolean): Boolean
    {
        return if (is24Hour)
        {
            (hour in 0..A_DAY_AS_HOURS
                    && minute >= 0 && minute <= AN_HOUR_AS_MINUTES)
        } else
        {
            (hour in 0..HALF_DAY_AS_HOURS
                    && minute >= 0 && minute <= AN_HOUR_AS_MINUTES)
        }
    }
}