package com.dongtronic.diabot.exceptions

class NightscoutPrivateException(displayName: String? = null) :
    NightscoutDataException("Nightscout data for ${displayName ?: "this nightscout"} is private")
