package com.wfairclough.rsvp.server.controllers

import com.google.gson.*
import com.wfairclough.rsvp.server.dao.InvitationDao
import com.wfairclough.rsvp.server.model.fold
import com.wfairclough.rsvp.server.model.orElse
import com.wfairclough.rsvp.server.model.tryCatch
import io.vertx.core.Handler
import io.vertx.core.http.ServerWebSocket
import java.lang.reflect.Type

/**
 * Created by will on 29/06/17.
 */
class WebSocketHandler : Handler<ServerWebSocket> {

    val gson by lazy { GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(BaseCommand::class.java, JsonPacketDeserializer())
            .create() }

    override fun handle(serverWebSocket: ServerWebSocket?) {
        val webSocket: ServerWebSocket = serverWebSocket ?: return

        webSocket.textMessageHandler { msg ->
            val cmdEither = tryCatch<BaseCommand> { gson.fromJson(msg, BaseCommand::class.java) }

            val cmd = cmdEither.orElse { ErrorCmd(it) }

            when (cmd) {
                is UnknownCmd -> {
                    webSocket.writeTextMessage(cmd.asJson())
                }
                is ErrorCmd -> {
                    webSocket.writeTextMessage(cmd.asJson())
                }
                is ProcessableCommand -> {
                    cmd.process { resp ->
                        webSocket.writeTextMessage(resp)
                    }
                }
            }
        }

        webSocket.endHandler {

        }

    }

}

typealias ProcessCallback = (String) -> Unit

abstract class BaseCommand(val type: CommandType) {
    fun asJson(): String {
        return Gson().toJson(this)
    }
}

class UnknownCmd : BaseCommand(Commands.UNKNOWN)

data class ErrorCmd(val throwable: Throwable) : BaseCommand(Commands.ERROR)

abstract class ProcessableCommand(type: CommandType) : BaseCommand(type) {
    abstract fun process(callback: ProcessCallback)
}



data class QueryByCodeCmd(val code: String) : ProcessableCommand(Commands.QUERY_BY_CODE) {
    override fun process(callback: ProcessCallback) {
        val invite = InvitationDao().findByCode(code)
        callback(Gson().toJson(invite))
    }
}

data class FindAllCmd(val skip: Int, val limit: Int) : ProcessableCommand(Commands.FIND_ALL) {
    override fun process(callback: ProcessCallback) {
        val invites = InvitationDao().findAll(skip, limit)
        callback(Gson().toJson(invites))
    }
}


class JsonPacketDeserializer : JsonDeserializer<BaseCommand> {

    val gson = Gson()

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): BaseCommand {
        val cmdType = json?.asJsonObject?.get("type")?.asString as? CommandType

        cmdType?.let {
            return tryCatch<BaseCommand> { gson.fromJson(json, it.typeClass) }.orElse { throwable ->
                when (throwable) {
                    is NoSuchTypeError -> UnknownCmd()
                    else -> ErrorCmd(throwable)
                }
            }
        } ?: return UnknownCmd()
    }
}

typealias CommandType = String

val CommandType.typeClass: Type
    get() = Commands.classes[this] ?: throw NoSuchTypeError(this)

object Commands {
    val QUERY_BY_CODE: CommandType = "QUERY_BY_CODE"
    val FIND_ALL: CommandType = "FIND_ALL"
    val UNKNOWN: CommandType = "UNKNOWN"
    val ERROR: CommandType = "ERROR"
    val classes : Map<CommandType, Type> = mapOf(
            QUERY_BY_CODE to QueryByCodeCmd::class.java,
            FIND_ALL to FindAllCmd::class.java,
            ERROR to ErrorCmd::class.java)
}

data class NoSuchTypeError(val type: String) : Exception("No such type: $type")