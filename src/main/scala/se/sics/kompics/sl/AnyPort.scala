package se.sics.kompics.sl

import se.sics.kompics.KompicsEvent

trait AnyPort {
    def uponEvent(handler: Handler): Handler;
}