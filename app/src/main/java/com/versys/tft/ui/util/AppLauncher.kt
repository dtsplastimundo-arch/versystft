package com.versys.tft.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object AppLauncher {

    /** Abre la primera app instalada de la lista de paquetes candidatos. */
    fun launchFirstInstalled(context: Context, packages: List<String>, label: String) {
        val intent = packages.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }
        if (intent != null) {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            Toast.makeText(context, "$label no está instalada", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Navega a una gasolinera buscándola por nombre (o "gasolinera" si no hay nombre),
     * porque TelemetryState no incluye coordenadas. Waze si existe; si no, cualquier app con geo:.
     */
    fun navigateToFuelStation(context: Context, stationName: String?) {
        val query = Uri.encode(stationName?.takeIf { it.isNotBlank() } ?: "gasolinera")
        val waze = Intent(Intent.ACTION_VIEW, Uri.parse("waze://?q=$query&navigate=yes"))
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query"))
        val target = listOf(waze, geo).firstOrNull { it.resolveActivity(context.packageManager) != null }
        if (target != null) {
            context.startActivity(target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else {
            Toast.makeText(context, "No hay app de navegación instalada", Toast.LENGTH_SHORT).show()
        }
    }
}
