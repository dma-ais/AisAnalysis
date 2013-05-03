package dk.dma.ais.analysis.coverage.configuration;

public class DatabaseConfiguration{
    	private String type = "MemoryOnly"; 
    	private String dbName = "nordicCoverage";
    	private String addr = "localhost";
    	private int port = 5000;

		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getDbName() {
			return dbName;
		}
		public void setDbName(String dbName) {
			this.dbName = dbName;
		}
		public String getAddr() {
			return addr;
		}
		public void setAddr(String addr) {
			this.addr = addr;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
    }