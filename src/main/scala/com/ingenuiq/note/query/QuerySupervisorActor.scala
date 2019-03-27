package com.ingenuiq.note.query

import akka.actor.{ Actor, ActorRef, Props }
import com.ingenuiq.note.query.events.{ NoteEventQuery, NoteEventView, NoteEventViewBuilder }
import com.ingenuiq.note.query.note._
import com.ingenuiq.note.utils.BackoffActorHelper

object QuerySupervisorActor {

  def apply(): Props =
    Props(classOf[QuerySupervisorActor])
}

class QuerySupervisorActor extends Actor with BackoffActorHelper with SingletonBootstrap {

  startSingleton(context.system, backoffActor(NoteViewBuilder.name, NoteViewBuilder()), NoteViewBuilder.name)
  startSingleton(context.system, backoffActor(NoteEventViewBuilder.name, NoteEventViewBuilder()), NoteEventViewBuilder.name)

  val noteHistoryViewActor:      ActorRef = context.actorOf(backoffActor(NoteView.name, NoteView()))
  val noteEventHistoryViewActor: ActorRef = context.actorOf(backoffActor(NoteEventView.name, NoteEventView()))

  override def receive: Receive = {
    case e: NoteQuery      => noteHistoryViewActor.forward(e)
    case e: NoteEventQuery => noteEventHistoryViewActor.forward(e)
  }
}
