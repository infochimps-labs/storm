package storm.trident.operation;

import java.util.List;

import java.io.Serializable;
import storm.trident.tuple.TridentTuple;

//
// Same as ReducerAggregator but all the tuples for a batch are
// recieved at once
//
public interface ImproverAggregator<T> extends Serializable {
    T init();
    T improve(T curr, List<TridentTuple> tuples);
}
