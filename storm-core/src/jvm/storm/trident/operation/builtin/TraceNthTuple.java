package storm.trident.operation.builtin;

import java.util.Map;

import backtype.storm.tuple.Fields;
import storm.trident.Stream;
import storm.trident.tuple.TridentTuple;
import storm.trident.tuple.MetadataMap;
import storm.trident.operation.Assembly;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.operation.TridentOperationContext;
    
public class TraceNthTuple implements Assembly {

    private Long _N;
    
    public TraceNthTuple(Long N) {
        _N = N;
    }

    /**
       Don't return more fields than what we started with
     */
    @Override
    public Stream apply(Stream input) {
        Fields outputFields = input.getOutputFields();
        return input.each(outputFields, new TraceNthTupleFunction(_N), new Fields("false"))
            .project(outputFields);
    }

    public static class TraceNthTupleFunction extends BaseFunction {

        private Long _N;
        private Long _numTuples;
        
        public TraceNthTupleFunction(Long N) {
            _N = N;
        }
        
        @Override
        public void prepare(Map conf, TridentOperationContext context) {
            _numTuples = 0l;
        }
    
        @Override
        public void execute(TridentTuple tuple, TridentCollector collector) {
            if (_numTuples % _N == 0) {
                MetadataMap metadata = tuple.getMetadataMap();
                metadata.put("is_traceable", true);
                
                collector.emit(null,metadata);                
            } else {
                collector.emit(null);
            }
            _numTuples += 1l;
        }
    }
}
