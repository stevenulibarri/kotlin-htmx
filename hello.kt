import java.io.OutputStream
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange

fun main() {
  val server = HttpServer.create(InetSocketAddress(8080), 0)
  server.createContext("/todos", TodoHandler())
  server.createContext("/", RootHandler())
  server.executor = Executors.newFixedThreadPool(10)
  server.start()
  println("Server started on port 8080")
}

class RootHandler : HttpHandler {
  override fun handle(exchange: HttpExchange) {
    val html = File("index.html").readText()
    exchange.responseHeaders.set("Content-Type", "text/html")
    exchange.sendResponseHeaders(200, html.length.toLong())
    val outputStream: OutputStream = exchange.responseBody
    outputStream.write(html.toByteArray())
    outputStream.close()
  }
}

class TodoHandler : HttpHandler {
  private val todos = HashMap<Int, String>()

  override fun handle(exchange: HttpExchange) {
    val method = exchange.requestMethod
    when (method) {
      "GET" -> handleGet(exchange)
      "POST" -> handlePost(exchange)
      "DELETE" -> handleDelete(exchange)
      else -> sendResponse(exchange, 405, "Method Not Allowed", "text/utf-8")
    }
  }

  private fun extractIdFromPath(path: String): Int? {
    val idString = path.substringAfterLast("/")
    return idString.toIntOrNull()
  }

  private fun buildTodoList(): String {
    val stringBuilder = StringBuilder()
    stringBuilder.append("<div id='container'>")
    todos.entries.forEach { (id, description) ->
      stringBuilder.append("<div>")
      stringBuilder.append("$id: $description")
      stringBuilder.append("<button hx-delete='/todos/$id' hx-target='#container'>Delete</button>")
      stringBuilder.append("</div>")
    }
    stringBuilder.append("</div>")
    return stringBuilder.toString()
  }

  private fun handleGet(exchange: HttpExchange) {
    val response = buildTodoList()
    sendResponse(exchange, 200, response, "text/html")
  }

  private fun handlePost(exchange: HttpExchange) {
    val requestBody = exchange.requestBody.bufferedReader().use { it.readText() }
    val pairs = requestBody.split("&")
    val id = pairs[0].split("=")[1].toInt()
    val description = pairs[1].split("=")[1]
    todos[id] = description
    val html = buildTodoList()
    sendResponse(exchange, 200, html, "text/html")
  }

  private fun handleDelete(exchange: HttpExchange) {
    val id = extractIdFromPath(exchange.requestURI.path)
    if (id != null && todos.containsKey(id)) {
      todos.remove(id)
    }
    val html = buildTodoList()
    sendResponse(exchange, 200, html, "text/html")
  }

  private fun sendResponse(exchange: HttpExchange, statusCode: Int, response: String, contentType: String) {
    exchange.responseHeaders.set("Content-Type", contentType)
    exchange.sendResponseHeaders(statusCode, response.length.toLong())
    val outputStream: OutputStream = exchange.responseBody
    outputStream.write(response.toByteArray())
    outputStream.close()
  }
}
