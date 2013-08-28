package storm.trident.tuple;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;

public class MetadataMap implements Map<String,Object> {

    public static String IS_TRACEABLE = "is_traceable";
    public static String TRACE_HISTORY = "trace_history";
    
    private Map<String, Object> _metadata;
    
    public MetadataMap() {
        _metadata = new HashMap<String, Object>();
    }

    private void initializeTraceIfNotInitialized() {
        if (!containsKey(TRACE_HISTORY)) {
            _metadata.put(TRACE_HISTORY, new ArrayList<TraceEntry>());
        }
    }
    
    public boolean isTraceable() {
        Object value = get(IS_TRACEABLE);
        return (value != null && (Boolean)value == true); 
    }

    public void addTraceEntry(TraceEntry entry) {
        initializeTraceIfNotInitialized();
        getTrace().add(entry);        
    }

    public TraceEntry getTraceEntry(Integer index) {
        initializeTraceIfNotInitialized();
        return getTrace().get(index);
    }

    public List<TraceEntry> getTrace() {
        initializeTraceIfNotInitialized();
        return (List<TraceEntry>)_metadata.get(TRACE_HISTORY);
    }
    
    @Override
    public void clear() {
        _metadata.clear();
    }

    public boolean containsKey(Object key) {        
        return (get(key) != null);
    }

    public boolean containsValue(Object value) {
        return _metadata.containsValue(value);
    }
    
    public Set<Map.Entry<String,Object>> entrySet() {
        return _metadata.entrySet();
    }
 
    public Object get(Object key) {
        return _metadata.get(key);
    }

    public boolean isEmpty() {
        return _metadata.isEmpty();
    }
    
    public Set<String> keySet() {
        return _metadata.keySet();
    }
    
    public Object put(String key, Object value) {
        return _metadata.put(key, value);
    }

    public void	putAll(Map<? extends String,? extends Object> m) {
        _metadata.putAll(m);
    }

    public Object remove(Object key) {
        return _metadata.remove(key);
    }

    public int size() {
        return _metadata.size();
    }

    public Collection<Object> values() {
        return _metadata.values();
    }

    // TODO: This isn't the best, but it's ok for debugging
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("{");
        for (Map.Entry<String, Object> pair: entrySet()) {
            b.append(" [");
            b.append(pair.getKey());
            b.append(" => ");
            b.append(pair.getValue());
            b.append("] ");
        }
        b.append("}");
        return b.toString();
    }
}
