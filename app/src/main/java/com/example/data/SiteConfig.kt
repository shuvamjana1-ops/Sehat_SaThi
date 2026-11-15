package com.example.data

import java.util.Calendar

/**
 * Central brand and support contact configuration.
 * Keep optional contact values blank until verified real support details are provided.
 */
object SiteConfig {
  const val APP_NAME = "SehatSaathi"
  const val EMERGENCY_NUMBER = "112"

  // Leave blank until real support details are configured.
  const val SUPPORT_PHONE = ""
  const val SUPPORT_EMAIL = ""
  const val SUPPORT_ADDRESS = ""

  fun currentCopyrightYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

  fun copyrightNotice(year: Int = currentCopyrightYear()): String =
    "© $year $APP_NAME. All rights reserved."
}
