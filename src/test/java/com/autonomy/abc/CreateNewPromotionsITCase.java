package com.autonomy.abc;

import com.autonomy.abc.config.ABCTestBase;
import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.menubar.NavBarTabId;
import com.autonomy.abc.selenium.menubar.TopNavBar;
import com.autonomy.abc.selenium.page.CreateNewPromotionsPage;
import com.autonomy.abc.selenium.page.PromotionsPage;
import com.autonomy.abc.selenium.page.SearchPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Platform;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.hamcrest.MatcherAssert.assertThat;

public class CreateNewPromotionsITCase extends ABCTestBase {

	public CreateNewPromotionsITCase(final TestConfig config, final String browser, final Platform platform) {
		super(config, browser, platform);
	}

	private SearchPage searchPage;
	private String promotedDocTitle;
	private PromotionsPage promotionsPage;
	private TopNavBar topNavBar;

	private CreateNewPromotionsPage createPromotionsPage;

	@Before
	public void setUp() {
		topNavBar = body.getTopNavBar();
		topNavBar.search("fox");
		searchPage = body.getSearchPage();
		promotedDocTitle = searchPage.createAPromotion();
		createPromotionsPage = body.getCreateNewPromotionsPage();
	}

	@After
	public void cleanUp() {
		promotionsPage = body.getPromotionsPage();
		promotionsPage.deleteAllPromotions();
	}

