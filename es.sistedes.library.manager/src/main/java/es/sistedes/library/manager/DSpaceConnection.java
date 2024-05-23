package es.sistedes.library.manager;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;

import es.sistedes.library.manager.dspace.model.DSRoot;

public class DSpaceConnection {
	
	private static final int TIMEOUT_MINUTES = 25;
	private DSRoot dsRoot;
	private Date lastIssued;
	private Thread refreshThread;
	private volatile boolean exit = false;
	
	public DSpaceConnection(URI uri, String email, String password) {
		dsRoot = DSRoot.create(uri);
		dsRoot.getAuthnEndpoint().doLogin(email, password);
		lastIssued = Calendar.getInstance().getTime();
		refreshThread = new Thread() {
			@Override
			public void run() {
				while (!exit) {
					if (Calendar.getInstance().getTime().toInstant().getEpochSecond() - lastIssued.toInstant().getEpochSecond() > TIMEOUT_MINUTES * 60) {
						dsRoot.getAuthnEndpoint().refreshAuth();
						lastIssued = Calendar.getInstance().getTime();
					}
					try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		refreshThread.setDaemon(true);
		refreshThread.start();
	}
	
	public DSRoot getDsRoot() {
		return dsRoot;
	}
	
	public void close() {
		dsRoot.getAuthnEndpoint().doLogout();
		exit = true;
	}
}