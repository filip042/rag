package cz.cuni.mff.hanaf.mainapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the application, bound from the {@code app} prefix.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String baseUrl;
    private ApiUrls apiUrls = new ApiUrls();
    private FrontendUrls frontendUrls = new FrontendUrls();

    /**
     * Returns the base URL of the application.
     *
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Sets the base URL of the application.
     *
     * @param baseUrl the base URL to set
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the API endpoint URL configuration.
     *
     * @return the API URLs
     */
    public ApiUrls getApiUrls() {
        return apiUrls;
    }

    /**
     * Sets the API endpoint URL configuration.
     *
     * @param apiUrls the API URLs to set
     */
    public void setApiUrls(ApiUrls apiUrls) {
        this.apiUrls = apiUrls;
    }

    /**
     * Returns the frontend page URL configuration.
     *
     * @return the frontend URLs
     */
    public FrontendUrls getFrontendUrls() {
        return frontendUrls;
    }

    /**
     * Sets the frontend page URL configuration.
     *
     * @param frontendUrls the frontend URLs to set
     */
    public void setFrontendUrls(FrontendUrls frontendUrls) {
        this.frontendUrls = frontendUrls;
    }

    /**
     * Configuration properties for the backend API endpoint URLs.
     */
    public static class ApiUrls {
        private String base;
        private String ask;
        private String answer;
        private String add;
        private String delete;
        private String status;

        /**
         * Returns the base API URL.
         *
         * @return the base URL
         */
        public String getBase() {
            return base;
        }

        /**
         * Sets the base API URL.
         *
         * @param base the base URL to set
         */
        public void setBase(String base) {
            this.base = base;
        }

        /**
         * Returns the URL of the ask endpoint.
         *
         * @return the ask URL
         */
        public String getAsk() {
            return ask;
        }

        /**
         * Sets the URL of the ask endpoint.
         *
         * @param ask the ask URL to set
         */
        public void setAsk(String ask) {
            this.ask = ask;
        }

        /**
         * Returns the URL of the answer endpoint.
         *
         * @return the answer URL
         */
        public String getAnswer() {
            return answer;
        }

        /**
         * Sets the URL of the answer endpoint.
         *
         * @param answer the answer URL to set
         */
        public void setAnswer(String answer) {
            this.answer = answer;
        }

        /**
         * Returns the URL of the file add endpoint.
         *
         * @return the add URL
         */
        public String getAdd() {
            return add;
        }

        /**
         * Sets the URL of the file add endpoint.
         *
         * @param add the add URL to set
         */
        public void setAdd(String add) {
            this.add = add;
        }

        /**
         * Returns the URL of the project delete endpoint.
         *
         * @return the delete URL
         */
        public String getDelete() {
            return delete;
        }

        /**
         * Sets the URL of the project delete endpoint.
         *
         * @param delete the delete URL to set
         */
        public void setDelete(String delete) {
            this.delete = delete;
        }

        /**
         * Returns the URL of the indexing status endpoint.
         *
         * @return the status URL
         */
        public String getStatus() {
            return status;
        }

        /**
         * Sets the URL of the indexing status endpoint.
         *
         * @param status the status URL to set
         */
        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * Configuration properties for the frontend page URLs.
     */
    public static class FrontendUrls {
        private String base;
        private String answer;
        private String chat;
        private String dashboard;
        private String deleteProject;
        private String load;
        private String login;
        private String logout;
        private String newProject;
        private String newUser;

        /**
         * Returns the base frontend URL.
         *
         * @return the base URL
         */
        public String getBase() {
            return base;
        }

        /**
         * Sets the base frontend URL.
         *
         * @param base the base URL to set
         */
        public void setBase(String base) {
            this.base = base;
        }

        /**
         * Returns the URL of the answer page.
         *
         * @return the answer page URL
         */
        public String getAnswer() {
            return answer;
        }

        /**
         * Sets the URL of the answer page.
         *
         * @param answer the answer page URL to set
         */
        public void setAnswer(String answer) {
            this.answer = answer;
        }

        /**
         * Returns the URL of the chat page.
         *
         * @return the chat page URL
         */
        public String getChat() {
            return chat;
        }

        /**
         * Sets the URL of the chat page.
         *
         * @param chat the chat page URL to set
         */
        public void setChat(String chat) {
            this.chat = chat;
        }

        /**
         * Returns the URL of the user dashboard page.
         *
         * @return the dashboard page URL
         */
        public String getDashboard() {
            return dashboard;
        }

        /**
         * Sets the URL of the user dashboard page.
         *
         * @param dashboard the dashboard page URL to set
         */
        public void setDashboard(String dashboard) {
            this.dashboard = dashboard;
        }

        /**
         * Returns the URL of the delete project page.
         *
         * @return the delete project page URL
         */
        public String getDeleteProject() {
            return deleteProject;
        }

        /**
         * Sets the URL of the delete project page.
         *
         * @param deleteProject the delete project page URL to set
         */
        public void setDeleteProject(String deleteProject) {
            this.deleteProject = deleteProject;
        }

        /**
         * Returns the URL of the file upload API endpoint.
         *
         * @return the load endpoint URL
         */
        public String getLoad() {
            return load;
        }

        /**
         * Sets the URL of the file upload API endpoint.
         *
         * @param load the load endpoint URL to set
         */
        public void setLoad(String load) {
            this.load = load;
        }

        /**
         * Returns the URL of the login page.
         *
         * @return the login page URL
         */
        public String getLogin() {
            return login;
        }

        /**
         * Sets the URL of the login page.
         *
         * @param login the login page URL to set
         */
        public void setLogin(String login) {
            this.login = login;
        }

        /**
         * Returns the URL of the logout endpoint.
         *
         * @return the logout URL
         */
        public String getLogout() {
            return logout;
        }

        /**
         * Sets the URL of the logout endpoint.
         *
         * @param logout the logout URL to set
         */
        public void setLogout(String logout) {
            this.logout = logout;
        }

        /**
         * Returns the URL of the new project page.
         *
         * @return the new project page URL
         */
        public String getNewProject() {
            return newProject;
        }

        /**
         * Sets the URL of the new project page.
         *
         * @param newProject the new project page URL to set
         */
        public void setNewProject(String newProject) {
            this.newProject = newProject;
        }

        /**
         * Returns the URL of the new user page.
         *
         * @return the new user page URL
         */
        public String getNewUser() {
            return newUser;
        }

        /**
         * Sets the URL of the new user page.
         *
         * @param newUser the new user page URL to set
         */
        public void setNewUser(String newUser) {
            this.newUser = newUser;
        }
    }
}
