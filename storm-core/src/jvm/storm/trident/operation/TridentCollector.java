package storm.trident.operation;

import java.util.Map;
import java.util.List;

import storm.trident.tuple.TridentTuple;

public interface TridentCollector {
    void emit(List<Object> values);
    void emitWithMetadata(List<Object> values, Map<TridentTuple.AnnotationKeys, Object> metadata);
    void reportError(Throwable t);
}
