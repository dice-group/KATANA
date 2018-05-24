package org.aksw.katana.evaluation;

import org.aksw.katana.evaluation.benchmark.BenchMarkEvaluation;
import org.aksw.katana.evaluation.benchmark.KnowledgeBaseGenerator;
import org.aksw.katana.service.InMemoryTripleStore;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

//@ActiveProfiles({"random", "test"})
////@ActiveProfiles({"static", "test"})
//@RunWith(SpringRunner.class)
//@SpringBootTest
//@TestPropertySource(locations = "classpath:test.properties")
//public class BenchMarkEvaluationTest {
//
//    @Autowired
//    private BenchMarkEvaluation benchMarkEvaluation;
//
//    @Autowired
//    private InMemoryTripleStore inMemoryTripleStore;
//
//    @Autowired
//    private KnowledgeBaseGenerator knowledgeBaseGenerator;
//
//    @Test
//    public void run() {
//        Model model = knowledgeBaseGenerator.generate();
//        Mockito.when(inMemoryTripleStore.getModel()).thenReturn(model);
//        benchMarkEvaluation.run();
//    }
//}
//

