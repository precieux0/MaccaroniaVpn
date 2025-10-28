package com.maccaronia.vpn.utils

import android.content.Context
import android.content.Intent
import com.maccaronia.vpn.AboutActivity
import com.maccaronia.vpn.FAQActivity

object NavigationUtils {
    
    fun openFAQActivity(context: Context) {
        val intent = Intent(context, FAQActivity::class.java)
        context.startActivity(intent)
    }
    
    fun openAboutActivity(context: Context) {
        val intent = Intent(context, AboutActivity::class.java)
        context.startActivity(intent)
    }
}