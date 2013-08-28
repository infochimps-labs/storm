package storm.trident.tuple;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
   Annotation of a lifecycle event for a tuple
 */
public class TraceEntry {
    private Integer _index; // order in trace history
    private String _name;   // the name of this entry    
    private Map<String,String> _content;
    
    public TraceEntry(Integer index, String name) {
        _index = index;
        _name = name;
        _content = new HashMap<String,String>();
    }

    public Integer getIndex() {
        return _index;
    }

    public String getName() {
        return _name;
    }

    public Map<String,String> getContent() {
        return _content;
    }

    public void add(String key, String value) {
        _content.put(key,value);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("TraceEntry( ");
        sb.append("name:");
        sb.append(getName());
        sb.append(", ");
        sb.append("index:");
        sb.append(getIndex());
        sb.append(", ");
        sb.append("content:{");
        Iterator<Map.Entry<String,String>> itr = _content.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String,String> pair = itr.next();
            sb.append(pair.getKey());
            sb.append(" => ");
            sb.append(pair.getValue());
            if (itr.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        sb.append(" )");
        return sb.toString();
    }
}
