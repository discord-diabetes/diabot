package com.dongtronic.diabot.exceptions

class NightscoutPrivateException(displayName: String = "this nightscout")
    : NightscoutDataException("Nightscout data for $displayName is private")