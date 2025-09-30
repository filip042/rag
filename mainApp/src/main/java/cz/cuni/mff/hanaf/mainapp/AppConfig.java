package cz.cuni.mff.hanaf.mainapp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private String baseUrl;
    private ApiUrls apiUrls = new ApiUrls();
    private FrontendUrls frontendUrls = new FrontendUrls();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ApiUrls getApiUrls() {
        return apiUrls;
    }

    public void setApiUrls(ApiUrls apiUrls) {
        this.apiUrls = apiUrls;
    }

    public FrontendUrls getFrontendUrls() {
        return frontendUrls;
    }

    public void setFrontendUrls(FrontendUrls frontendUrls) {
        this.frontendUrls = frontendUrls;
    }

    public static class ApiUrls {
        private String base;
        private String ask;
        private String add;
        private String delete;
        private String status;

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getAsk() {
            return ask;
        }

        public void setAsk(String ask) {
            this.ask = ask;
        }

        public String getAdd() {
            return add;
        }

        public void setAdd(String add) {
            this.add = add;
        }

        public String getDelete() {
            return delete;
        }

        public void setDelete(String delete) {
            this.delete = delete;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

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

        public String getBase() {
            return base;
        }

        public void setBase(String base) {
            this.base = base;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getChat() {
            return chat;
        }

        public void setChat(String chat) {
            this.chat = chat;
        }

        public String getDashboard() {
            return dashboard;
        }

        public void setDashboard(String dashboard) {
            this.dashboard = dashboard;
        }

        public String getDeleteProject() {
            return deleteProject;
        }

        public void setDeleteProject(String deleteProject) {
            this.deleteProject = deleteProject;
        }

        public String getLoad() {
            return load;
        }

        public void setLoad(String load) {
            this.load = load;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getLogout() {
            return logout;
        }

        public void setLogout(String logout) {
            this.logout = logout;
        }

        public String getNewProject() {
            return newProject;
        }

        public void setNewProject(String newProject) {
            this.newProject = newProject;
        }

        public String getNewUser() {
            return newUser;
        }

        public void setNewUser(String newUser) {
            this.newUser = newUser;
        }
    }
}
