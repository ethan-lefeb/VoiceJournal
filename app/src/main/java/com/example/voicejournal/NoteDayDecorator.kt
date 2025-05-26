package com.example.voicejournal

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class NoteDayDecorator(
    private val datesWithNotes: Set<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return datesWithNotes.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        // Example: Change the text color to orange
        view.addSpan(ForegroundColorSpan(Color.parseColor("#FFA726")))
        // You could also use view.setBackgroundDrawable() with a custom shape drawable
    }
}
