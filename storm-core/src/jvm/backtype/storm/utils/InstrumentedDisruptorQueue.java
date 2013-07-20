package backtype.storm.utils;

import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.WaitStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instrumented Disruptor Queue.
 */
public class InstrumentedDisruptorQueue extends DisruptorQueue{

    String _id;
    String _topology;
    int    _consumeLoggingFreq;
    int    _publishLoggingFreq;
    long sample_rate    = 1L;
    static final Logger LOG = LoggerFactory.getLogger(InstrumentedDisruptorQueue.class);
    double _logFreq = 200;

    public InstrumentedDisruptorQueue(String role, String ownerId, String componentId, String topology, ClaimStrategy claim, WaitStrategy wait) {
        super(role, ownerId, componentId, claim, wait);
        _topology = topology;
        _id   = role + ":" + ownerId + ":" + componentId;
        _consumeLoggingFreq = (_role.equals("trns") ? 10000 :  1000);
        _publishLoggingFreq = (_role.equals("recv") ?  1000 :  5000);
        if (LOG.isTraceEnabled())
            LOG.trace(Utils.logString("DisruptorQueue.constructor", _id, "", "size" , ""+capacity()));
    }
    public String summary() {
        return String.format("%5d %4d%% /%5d w %9d r %9d", population(), (long)(100*pctFull()), capacity(), writePos(), readPos());
    }

    @Override
    protected void consumeBatchToCursor(long cursor, EventHandler<Object> handler) {
        if (LOG.isTraceEnabled()) logSummary("consume", _consumeLoggingFreq, "=>"+cursor);
        super.consumeBatchToCursor(cursor, handler);
    }

    @Override
    public void publish(Object obj, boolean block) throws InsufficientCapacityException {
        if (LOG.isTraceEnabled()) logSummary("publish", _publishLoggingFreq, "");
        super.publish(obj, block);
    }

    protected void logSummary(String action, int freq, String extraInfo) {
        if (population() == 0) return;
        if ((pctFull() >= 0.9) || Utils.isTraceSampled(freq)) {
            String danger = ((pctFull() > 0.9) ? "qfull!!!!!!" : "qok");
            LOG.trace(Utils.logString("DisruptorQueue."+action, _id, "", "", danger, "q", summary()+extraInfo));
        }
    }

}
