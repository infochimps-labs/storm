package storm.trident.state;

import java.util.List;
import storm.trident.operation.ImproverAggregator;
import storm.trident.tuple.TridentTuple;

public class ImproverValueUpdater implements ValueUpdater<Object> {
    List<TridentTuple> tuples;
    ImproverAggregator improver;
    
    public ImproverValueUpdater(ImproverAggregator improver, List<TridentTuple> tuples) {
        this.improver = improver;
        this.tuples = tuples;
    }

    @Override
    public Object update(Object stored) {
        Object ret = (stored == null) ? this.improver.init() : stored;
        ret = this.improver.improve(ret, tuples);
        return ret;
    }        
}
