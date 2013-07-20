package storm.trident.spout;

import backtype.storm.Config;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.IBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.utils.Utils;
import storm.trident.topology.TransactionAttempt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storm.trident.topology.MasterBatchCoordinator;
import storm.trident.topology.state.RotatingTransactionalState;
import storm.trident.topology.state.TransactionalState;

public class TridentSpoutCoordinator implements IBasicBolt {
    public static final Logger LOG = LoggerFactory.getLogger(TridentSpoutCoordinator.class);
    private static final String META_DIR = "meta";

    ITridentSpout _spout;
    ITridentSpout.BatchCoordinator _coord;
    RotatingTransactionalState _state;
    TransactionalState _underlyingState;
    String _id;
    String _compId;
    
    public TridentSpoutCoordinator(String id, ITridentSpout spout) {
        _spout = spout;
        _id = id;
    }
    
    @Override
    public void prepare(Map conf, TopologyContext context) {
        _compId = context.getThisComponentId() + ":" + context.getThisTaskId();

        _coord = _spout.getCoordinator(_id, conf, context);
        _underlyingState = TransactionalState.newCoordinatorState(conf, _id);
        _state = new RotatingTransactionalState(_underlyingState, META_DIR);
    }

    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector) {
        TransactionAttempt attempt = (TransactionAttempt) tuple.getValue(0);

        // if (LOG.isTraceEnabled())
        //     LOG.trace(Utils.logString("TridentSpoutCoordinator.execute:beg", _compId, "id", _id, "tuple", tuple.toString()) );

        if(tuple.getSourceStreamId().equals(MasterBatchCoordinator.SUCCESS_STREAM_ID)) {
            _state.cleanupBefore(attempt.getTransactionId());
            _coord.success(attempt.getTransactionId());
        } else {
            long txid = attempt.getTransactionId();
            Object prevMeta = _state.getPreviousState(txid);
            Object meta = _coord.initializeTransaction(txid, prevMeta, _state.getState(txid));
            _state.overrideState(txid, meta);

            collector.emit(MasterBatchCoordinator.BATCH_STREAM_ID, new Values(attempt, meta));

           // if(LOG.isTraceEnabled() && Utils.isTraceSampled(1) )
           //     LOG.trace(Utils.logString("TridentSpoutCoordinator.execute:emits", _compId, ""+txid+":"+attempt,
           //             "id", _id, "tuple", tuple.toString(), "meta", meta.toString()));
        }
                
    }

    @Override
    public String toString() {
        return _compId;
    }

    @Override
    public void cleanup() {
        _coord.close();
        _underlyingState.close();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream(MasterBatchCoordinator.BATCH_STREAM_ID, new Fields("tx", "metadata"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config ret = new Config();
        ret.setMaxTaskParallelism(1);
        return ret;
    }   
}
