package data;

public class IndexNote {
	private String column;
    private boolean clustered;
    private int order;
    
    public IndexNote(String clmn, boolean clustered, int d) {
    	this.column = clmn;
    	this.clustered = clustered;
    	this.order = d;
    }
    
    public String getColumn() {
    	return column;
    }
    
    public boolean isClustered() {
    	return clustered;
    }
    
    public int getOrder() {
    	return order;
    }
}