	@Test
	public void testAddPinToPosition() {
		createPromotionsPage.pinToPosition().click();
		createPromotionsPage.continueButton("type").click();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select the position"));
		assertThat("Continue button is not disabled when position equals 0", createPromotionsPage.continueButton("pinToPosition").getAttribute("class").contains("disabled"));
		assertThat("Minus button is not disabled when position equals 0", createPromotionsPage.selectPositionMinusButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);

		createPromotionsPage.continueButton("pinToPosition").click();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Promotion Triggers"));
		assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.finishButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.addSearchTrigger("animal");
		assertThat("Promote button is not disabled when no triggers are added", !createPromotionsPage.finishButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.finishButton().click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("animal").click();

		new WebDriverWait(getDriver(),5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		assertThat("page does not have pin to position name", promotionsPage.getText().contains("animal"));
		assertThat("page does not have correct pin to position number", promotionsPage.getText().contains("Pinned to position: 1"));
	}

	@Test
	public void testPinToPositionSetPosition() {
		createPromotionsPage.pinToPosition().click();
		createPromotionsPage.continueButton("type").click();
		createPromotionsPage.loadOrFadeWait();

		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 1", createPromotionsPage.positionInputValue() == 1);
		assertThat("Minus button is not enabled when position equals 1", !createPromotionsPage.continueButton("pinToPosition").getAttribute("class").contains("disabled"));
		assertThat("Continue button is not enabled when position equals 1", !createPromotionsPage.selectPositionMinusButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		createPromotionsPage.selectPositionPlusButton().click();
		assertThat("Pin to position value not set to 5", createPromotionsPage.positionInputValue() == 5);

		createPromotionsPage.selectPositionMinusButton().click();
		createPromotionsPage.selectPositionMinusButton().click();
		createPromotionsPage.selectPositionMinusButton().click();
		assertThat("Pin to position value not set to 5", createPromotionsPage.positionInputValue() == 2);

		createPromotionsPage.typePositionNumber(16);
		assertThat("Pin to position value not set to 16", createPromotionsPage.positionInputValue() == 16);

		createPromotionsPage.cancelButton("pinToPosition").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
	}

	@Test
	public void testAddRemoveTriggerTermsAndCancel() {
		createPromotionsPage.navigateToTriggers();
		assertThat("Wizard has not progressed to Select the position", createPromotionsPage.getText().contains("Select Promotion Triggers"));
		assertThat("Trigger add button is not disabled when text box is empty", createPromotionsPage.triggerAddButton().getAttribute("class").contains("disabled"));
		assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.finishButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.addSearchTrigger("animal");
		assertThat("Promote button is not enabled when a trigger is added", !createPromotionsPage.finishButton().getAttribute("class").contains("disabled"));
		assertThat("animal search trigger not added", createPromotionsPage.getSearchTriggersList().contains("animal"));

		createPromotionsPage.removeSearchTrigger("animal");
		assertThat("animal search trigger not removed", !createPromotionsPage.getSearchTriggersList().contains("animal"));
		assertThat("Promote button is not disabled when no triggers are added", createPromotionsPage.finishButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.addSearchTrigger("bushy tail");
		assertThat("Number of triggers does not equal 2", createPromotionsPage.getSearchTriggersList().size() == 2);
		assertThat("bushy search trigger not added", createPromotionsPage.getSearchTriggersList().contains("bushy"));
		assertThat("tail search trigger not added", createPromotionsPage.getSearchTriggersList().contains("tail"));

		createPromotionsPage.removeSearchTrigger("tail");
		assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);
		assertThat("bushy search trigger not present", createPromotionsPage.getSearchTriggersList().contains("bushy"));
		assertThat("tail search trigger not removed", !createPromotionsPage.getSearchTriggersList().contains("tail"));

		createPromotionsPage.cancelButton("trigger").click();
		assertThat("Wizard has not cancelled", !getDriver().getCurrentUrl().contains("create"));
	}

	@Test
	public void testWhitespaceTrigger() {
		createPromotionsPage.navigateToTriggers();
		createPromotionsPage.triggerAddButton().click();
		assertThat("Number of triggers does not equal 0", createPromotionsPage.getSearchTriggersList().size() == 0);

		createPromotionsPage.addSearchTrigger("trigger");
		assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

		createPromotionsPage.addSearchTrigger("   ");
		assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

		createPromotionsPage.addSearchTrigger(" trigger");
		assertThat("Whitespace at beginning should be ignored", createPromotionsPage.getSearchTriggersList().size() == 1);

		createPromotionsPage.addSearchTrigger("\t");
		assertThat("Whitespace at beginning should be ignored", createPromotionsPage.getSearchTriggersList().size() == 1);
	}

	@Test
	public void testQuotesTrigger() {
		createPromotionsPage.navigateToTriggers();
		createPromotionsPage.triggerAddButton().click();
		assertThat("Number of triggers does not equal 0", createPromotionsPage.getSearchTriggersList().size() == 0);

		createPromotionsPage.addSearchTrigger("bag");
		createPromotionsPage.addSearchTrigger("\"bag");
		createPromotionsPage.addSearchTrigger("bag\"");
		createPromotionsPage.addSearchTrigger("\"bag\"");
		assertThat("Number of triggers does not equal 4", createPromotionsPage.getSearchTriggersList().size() == 4);

		createPromotionsPage.removeSearchTrigger("\"bag\"");
		assertThat("Number of triggers does not equal 3", createPromotionsPage.getSearchTriggersList().size() == 3);

		createPromotionsPage.removeSearchTrigger("\"bag");
		assertThat("Number of triggers does not equal 2", createPromotionsPage.getSearchTriggersList().size() == 2);

		createPromotionsPage.removeSearchTrigger("bag\"");
		assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

		createPromotionsPage.removeSearchTrigger("bag");
		assertThat("Number of triggers does not equal 0", createPromotionsPage.getSearchTriggersList().size() == 0);
	}

	@Test
	public void testCommasTrigger() {
		createPromotionsPage.navigateToTriggers();
		createPromotionsPage.addSearchTrigger("France");
		assertThat("Number of triggers does not equal 1", createPromotionsPage.getSearchTriggersList().size() == 1);

		createPromotionsPage.addSearchTrigger(",Germany");
		assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 1);

		createPromotionsPage.addSearchTrigger("Ita,ly Spain");
		assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 2);

		createPromotionsPage.addSearchTrigger("Ireland, Belgium");
		assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 3);

		createPromotionsPage.addSearchTrigger("UK , Luxembourg");
		assertThat("Commas should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 5);
	}

	@Test
	public void testScriptTrigger() {
		createPromotionsPage.navigateToTriggers();
		createPromotionsPage.addSearchTrigger("<script> alert(\"We don't want this to happen\") </script>");
		assertThat("Scripts should not be included in triggers", createPromotionsPage.getSearchTriggersList().size() == 8);
	}

	@Test
	public void testAddRemoveTriggersAndComplete() {
		createPromotionsPage.navigateToTriggers();
		createPromotionsPage.addSearchTrigger("alpha");
		createPromotionsPage.addSearchTrigger("beta gamma delta");
		createPromotionsPage.removeSearchTrigger("gamma");
		createPromotionsPage.removeSearchTrigger("alpha");
		createPromotionsPage.addSearchTrigger("epsilon");
		createPromotionsPage.removeSearchTrigger("beta");
		assertThat("Number of triggers does not equal 2", createPromotionsPage.getSearchTriggersList().size() == 2);

		createPromotionsPage.finishButton().click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining("delta").click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));

