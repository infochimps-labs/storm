package storm.trident.operation.impl;

import backtype.storm.tuple.Values;
import java.util.List;
import java.util.Map;
import storm.trident.operation.ImproverAggregator;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
import storm.trident.state.ImproverValueUpdater;
import storm.trident.state.StateUpdater;
import storm.trident.state.snapshot.Snapshottable;
import storm.trident.tuple.TridentTuple;

public class ImproverAggStateUpdater implements StateUpdater<Snapshottable> {
    ImproverAggregator _improver;
    
    public ImproverAggStateUpdater(ImproverAggregator improver) {
        _improver = improver;
    }
    

    @Override
    public void updateState(Snapshottable state, List<TridentTuple> tuples, TridentCollector collector) {
        Object newVal = state.update(new ImproverValueUpdater(_improver, tuples));
        collector.emit(new Values(newVal));
    }

    @Override
    public void prepare(Map conf, TridentOperationContext context) {        
    }

    @Override
    public void cleanup() {
    }
    
}
