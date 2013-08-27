package storm.trident.planner.processor;

import java.util.Map;
import java.util.List;
import storm.trident.operation.TridentCollector;
import storm.trident.planner.ProcessorContext;
import storm.trident.planner.TupleReceiver;
import storm.trident.tuple.TridentTuple;
import storm.trident.tuple.TridentTuple.Factory;
import storm.trident.tuple.TridentTupleView;
import storm.trident.tuple.TridentTupleView.OperationOutputFactory;


public class AppendCollector implements TridentCollector {
    OperationOutputFactory _factory;
    TridentContext _triContext;
    TridentTuple tuple;
    ProcessorContext context;
    
    public AppendCollector(TridentContext context) {
        _triContext = context;
        _factory = new OperationOutputFactory(context.getParentTupleFactories().get(0), context.getSelfOutputFields());
    }
                
    public void setContext(ProcessorContext pc, TridentTuple t) {
        this.context = pc;
        this.tuple = t;
    }

    @Override
    public void emit(List<Object> values) {
        emitWithMetadata(values, null);
    }

    @Override
    public void emitWithMetadata(List<Object> values, Map<TridentTuple.AnnotationKeys,Object> metadata) {
        TridentTuple toEmit = _factory.create((TridentTupleView) tuple, values);
        if (metadata != null) {
            toEmit.makeTraceable();
            for (Map.Entry<TridentTuple.AnnotationKeys, Object> meta : metadata.entrySet()) {
                toEmit.annotate(meta.getKey(), meta.getValue());
            }
        }
        for(TupleReceiver r: _triContext.getReceivers()) {
            r.execute(context, _triContext.getOutStreamId(), toEmit);
        }
    }

    @Override
    public void reportError(Throwable t) {
        _triContext.getDelegateCollector().reportError(t);
    } 
    
    public Factory getOutputFactory() {
        return _factory;
    }
}
