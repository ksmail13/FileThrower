package dropbox.server.Util;

import java.nio.channels.SocketChannel;

/**
 * Created by micky on 2014. 11. 23..
 */
public class SocketChannelWrapper implements Comparable<SocketChannelWrapper>{
    private SocketChannel sc;

    public SocketChannelWrapper(SocketChannel sc){
        this.setSocketChannel(sc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocketChannelWrapper)) return false;

        SocketChannelWrapper that = (SocketChannelWrapper) o;

        if (getSocketChannel() != null ? !getSocketChannel().equals(that.getSocketChannel()) : that.getSocketChannel() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getSocketChannel() != null ? getSocketChannel().hashCode() : 0;
    }

    @Override
    public int compareTo(SocketChannelWrapper o) {
        return getSocketChannel().hashCode() - o.getSocketChannel().hashCode();
    }

    public SocketChannel getSocketChannel() {
        return sc;
    }

    public void setSocketChannel(SocketChannel sc) {
        if(sc == null) throw new NullPointerException("SocketChannel is not null");
        this.sc = sc;
    }
}
