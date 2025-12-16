package cz.cuni.mff.hanaf.mainapp.providers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.*;
import java.util.stream.Collectors;

public class VectorStoreExclusionPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String provider = environment.getProperty("app.vectorstore.provider");

        List<VectorStoreExclusionStrategy> strategies = loadStrategies();

        String selectedAutoConfig = strategies.stream()
                .filter(strategy -> strategy.supports(provider))
                .findFirst().map(VectorStoreExclusionStrategy::getAutoConfigurationClass).orElse(null);

        if (selectedAutoConfig == null) {
            System.err.println("WARNING: No strategy found for provider: " + provider);
            return;
        }

        List<String> exclusions = strategies.stream()
                .map(VectorStoreExclusionStrategy::getAutoConfigurationClass)
                .filter(config -> !config.equals(selectedAutoConfig))
                .collect(Collectors.toList());

        if (!exclusions.isEmpty()) {
            Map<String, Object> props = new HashMap<>();
            props.put("spring.autoconfigure.exclude", String.join(",", exclusions));
            environment.getPropertySources().addFirst(new MapPropertySource("vectorStoreExclusion", props));

            System.out.println("Selected vector store: " + provider);
            System.out.println("Excluded auto-configurations: " + exclusions);
        }
    }

    private List<VectorStoreExclusionStrategy> loadStrategies() {
        List<VectorStoreExclusionStrategy> strategies = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(VectorStoreExclusionStrategy.class));

        scanner.findCandidateComponents("cz.cuni.mff.hanaf.mainapp.providers")
                .forEach(beanDefinition -> {
                    try {
                        Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                        if (!clazz.isInterface() && VectorStoreExclusionStrategy.class.isAssignableFrom(clazz)) {
                            strategies.add((VectorStoreExclusionStrategy) clazz.getDeclaredConstructor().newInstance());
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load strategy: " + beanDefinition.getBeanClassName());
                    }
                });

        return strategies;
    }
}