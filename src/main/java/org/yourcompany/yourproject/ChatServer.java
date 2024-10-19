package org.yourcompany.yourproject;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ChatServer {
    private static final int PORT = 8080;
    private NioSocketAcceptor acceptor;

    public ChatServer() throws IOException {
        acceptor = new NioSocketAcceptor();
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));

        acceptor.setHandler(new ServerHandler());
        acceptor.bind(new InetSocketAddress(PORT));

        System.out.println("Chat server started on port " + PORT);
    }

    public static void main(String[] args) throws IOException {
        new ChatServer();
    }

    private static class ServerHandler extends IoHandlerAdapter {
        @Override
        public void messageReceived(IoSession session, Object message) {
            if (message instanceof Message) {
                Message msg = (Message) message;

                // Broadcast to all clients
                for (IoSession sess : session.getService().getManagedSessions().values()) {
                    if (sess != session) { // Avoid sending back to the sender
                        sess.write(msg);
                    }
                }
            }
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause) {
            cause.printStackTrace();
            session.closeNow();
        }
    }
}