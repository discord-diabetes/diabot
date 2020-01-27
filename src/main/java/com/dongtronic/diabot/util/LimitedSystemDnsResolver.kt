package com.dongtronic.diabot.util

import org.apache.http.impl.conn.SystemDefaultDnsResolver
import java.net.InetAddress

class LimitedSystemDnsResolver(private val limit: Int = 2) : SystemDefaultDnsResolver() {
    /**
     * Resolves IP addresses for a host, limiting to the amount in the `max` parameter
     */
    fun resolve(host: String?, max: Int): Array<InetAddress> {
        return super.resolve(host).take(max).toTypedArray()
    }

    /**
     * Resolves a limited number of IP addresses for a host
     */
    override fun resolve(host: String?): Array<InetAddress> {
        return resolve(host, this.limit)
    }
}