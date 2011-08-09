package org.gofleet.module.dummy;

/**
 * Test job. 
 * 
 * Just prints every five seconds a "Test Job" message in console.
 *  
 * @author marias
 *
 */
public class DummyJob extends org.gofleet.scheduler.Job {

	public String getName() {
		return "Dummy Job";
	}

	public String getDescription() {
		return "Dummy Job Description";
	}

	public Integer getFrequency() {
		return new Integer(5 * 1000);
	}

	public void run(){
		System.out.println("Test Job");
	}
}
