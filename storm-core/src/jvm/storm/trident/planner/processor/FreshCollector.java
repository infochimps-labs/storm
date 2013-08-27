package storm.trident.planner.processor;

import java.util.List;
import java.util.Map;
import storm.trident.operation.TridentCollector;
import storm.trident.planner.ProcessorContext;
import storm.trident.planner.TupleReceiver;
import storm.trident.tuple.TridentTuple;
import storm.trident.tuple.MetadataMap;
import storm.trident.tuple.TridentTuple.Factory;
import storm.trident.tuple.TridentTupleView.FreshOutputFactory;


public class FreshCollector implements TridentCollector {
    FreshOutputFactory _factory;
    TridentContext _triContext;
    ProcessorContext context;
    
    public FreshCollector(TridentContext context) {
        _triContext = context;
        _factory = new FreshOutputFactory(context.getSelfOutputFields());
    }
                
    public void setContext(ProcessorContext pc) {
        this.context = pc;
    }

    @Override
    public void emit(List<Object> values) {
        TridentTuple toEmit = _factory.create(values);
        for(TupleReceiver r: _triContext.getReceivers()) {
            r.execute(context, _triContext.getOutStreamId(), toEmit);
        }            
    }

    @Override
    public void emit(List<Object> values, MetadataMap metadata) {
        TridentTuple toEmit = _factory.create(values);
        if (metadata != null) {
            toEmit.getMetadataMap().putAll(metadata);
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
