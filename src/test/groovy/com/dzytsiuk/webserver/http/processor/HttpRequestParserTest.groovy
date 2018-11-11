package com.dzytsiuk.webserver.http.processor

import com.dzytsiuk.webserver.http.HttpRequest
import org.junit.Test

import static org.junit.Assert.*

class HttpRequestParserTest {
    @Test
    void setRequestLanguage() {
        def expectedLocales = [1.0: 'uk-ua', 0.7: 'en-us', 0.6: 'en', 0.9: 'uk', 0.8: 'ru'] as HashMap<Double, Locale>
        HttpRequestParser parser = new HttpRequestParser()
        def httpRequest = new HttpRequest()
        parser.setRequestLanguage(httpRequest, "uk-UA,uk;q=0.9,ru;q=0.8,en-US;q=0.7,en;q=0.6")
        def actualLocales = httpRequest.localeMap
        expectedLocales.each { assertEquals(it.value, actualLocales[it.key as Double] as String) }
    }
}
