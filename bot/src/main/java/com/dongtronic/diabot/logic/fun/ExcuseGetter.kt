package com.dongtronic.diabot.logic.`fun`

import org.jsoup.Jsoup

object ExcuseGetter {
    fun get(): String {
        val html = Jsoup.connect("http://programmingexcuses.com/").get()
        val excuse = html.select("a").first().text()

        return if (excuse.isNullOrEmpty()) {
            "Error"
        } else {
            excuse
        }
    }
}
