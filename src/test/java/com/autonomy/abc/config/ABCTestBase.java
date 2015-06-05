package com.autonomy.abc.config;

import com.autonomy.abc.selenium.AppElement;
import com.autonomy.abc.selenium.element.ModalView;
import com.autonomy.abc.selenium.menubar.SideNavBar;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.AppBody;
import com.autonomy.abc.selenium.page.login.LoginHostedPage;
import com.autonomy.abc.selenium.util.ImplicitWaits;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;


@Ignore
@RunWith(Parameterized.class)
public abstract class ABCTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(ABCTestBase.class);
	private final static Set<String> USER_BROWSERS;

	public final TestConfig config;
	public final String browser;
	private final Platform platform;
	private WebDriver driver;
	public AppBody body;
	protected SideNavBar navBar;
	protected TopNavBar topNavBar;

	static {
		final String[] allBrowsers = {"firefox", "internet explorer", "chrome"};
		final String browserProperty = System.getProperty("com.autonomy.browsers");

		if (browserProperty == null) {
			USER_BROWSERS = new HashSet<>(Arrays.asList(allBrowsers));
		} else {
			USER_BROWSERS = new HashSet<>();

			for (final String browser : allBrowsers) {
				if (browserProperty.contains(browser)) {
					USER_BROWSERS.add(browser);
				}
			}
		}
	}

	public ABCTestBase(final TestConfig config, final String browser, final Platform platform) {
		this.config = config;
		this.browser = browser;
		this.platform = platform;
	}

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() throws MalformedURLException {
		final Collection<TestConfig.ApplicationType> applicationType = Arrays.asList(TestConfig.ApplicationType.HOSTED, TestConfig.ApplicationType.ON_PREM);
		return parameters(applicationType);
	}

	protected static List<Object[]> parameters(final Iterable<TestConfig.ApplicationType> applicationTypes) throws MalformedURLException {
		final List<Object[]> output = new ArrayList<>();

		for (final TestConfig.ApplicationType type : applicationTypes) {
			for (final String browser : USER_BROWSERS) {
				// TODO: Make type an element of the Object[]
				output.add(new Object[]{
						new TestConfig(output.size(), type),
						browser,
						Platform.WINDOWS
				});
			}
		}
		return output;
	}

	@Before
	public void baseSetUp() throws MalformedURLException {
		LOGGER.info("parameter-set: [" + config.getIndex() + "]; browser: " + browser + "; platform: " + platform);
		driver = config.createWebDriver(browser, platform);
		ImplicitWaits.setImplicitWait(driver);
		driver.get(config.getWebappUrl());
		getDriver().manage().window().maximize();

		if (config.getType() == TestConfig.ApplicationType.ON_PREM) {
			abcOnPremiseLogin("richard", "q");
		} else {
			abcHostedLogin(System.getProperty("com.autonomy.apiKey"));
		}

		body = new AppBody(driver);
		navBar = new SideNavBar(driver);
		topNavBar = new TopNavBar(driver);
	}

	@After
	public void baseTearDown() {
		driver.quit();
	}

	public WebDriver getDriver() {
		return driver;
	}

	public TestConfig getConfig() {
		return config;
	}

	public void abcOnPremiseLogin(final String userName, final String password) {
		driver.findElement(By.cssSelector("[name='username']")).clear();
		driver.findElement(By.cssSelector("[name='username']")).sendKeys(userName);
		driver.findElement(By.cssSelector("[name='password']")).clear();
		driver.findElement(By.cssSelector("[name='password']")).sendKeys(password);
		driver.findElement(By.cssSelector("[type='submit']")).click();
		new WebDriverWait(driver, 15).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-static-top-blue")));
	}

	public void abcHostedLogin(final String apiKey) {
		final AppElement appElement = new AppElement(body, driver);
		appElement.loadOrFadeWait();
		final WebDriverWait wait = new WebDriverWait(driver, 40);
		new LoginHostedPage(body, driver).loginWith(LoginHostedPage.LoginProviders.API_KEY);
		appElement.loadOrFadeWait();

		/* Clicking APIKey Button doesn't always open the modal first time. The if below will retry the button if the modal doesn't open */
		if (getDriver().findElements(By.cssSelector(".modal[aria-hidden='false']")).size() == 0) {
			new LoginHostedPage(body, driver).loginWith(LoginHostedPage.LoginProviders.API_KEY);
		}

		wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector(".modal[aria-hidden='false']"))));
		wait.until(ExpectedConditions.visibilityOf(ModalView.getVisibleModalView(driver).findElement(By.cssSelector(".js-apikey-input"))));
		ModalView.getVisibleModalView(driver).findElement(By.cssSelector(".js-apikey-input")).sendKeys(apiKey);
		wait.until(ExpectedConditions.visibilityOf(ModalView.getVisibleModalView(driver).findElement(By.id("apikey_submit"))));
		ModalView.getVisibleModalView(driver).findElement(By.id("apikey_submit")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-static-top-blue")));
	}
}

