package cz.cuni.mff.hanaf.mainapp.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Configuration properties for the Thymeleaf template, bound from the {@code app.ui.theme} prefix.
 */
@Configuration
public class ThymeleafConfig {

    @Value("${app.ui.theme:default}")
    private String theme;

    /**
     * Returns a {@link SpringResourceTemplateResolver} bean for the active UI theme.
     *
     * @return the custom template resolver
     */
    @Bean
    public SpringResourceTemplateResolver customTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/" + theme + "/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setOrder(1);
        resolver.setCheckExistence(true);
        return resolver;
    }

    /**
     * Returns a fallback {@link SpringResourceTemplateResolver} bean for the default theme.
     *
     * @return the default template resolver
     */
    @Bean
    public SpringResourceTemplateResolver defaultTemplateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/default/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setOrder(2);
        return resolver;
    }
}
