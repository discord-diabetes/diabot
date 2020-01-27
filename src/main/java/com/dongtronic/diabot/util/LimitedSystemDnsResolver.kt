package com.dongtronic.diabot.util

import org.apache.http.impl.conn.SystemDefaultDnsResolver
import java.net.InetAddress

class LimitedSystemDnsResolver: SystemDefaultDnsResolver() {
    /**
     * Resolves IP addresses for a host, limiting to the amount in the `limit` parameter
     */
    fun resolve(host: String?, limit: Int): Array<InetAddress> {
        return super.resolve(host).take(limit).toTypedArray()
    }

    /**
     * Resolves IP addresses for a host, limiting to the first 2 IP addresses
     */
    override fun resolve(host: String?): Array<InetAddress> {
        return resolve(host, 2)
    }

    companion object {
        private var instance: LimitedSystemDnsResolver? = null

        fun getInstance(): LimitedSystemDnsResolver {
            if (instance == null) {
                instance = LimitedSystemDnsResolver()
            }
            return instance as LimitedSystemDnsResolver
        }
    }
}