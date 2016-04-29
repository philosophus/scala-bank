package com.fdahms.scalabank.zmq

import org.zeromq.ZMQ.{Poller, PollItem}
import org.zeromq.{ZMQ, ZContext}

/** Scala implementation of the lazy pirate pattern
  *
  * @param context ZMQContext
  * @param endpoint The servers endpoint
  * @param connect If set to true the pirate will connect, otherwise it will bind (which would be somehow unorthodox - but who am I to judge)
  * @param retries Number of retries before giving up
  * @param timeout Timeout in milliseconds before declaring a message for dead
  */
class LazyPirate(context: ZContext,
                 endpoint: String,
                 connect: Boolean = true,
                 retries: Int = 3,
                 timeout: Int = 2500) {

  private var socket: ZMQ.Socket = null

  private def initSocket: ZMQ.Socket = {
    val socket = context.createSocket(ZMQ.REQ)
    assert (socket != null)
    if (connect) {
      socket.connect(endpoint)
    } else {
      socket.bind(endpoint)
    }

    socket
  }

  /** Perform a request - response cycle with retries.
    *
    * If the server did not respond within the specified timeout
    * the lazy pirate will perform a specified number of retries
    * until giving up and returning None.
    *
    * @param request The request as a raw Array of Bytes
    * @return The response as an optional raw Array of Bytes
    */
  def reqResp(request: Array[Byte]): Option[Array[Byte]] = {
    var reply: Array[Byte] = null
    var retriesLeft = retries

    while (reply == null && retriesLeft > 0) {
      if (socket == null) socket = initSocket
      socket.send(request)

      val items = Array(new PollItem(socket, Poller.POLLIN))
      val rc = ZMQ.poll(items, timeout)

      if (rc != -1 && items.head.isReadable) {
        reply = socket.recv
      } else {
        retriesLeft -= 1
        context.destroySocket(socket)
        socket = null
      }
    }

    Option(reply)
  }
}
