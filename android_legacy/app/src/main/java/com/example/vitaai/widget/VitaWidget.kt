package com.example.vitaai.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.vitaai.data.VitaRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun vitaRepository(): VitaRepository
}

class VitaWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val repository = entryPoint.vitaRepository()
        val snapshot = try {
            repository.getDailySnapshot()
        } catch (e: Exception) {
            null
        }

        val activeCalories = snapshot?.calories ?: 0.0

        provideContent {
            WidgetContent(activeCalories)
        }
    }
}

@Composable
fun WidgetContent(activeCalories: Double) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Active Burn",
            style = TextStyle(
                color = androidx.glance.unit.ColorProvider(Color(0xFF00E5FF)),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(GlanceModifier.height(8.dp))
        Text(
            text = String.format(Locale.US, "%.0f KCAL", activeCalories),
            style = TextStyle(
                color = androidx.glance.unit.ColorProvider(Color.White),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

class VitaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VitaWidget()
}
