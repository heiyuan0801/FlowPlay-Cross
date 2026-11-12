package app.flowplay.cross.provider

import app.flowplay.cross.network.CatalogException
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

internal actual fun parseCmsXml(text: String): CmsResponse {
    if (Regex("<!\\s*(DOCTYPE|ENTITY)", RegexOption.IGNORE_CASE).containsMatchIn(text)) {
        throw CatalogException("CMS XML 含 DTD 或实体声明，已拒绝解析。")
    }
    try {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isExpandEntityReferences = false
            listOf(
                "http://xml.org/sax/features/external-general-entities",
                "http://xml.org/sax/features/external-parameter-entities",
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
            ).forEach { feature ->
                try { setFeature(feature, false) } catch (_: Exception) { /* Declaration rejection also protects platforms with limited XML features. */ }
            }
        }
        val builder = factory.newDocumentBuilder().apply {
            setEntityResolver { _, _ -> throw CatalogException("XML 外部实体已被拒绝。") }
            setErrorHandler(object : org.xml.sax.helpers.DefaultHandler() {
                override fun fatalError(error: org.xml.sax.SAXParseException) { throw error }
                override fun error(error: org.xml.sax.SAXParseException) { throw error }
            })
        }
        val document = builder.parse(InputSource(StringReader(text)))
        val root = document.documentElement
        require(root.tagName in listOf("rss", "list"))
        val list = if (root.tagName == "list") root else root.getElementsByTagName("list").item(0) as? Element
        val classes = root.getElementsByTagName("class")
        val categories = mutableListOf<CmsCategory>()
        for (index in 0 until classes.length) {
            val children = (classes.item(index) as Element).getElementsByTagName("ty")
            for (child in 0 until children.length) {
                val category = children.item(child) as Element
                if (category.getAttribute("id").isNotBlank()) {
                    categories.add(CmsCategory(category.getAttribute("id"), plainText(category.textContent)))
                }
            }
        }
        val nodes = root.getElementsByTagName("video")
        val videos = mutableListOf<CmsVideo>()
        for (index in 0 until nodes.length) {
            val node = nodes.item(index) as Element
            val fields = mutableMapOf<String, String>()
            val children = node.childNodes
            for (child in 0 until children.length) {
                val element = children.item(child) as? Element ?: continue
                fields[element.tagName] = element.textContent.trim()
            }
            val lines = node.getElementsByTagName("dd")
            val xmlLines = (0 until lines.length).map { lineIndex ->
                val line = lines.item(lineIndex) as Element
                line.getAttribute("flag") to line.textContent
            }
            fields.video(xmlLines)?.let(videos::add)
        }
        fun number(key: String) = list?.getAttribute(key)?.toIntOrNull()
        return CmsResponse(categories.distinctBy { it.id }, videos, number("page") ?: 1,
            number("pagecount"), number("recordcount") ?: number("total"), number("pagesize"))
    } catch (error: CatalogException) {
        throw error
    } catch (_: Exception) {
        throw CatalogException("CMS XML 解析失败，需要 rss / list 格式的响应。")
    }
}
