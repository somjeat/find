package com.autonomy.abc.shared;

import com.autonomy.abc.selenium.application.SOElementFactory;
import com.autonomy.abc.selenium.application.SearchOptimizerApplication;
import com.autonomy.abc.selenium.users.UserService;
import com.autonomy.abc.selenium.users.UsersPage;
import com.hp.autonomy.frontend.selenium.application.LoginService;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.control.Session;
import com.hp.autonomy.frontend.selenium.control.Window;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.users.AuthenticationStrategy;
import com.hp.autonomy.frontend.selenium.users.NewUser;
import com.hp.autonomy.frontend.selenium.users.Role;
import com.hp.autonomy.frontend.selenium.users.User;
import com.hp.autonomy.frontend.selenium.util.Waits;
import org.openqa.selenium.TimeoutException;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;

import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.assertThat;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.verifyThat;
import static com.hp.autonomy.frontend.selenium.matchers.ControlMatchers.url;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.modalIsDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.lift.Matchers.displayed;

public class UserTestHelper {
    private SearchOptimizerApplication<?> app;
    private AuthenticationStrategy authStrategy;
    private LoginService loginService;
    private UserService userService;
    private TestConfig config;
    private SOElementFactory factory;

    public UserTestHelper(SearchOptimizerApplication<?> app, TestConfig config) {
        this.app = app;
        this.authStrategy = config.getAuthenticationStrategy();
        this.userService = app.userService();
        this.loginService = app.loginService();
        this.config = config;
        this.factory = app.elementFactory();
    }

    public void deleteEmails(Session session) {
        Window firstWindow = session.getActiveWindow();
        Window secondWindow = session.openWindow("about:blank");
        try {
            authStrategy.cleanUp(session.getDriver());
        } catch (TimeoutException e) {
            LoggerFactory.getLogger(UsersPageTestBase.class).warn("Could not tear down");
        } finally {
            secondWindow.close();
            firstWindow.activate();
        }
    }

    public User singleSignUp(NewUser toCreate) {
        UsersPage usersPage = userService.getUsersPage();

        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final ModalView newUserModal = usersPage.userCreationModal();
        User user = usersPage.addNewUser(toCreate, Role.USER);
        authStrategy.authenticate(user);
//		assertThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        verifyUserAdded(newUserModal, user);
        usersPage.closeModal();
        return user;
    }


    public void signUpAndLoginAs(NewUser newUser, Window window) {
        UsersPage usersPage = userService.getUsersPage();

        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());

        User user = usersPage.addNewUser(newUser, Role.USER);
        authStrategy.authenticate(user);
        usersPage.closeModal();

        try {
            Waits.waitForGritterToClear();
        } catch (InterruptedException e) { /**/ }

        logoutAndNavigateToWebApp(window);

        try {
            loginService.login(user);
        } catch (TimeoutException | NoSuchElementException e) { /* Probably because of the sessions you're already logged in */ }

        factory.getPromotionsPage();
        assertThat(window, url(not(containsString("login"))));
    }

    public void deleteAndVerify(User user) {
        UsersPage usersPage = userService.getUsersPage();
        userService.deleteUser(user);

        if (!app.isHosted()) {
            verifyThat(usersPage, containsText("User " + user.getUsername() + " successfully deleted"));
        } else {
            factory.getTopNavBar().openNotifications();
            verifyThat(factory.getTopNavBar().getNotifications().notificationNumber(1), containsText("Deleted user " + user.getUsername()));
            factory.getTopNavBar().closeNotifications();
        }
    }

    public void verifyUserAdded(ModalView newUserModal, User user){
        if (!app.isHosted()){
            verifyThat(newUserModal, containsText("Done! User " + user.getUsername() + " successfully created"));
        }

        //Hosted notifications are dealt with within the sign up method and there is no real way to ensure that a user's been created at the moment
    }

    public void logoutAndNavigateToWebApp(Window window) {
        if (loginService.getCurrentUser() != null) {
            loginService.logout();
        }
        window.goTo(config.getAppUrl(app));
    }

    public void verifyCreateDeleteInTable(NewUser newUser) {
        User user = userService.createNewUser(newUser, Role.USER);
        String username = user.getUsername();

        UsersPage usersPage = userService.getUsersPage();
        verifyThat(usersPage.deleteButton(user), displayed());
        verifyThat(usersPage.getTable(), containsText(username));

        deleteAndVerify(user);
        verifyThat(usersPage.getTable(), not(containsText(username)));
    }

}
