package org.aksw.simba.katana.NLUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.simba.katana.KBUtils.SparqlHandler;
import org.aksw.simba.katana.model.RDFProperty;
import org.aksw.simba.katana.model.RDFResource;

import edu.stanford.nlp.ie.util.RelationTriple;

public class ComparisonUtils {
	NLUtils nlHandler;
	public Map<RDFProperty, ArrayList<RDFResource>> kbPropResourceMap;

	public ComparisonUtils() {
		this.nlHandler = new NLUtils();
		//this.kbPropResourceMap = queryHandler.getPropertyResourceMap();
	}

	public Map<RDFResource, List<String>> addLabels(List<RelationTriple> triplesFromNL, Map<RDFProperty, ArrayList<RDFResource>> map) {
		Map<RDFResource, List<String>> labeledResources = new HashMap<>();
		for (Map.Entry<RDFProperty, ArrayList<RDFResource>> entry : map.entrySet()) {
			for (RelationTriple triple : triplesFromNL) {
				if (entry.getKey().getLabel().contains(triple.relationLemmaGloss())) {
					List<RDFResource> res = entry.getValue();
					System.out.println("Property match Found !! \n" + entry.getKey().getLabel());
					String object = triple.objectLemmaGloss();
					String subject = triple.subjectLemmaGloss();
					for (RDFResource resource : res) {
						if (resource.getKbLabel().toLowerCase().contains(subject.toLowerCase())) {
							System.out.println("Got Resource match with a subject!");
							System.out
									.println("Resource : " + resource.getKbLabel() + "  Potential Label : " + subject);
							addLabelToMap(labeledResources, resource, subject);
						}
						if (resource.getKbLabel().toLowerCase().contains(object.toLowerCase())) {
							System.out.println("Got Resource match with a object");
							System.out
									.println("Resource : " + resource.getKbLabel() + "  Potential Label : " + object);
							addLabelToMap(labeledResources, resource, object);
						}
					}
				}
			}
		}

		return labeledResources;
	}

	private <T1, T2> void addLabelToMap(Map<T1, List<T2>> map, T1 key, T2 value) {
		List<T2> mapValue = map.get(key);
		if (mapValue == null) {
			mapValue = new ArrayList<>(1);
			mapValue.add(value);
			map.put(key, mapValue);
		} else {
			mapValue.add(value);
		}
	}

	public void pseudoAddLabels(List<RelationTriple> triplesFromNL, Map<RDFProperty, ArrayList<RDFResource>> map) {

		for (Map.Entry<RDFProperty, ArrayList<RDFResource>> entry : map.entrySet()) {
			for (RelationTriple triple : triplesFromNL) {
				if (entry.getKey().getLabel().contains(triple.relationLemmaGloss())) {
					System.out.println("Property match Found !! \n" + entry.getKey().getLabel());
					List<RDFResource> res = entry.getValue();
					System.out.println(
							"Potential Labels are \n " + triple.subjectGloss() + " :: " + triple.objectLemmaGloss());
					System.out.println("Associated resources are :");
					for (RDFResource resource : res) {
						System.out.println(resource.getKbLabel());
					}
				}
			}
		}
	}

	public void searchElement(String[] words, List<RDFResource> res) {
		for (String nlEle : words) {
			for (RDFResource resource : res) {
				if (resource.getLemmas().contains(nlEle)) {
					System.out.println(resource.getKbLabel() + ":" + nlEle);
				}
			}
		}
	}


}