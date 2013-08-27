package storm.trident.operation;

import java.util.Map;
import java.util.List;

import storm.trident.tuple.MetadataMap;

public interface TridentCollector {
    void emit(List<Object> values);
    void emit(List<Object> values, MetadataMap metadata);
    void reportError(Throwable t);
}
