package org.jsoup.mytests;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ManualTest {
    @Test public void testSetHtmlIframe() {
        Document doc = Jsoup.parse("<html><body><iframe id=1></iframe></body></html>");
        Element iframe = doc.getElementById("1");
        iframe.html("<p>iframe</p>");
        assertEquals("&lt;p&gt;iframe&lt;/p&gt;", iframe.html());
    }
    
    @Test public void testSetHtmlNoembed() {
        Document doc = Jsoup.parse("<html><body><noembed id=1></noembed></body></html>");
        Element noembed = doc.getElementById("1");
        noembed.html("<p>noembed</p>");
        assertEquals("&lt;p&gt;noembed&lt;/p&gt;", noembed.html());
    }

    @Test public void testSetHtmlNoframes() {
        Document doc = Jsoup.parse("<html><body><noframes id=1></noframes></body></html>");
        Element noframes = doc.getElementById("1");
        noframes.html("<p>noframes</p>");
        assertEquals("&lt;p&gt;noframes&lt;/p&gt;", noframes.html());
    }

    @Test public void testSetHtmlStyle() {
        Document doc = Jsoup.parse("<html><head><style id=1></style></head></html>");
        Element style = doc.getElementById("1");
        style.html("body {color: white;}");
        assertEquals("body {color: white;}", style.html());
    }

    @Test public void testSetHtmlXmp() {
        Document doc = Jsoup.parse("<html><body><xmp id=1></xmp></body></html>");
        Element xmp = doc.getElementById("1");
        xmp.html("<p>xmp</p>");
        assertEquals("&lt;p&gt;xmp&lt;/p&gt;", xmp.html());
    }

    @Test public void testSetHtmlScript() {
        Document doc = Jsoup.parse("<html><body><script id=1></script></body></html>");
        Element script = doc.getElementById("1");
        script.html("console.log('script');");
        assertEquals("console.log('script');", script.html());
    }

    @Test public void testSetHtmlNoscript() {
        Document doc = Jsoup.parse("<html><body><noscript id=1></noscript></body></html>");
        Element noscript = doc.getElementById("1");
        noscript.html("<p>noscript</p>");
        assertEquals("<p>noscript</p>", noscript.html());
    }

    @Test public void testSetHtmlPlaintext() {
        Document doc = Jsoup.parse("<html><body><plaintext id=1></plaintext></body></html>");
        Element plaintext = doc.getElementById("1");
        plaintext.html("plaintext");
        assertEquals("plaintext", plaintext.html());
    }
}