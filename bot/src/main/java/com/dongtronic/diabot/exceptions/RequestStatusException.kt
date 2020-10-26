package com.dongtronic.diabot.exceptions

class RequestStatusException(var status: Int) : Exception("Unexpected status code from HTTP request: $status")
