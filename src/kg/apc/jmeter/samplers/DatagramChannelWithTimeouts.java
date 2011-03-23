package kg.apc.jmeter.samplers;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * SocketChannel with timeouts.
 * This class performs blocking operations for connect and IO.
 * Make note that some of methods are not implemeted yet.
 * @author apc@apc.kg
 */
public class DatagramChannelWithTimeouts extends DatagramChannel {

    protected  DatagramChannel channel;
    protected  Selector selector;
    private long connectTimeout = 5000;
    private long readTimeout = 10000;
    protected SelectionKey channelKey;
    private static final Logger log = LoggingManager.getLoggerForClass();
    private boolean fastFirstPacketRead;

    protected DatagramChannelWithTimeouts() throws IOException {
        super(null);
        log.debug("Creating DatagramChannel");
        selector = Selector.open();
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channelKey = channel.register(selector, SelectionKey.OP_CONNECT);
    }

    public static DatagramChannel open() throws IOException {
        return new DatagramChannelWithTimeouts();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int bytesRead = 0;
        while (selector.select(readTimeout) > 0) {
            selector.selectedKeys().remove(channelKey);
            int cnt = channel.read(dst);
            log.debug("Read " + cnt);
            if (cnt < 1) {
                if (bytesRead < 1) {
                    bytesRead = -1;
                }
                return bytesRead;
            } else {
                bytesRead += cnt;
                if (!fastFirstPacketRead) {
                    fastFirstPacketRead = true;
                    return bytesRead;
                }
            }
        }

        throw new SocketTimeoutException("Timeout exceeded while reading from socket");
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        fastFirstPacketRead = false;
        int res = 0;
        int size = src.remaining();
        while (res<size) {
            res += channel.write(src);
        }
        return res;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        channel.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        throw new UnsupportedOperationException("This class is blocking implementation of SocketChannel");
    }

    @Override
    public boolean isConnected() {
        return channel.isConnected();
    }

    public void setConnectTimeout(int t) {
        connectTimeout = t;
    }

    public void setReadTimeout(int t) {
        readTimeout = t;
    }

    @Override
    public DatagramSocket socket() {
        return channel.socket();
    }

    @Override
    public DatagramChannel connect(SocketAddress remote) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DatagramChannel disconnect() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SocketAddress receive(ByteBuffer dst) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
