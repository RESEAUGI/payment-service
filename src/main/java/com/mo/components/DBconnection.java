package com.mo.components;

import java.net.InetSocketAddress;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;

import jakarta.annotation.PostConstruct;


@Component
public class DBconnection {
	
	private CqlSession session;
	
	
	 @PostConstruct
	    public void initialize() {
	        connect("127.0.0.1", 9042, "datacenter1");
	    }

  public void connect(String node, Integer port, String dataCenter) {
      CqlSessionBuilder builder = CqlSession.builder();
      builder.addContactPoint(new InetSocketAddress("127.0.0.1", 9042));
      builder.withLocalDatacenter(dataCenter);

      session = builder.build();
  }

  public CqlSession getSession() {
      return this.session;
  }

  public void close() {
      session.close();
  }
  

}
