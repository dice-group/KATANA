package org.aksw.simba.katana.model;

import java.util.ArrayList;

public class RDFResource {

	String uri;
	ArrayList<String> labels;

	public RDFResource(String uri, String label) {

		this.uri = uri;
		this.labels = new ArrayList<String>();
		labels.add(label);

	}

	public RDFResource(String uri) {

		this.uri = uri;
		this.labels = null;

	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public ArrayList<String> getLabels() {
		return labels;
	}

	public void setLabels(ArrayList<String> labels) {
		this.labels = labels;
	}

}
