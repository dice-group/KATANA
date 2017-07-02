package org.aksw.simba.katana.model;

import java.util.ArrayList;

public class RDFResource {

	String uri;
	String kbLabel;
	ArrayList<String> labels;

	public String getKbLabel() {
		return kbLabel;
	}

	public void setKbLabel(String kbLabel) {
		this.kbLabel = kbLabel;
	}

	public RDFResource(String uri, String label) {

		this.uri = uri;
		this.kbLabel = label;
		labels = new ArrayList<String>();

	}

	public RDFResource(String uri) {

		this.uri = uri;

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
