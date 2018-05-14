package org.aksw.katana.evaluation;

import org.aksw.katana.service.InMemoryTripleStore;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
@Configuration
public class BeanConfiguration {
    @Bean
    @Primary
    public InMemoryTripleStore getInMemoryTripleStore() {
        return Mockito.mock(InMemoryTripleStore.class);
    }
}
