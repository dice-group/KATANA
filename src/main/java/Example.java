import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.aksw.simba.katana.NLUtils.ComparisonUtils;
import org.aksw.simba.katana.NLUtils.NLUtils;

import com.google.common.io.Files;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;

public class Example {
	public static void main(String[] args) {

		File inputFile = new File("src/main/resources/abc.txt");
		String text = null;
		try {
			text = Files.toString(inputFile, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		ComparisonUtils cu = new ComparisonUtils();
		// cu.addLabels(text, cu.kbPropResourceMap);
		NLUtils nlp = new NLUtils();
		Annotation doc = nlp.getAnnotatedText(text);
		List<RelationTriple> triplesFromNL = nlp.getTriplesfromNL(doc.get(SentencesAnnotation.class));
		cu.addLabels(triplesFromNL, cu.kbPropResourceMap);
		// cu.psuedoaddLabels(triplesFromNL, cu.kbPropResourceMap);

	}
}
