package org.gofleet.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.StatefulJob;

public abstract class Job implements StatefulJob {
	private final static Log LOG = LogFactory.getLog(Job.class);

	public abstract String getName();

	public abstract String getDescription();

	public abstract Integer getFrequency();

	public abstract void run();

	public List<String> getDependencies() {
		return new ArrayList<String>(0);
	}

	public List<JobListener> getListenerList() {
		return new ArrayList<JobListener>(0);
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			run();
		} catch (Throwable t) {
			LOG.error("Error executing job.", t);
		}
	}

	public Map<String, Object> getParameters() {
		return new HashMap<String, Object>();
	}
}
