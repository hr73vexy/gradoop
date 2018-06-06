package org.gradoop.flink.io.impl.rdbms.tuples;

import org.apache.flink.api.java.tuple.Tuple3;

public class RDBMSConfig extends Tuple3<String,String,String>{
	String url;
	String user;
	String pw;
	
	public RDBMSConfig(String url, String user, String pw){
		this.url = url;
		this.user = user;
		this.pw = pw;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}
}
