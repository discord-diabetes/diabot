package com.dongtronic.diabot.exceptions

import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO

class NightscoutFetchException(val userDTO: NightscoutUserDTO, val originalException: Throwable) : Exception()