		assertThat("page does not have pin to position name", promotionsPage.getText().contains("delta"));
		assertThat("page does not have correct pin to position number", promotionsPage.getText().contains("Pinned to position: 1"));
	}

	@Test
	public void testAddSpotlightSponsored() {
		addSpotlightPromotion("Sponsored", "apples");
	}

	@Test
	public void testAddSpotlightHotwire() {
		addSpotlightPromotion("Hotwire", "grapes");
	}

	@Test
	public void testAddSpotlightTopPromotions() {
		addSpotlightPromotion("Top Promotions", "oranges");
	}

	private void addSpotlightPromotion(final String spotlightType, final String searchTrigger) {
		createPromotionsPage.spotlight().click();
		assertThat("Continue button not enabled", !createPromotionsPage.continueButton("type").getAttribute("class").contains("disabled"));

		createPromotionsPage.continueButton("type").click();
		assertThat("Continue button not disabled", createPromotionsPage.continueButton("spotlightType").getAttribute("class").contains("disabled"));

		createPromotionsPage.spotlightType(spotlightType).click();
		assertThat("Continue button not enabled", !createPromotionsPage.continueButton("spotlightType").getAttribute("class").contains("disabled"));

		createPromotionsPage.continueButton("spotlightType").click();
		assertThat("Promote button not disabled", createPromotionsPage.finishButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.addSearchTrigger(searchTrigger);
		assertThat("Promote button not enabled", !createPromotionsPage.finishButton().getAttribute("class").contains("disabled"));

		createPromotionsPage.finishButton().click();

		new WebDriverWait(getDriver(), 5).until(ExpectedConditions.visibilityOf(searchPage.promoteButton()));
		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		promotionsPage = body.getPromotionsPage();
		promotionsPage.getPromotionLinkWithTitleContaining(searchTrigger).click();

		new WebDriverWait(getDriver(),3).until(ExpectedConditions.visibilityOf(promotionsPage.triggerAddButton()));
		assertThat("Linked to wrong page", getDriver().getCurrentUrl().contains("promotions/detail/spotlight"));
		assertThat("Linked to wrong page", promotionsPage.getText().contains("Spotlight for: " + searchTrigger));

		promotionsPage.clickableSearchTrigger(searchTrigger).click();

		assertThat("Wrong document spotlighted", createPromotionsPage.getTopPromotedLinkTitle().equals(promotedDocTitle));
		assertThat("Wrong spotlight button text", createPromotionsPage.getTopPromotedLinkButtonText().equals(spotlightType));

		searchPage.showHideUnmodifiedResults().click();
		searchPage.loadOrFadeWait();
		assertThat("Modified results have not been hidden", !searchPage.getText().contains(promotedDocTitle));

		searchPage.showHideUnmodifiedResults().click();
		searchPage.loadOrFadeWait();
		assertThat("Modified results have not been shown", searchPage.getText().contains(promotedDocTitle));

		navBar.getTab(NavBarTabId.PROMOTIONS).click();
		assertThat("Linked to wrong page", getDriver().getCurrentUrl().contains("promotions"));

		assertThat("page does not have correct spotlight name", promotionsPage.getText().contains("Spotlight for: " + searchTrigger));
		assertThat("page does not have correct spotlight type", promotionsPage.spotlightButton().getText().contains(spotlightType));
	}

}
