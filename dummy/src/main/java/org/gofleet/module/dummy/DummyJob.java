package org.gofleet.module.dummy;

public class DummyJob extends org.gofleet.scheduler.Job {

	public String getName() {
		return "Dummy Job";
	}

	public String getDescription() {
		return "Dummy Job Description";
	}

	public Integer getFrequency() {
		return new Integer(30 * 1000);
	}

	public void run(){
		System.out.println("Test Job");
	}
}
