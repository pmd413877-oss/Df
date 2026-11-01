package com.example.automation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import java.net.URLEncoder

sealed class AutomationResult {
    data class Success(val message: String, val intentLaunched: Boolean = true) : AutomationResult()
    data class Failure(val errorMessage: String) : AutomationResult()
}

class ZaraAutomationManager(private val context: Context) {

    /**
     * Opens an installed application based on its Bengali or English name.
     */
    fun openApp(rawName: String): AutomationResult {
        val appName = rawName.trim().lowercase()

        // 1. Direct hardware & system features
        when {
            appName.contains("ক্যামেরা") || appName.contains("camera") -> {
                return try {
                    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AutomationResult.Success("ক্যামেরা ওপেন করা হচ্ছে।")
                } catch (e: Exception) {
                    AutomationResult.Failure("ক্যামেরা চালু করতে সমস্যা হয়েছে: ${e.localizedMessage}")
                }
            }

            appName.contains("সেটিংস") || appName.contains("settings") -> {
                return try {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AutomationResult.Success("ডিভাইস সেটিংস ওপেন করা হচ্ছে।")
                } catch (e: Exception) {
                    AutomationResult.Failure("সেটিংস চালু করতে সমস্যা হয়েছে।")
                }
            }

            appName.contains("ইউটিউব") || appName.contains("youtube") -> {
                return launchPackageOrUrl("com.google.android.youtube", "https://www.youtube.com", "ইউটিউব")
            }

            appName.contains("হোয়াটসঅ্যাপ") || appName.contains("whatsapp") -> {
                return launchPackageOrUrl("com.whatsapp", "https://web.whatsapp.com", "হোয়াটসঅ্যাপ")
            }

            appName.contains("ফেসবুক") || appName.contains("facebook") -> {
                return launchPackageOrUrl("com.facebook.katana", "https://www.facebook.com", "ফেসবুক")
            }

            appName.contains("প্লে স্টোর") || appName.contains("play store") || appName.contains("playstore") -> {
                return launchPackageOrUrl("com.android.vending", "https://play.google.com", "গুগল প্লে স্টোর")
            }

            appName.contains("ম্যাপস") || appName.contains("maps") || appName.contains("ম্যাপ") -> {
                return launchPackageOrUrl("com.google.android.apps.maps", "https://maps.google.com", "গুগল ম্যাপস")
            }

            appName.contains("ব্রাউজার") || appName.contains("browser") || appName.contains("chrome") || appName.contains("ক্রোম") -> {
                return try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AutomationResult.Success("ওয়েব ব্রাউজার ওপেন করা হচ্ছে।")
                } catch (e: Exception) {
                    AutomationResult.Failure("ব্রাউজার খুলতে সমস্যা হয়েছে।")
                }
            }

            appName.contains("ডায়ালার") || appName.contains("ফোন") || appName.contains("dialer") || appName.contains("phone") -> {
                return try {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    AutomationResult.Success("ফোন ডায়ালার চালু করা হচ্ছে।")
                } catch (e: Exception) {
                    AutomationResult.Failure("ফোন ডায়ালার ওপেন করা যায়নি।")
                }
            }
        }

        // 2. Query installed apps from PackageManager
        val pm: PackageManager = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(appName) || appName.contains(label)) {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return AutomationResult.Success("$label অ্যাপটি চালু করা হচ্ছে।")
                }
            }
        }

        // Fallback: If not found, offer to search in Google Play Store
        return installApp(appName)
    }

    /**
     * Automates searching on YouTube.
     */
    fun searchYouTube(query: String): AutomationResult {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return AutomationResult.Failure("ইউটিউবে কী খুঁজবেন তা উল্লেখ করুন।")
        }

        return try {
            val encoded = URLEncoder.encode(trimmedQuery, "UTF-8")
            val webUri = Uri.parse("https://www.youtube.com/results?search_query=$encoded")

            // Try direct YouTube intent
            val appIntent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra("query", trimmedQuery)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(appIntent)
                AutomationResult.Success("ইউটিউবে '$trimmedQuery' সার্চ করা হচ্ছে।")
            } catch (e: Exception) {
                // Fallback to web browser YouTube search
                val browserIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                AutomationResult.Success("ইউটিউবে '$trimmedQuery' অনুসন্ধান চালু করা হয়েছে।")
            }
        } catch (e: Exception) {
            AutomationResult.Failure("ইউটিউব সার্চ শুরু করতে সমস্যা হয়েছে: ${e.localizedMessage}")
        }
    }

    /**
     * Automates searching the web with Google.
     */
    fun searchWeb(query: String): AutomationResult {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return AutomationResult.Failure("অনুসন্ধানের বিষয়বস্তু বলুন।")
        }

        return try {
            val encoded = URLEncoder.encode(trimmedQuery, "UTF-8")
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, trimmedQuery)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(intent)
                AutomationResult.Success("গুগলে '$trimmedQuery' সার্চ করা হচ্ছে।")
            } catch (e: Exception) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(browserIntent)
                AutomationResult.Success("ওয়েবে '$trimmedQuery' ফলাফল দেখানো হচ্ছে।")
            }
        } catch (e: Exception) {
            AutomationResult.Failure("ওয়েব সার্চ চালু করা যায়নি: ${e.localizedMessage}")
        }
    }

    /**
     * Directs to Google Play Store to install an app.
     */
    fun installApp(appName: String): AutomationResult {
        val trimmed = appName.trim()
        return try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val marketUri = Uri.parse("market://search?q=$encoded")
            val marketIntent = Intent(Intent.ACTION_VIEW, marketUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            try {
                context.startActivity(marketIntent)
                AutomationResult.Success("প্লে স্টোরে '$trimmed' অ্যাপ খোঁজা হচ্ছে...")
            } catch (e: Exception) {
                val webUri = Uri.parse("https://play.google.com/store/search?q=$encoded&c=apps")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
                AutomationResult.Success("প্লে স্টোর থেকে '$trimmed' ইনস্টল করার পেজ ওপেন হচ্ছে।")
            }
        } catch (e: Exception) {
            AutomationResult.Failure("প্লে স্টোর খোলা সম্ভব হয়নি।")
        }
    }

    /**
     * Dials a phone number.
     */
    fun dialNumber(phoneNumber: String): AutomationResult {
        val digits = phoneNumber.filter { it.isDigit() || it == '+' }
        if (digits.isEmpty()) {
            return AutomationResult.Failure("সঠিক ফোন নম্বর প্রদান করুন।")
        }

        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult.Success("$digits নম্বরে কল ডায়াল করা হচ্ছে...")
        } catch (e: Exception) {
            AutomationResult.Failure("কল ডায়াল করতে সমস্যা হয়েছে।")
        }
    }

    /**
     * Composes SMS message.
     */
    fun composeSms(recipient: String, messageText: String): AutomationResult {
        return try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${recipient.filter { it.isDigit() || it == '+' }}")
                putExtra("sms_body", messageText)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            AutomationResult.Success("বার্তা পাঠানোর জন্য মেসেজিং অ্যাপ প্রস্তুত।")
        } catch (e: Exception) {
            AutomationResult.Failure("মেসেজিং অ্যাপ চালু করা যায়নি।")
        }
    }

    private fun launchPackageOrUrl(packageName: String, fallbackUrl: String, displayName: String): AutomationResult {
        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(launchIntent)
            AutomationResult.Success("$displayName অ্যাপটি চালু করা হচ্ছে।")
        } else {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(browserIntent)
            AutomationResult.Success("$displayName ব্রাউজারে ওপেন করা হচ্ছে।")
        }
    }
}
