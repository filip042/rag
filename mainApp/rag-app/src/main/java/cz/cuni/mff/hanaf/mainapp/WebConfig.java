package cz.cuni.mff.hanaf.mainapp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for static resource handling based on the active UI theme, bound from the {@code app.ui.theme} prefix.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.ui.theme:default}")
    private String theme;

    /**
     * Configures resource handlers to serve static files from the active theme directory,
     * falling back to the default directory if a file is not found.
     *
     * @param registry the resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/" + theme + "/")
                .addResourceLocations("classpath:/static/default/");
    }
}
