package storm.trident.tuple;

public class TraceEntry {
    private Integer _index;       // order in trace
    private String _functionName; // associated function

    public TraceEntry(Integer index, String functionName) {
        _index = index;
        _functionName = functionName;
    }

    public Integer getIndex() {
        return _index;
    }

    public String getFunctionName() {
        return _functionName;
    }
}
