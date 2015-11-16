package com.autonomy.abc.selenium.page.promotions;

import com.autonomy.abc.selenium.element.*;
import com.autonomy.abc.selenium.util.Predicates;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.util.AppElement;
import com.hp.autonomy.frontend.selenium.util.AppPage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

public class PromotionsDetailPage extends AppElement implements AppPage {
    public PromotionsDetailPage(WebDriver driver) {
        super(new WebDriverWait(driver, 30).until(ExpectedConditions.visibilityOfElementLocated(By.className("wrapper-content"))), driver);
        waitForLoad();
    }

    @Override
    public void waitForLoad() {
        new WebDriverWait(getDriver(), 10)
                .withMessage("Failed to load Promotions Detail Page")
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("promotion-match-terms")));
    }

    public Dropdown editMenu() {
        return new Dropdown(findElement(By.className("extra-functions")), getDriver());
    }

    @Deprecated
    public void delete() {
        System.err.println("PromotionDetailsPage.delete no longer works, see CSA-1619");
        final Dropdown editMenu = editMenu();
        editMenu.open();
        editMenu.getItem("Delete").click();
        final ModalView deleteModal = ModalView.getVisibleModalView(getDriver());
        deleteModal.findElement(By.cssSelector(".btn-danger")).click();
    }

    public Editable promotionTitle() {
        return new InlineEdit(findElement(By.className("promotion-title-edit")), getDriver());
    }

    public String getPromotionType() {
        return findElement(By.cssSelector(".promotion-view-name")).getText();
    }

    public Dropdown spotlightTypeDropdown() {
        return new Dropdown(findElement(By.className("promotion-view-name-dropdown")), getDriver());
    }

    public Editable pinPosition() {
        final WebElement group = AppElement.getParent(findElement(By.cssSelector(".promotion-position-edit")));
        return new Editable() {
            @Override
            public String getValue() {
                return group.getText();
            }

            @Override
            public WebElement editButton() {
                return group.findElement(By.cssSelector(".inline-edit-open-form"));
            }

            @Override
            public void setValueAsync(String value) {
                int initialValue = Integer.valueOf(getValue());
                editButton().click();
                int desiredValue = Integer.valueOf(value);
                changeValue(initialValue, desiredValue);
                findElement(By.cssSelector(".hp-check")).click();
            }

            private void changeValue(int initialValue, int desiredValue) {
                WebElement button;
                int repeats;
                if (desiredValue > initialValue) {
                    button = group.findElement(By.cssSelector(".plus"));
                    repeats = desiredValue - initialValue;
                } else {
                    button = group.findElement(By.cssSelector(".minus"));
                    repeats = initialValue - desiredValue;
                }
                for (int i=0; i<repeats; i++) {
                    button.click();
                }
            }

            @Override
            public void setValueAndWait(String value) {
                setValueAsync(value);
                waitForUpdate();
            }

            @Override
            public void waitForUpdate() {
                new WebDriverWait(getDriver(), 20).withMessage("waiting for pin position to update").until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".promotion-position-edit .fa-refresh")));
            }

            @Override
            public WebElement getElement() {
                return group;
            }
        };
    }

    public String getLanguage() {
        return findElement(By.className("promotion-language")).getText();
    }

    public List<String> getTriggerList() {
        final List<String> triggers = new ArrayList<>();
        for (final WebElement trigger : triggerElements()) {
            triggers.add(trigger.getAttribute("data-id"));
        }
        return triggers;
    }

    public List<Removable> triggers() {
        final List<Removable> triggers = new ArrayList<>();
        for (final WebElement trigger : triggerElements()) {
            triggers.add(new LabelBox(trigger, getDriver()));
        }
        return triggers;
    }

    private List<WebElement> triggerElements() {
        return findElements(By.cssSelector(".promotion-view-match-terms .term"));
    }

    public Removable trigger(final String triggerName) {
        return new LabelBox(findElement(By.cssSelector(".promotion-view-match-terms [data-id='" + triggerName + "']")), getDriver());
    }

    public FormInput triggerAddBox() {
        return new FormInput(triggerEditor().findElement(By.cssSelector("[name='words']")), getDriver());
    }

    public WebElement triggerAddButton() {
        return triggerEditor().findElement(By.cssSelector("[type='submit']"));
    }

    public String getTriggerError() {
        try {
            return triggerEditor().findElement(By.cssSelector(".help-block")).getText();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private WebElement triggerEditor() {
        return findElement(By.cssSelector(".promotion-match-terms-editor"));
    }

    public void waitForTriggerRefresh() {
        new WebDriverWait(getDriver(), 20).until(Predicates.invisibilityOfAllElementsLocated(By.cssSelector(".promotion-view-match-terms .term .fa-spin")));
    }

    public void addTrigger(String text) {
        triggerAddBox().setAndSubmit(text);
        waitForTriggerRefresh();
    }

    public WebElement addMoreButton() {
        return findElement(By.className("add-more-promoted-documents"));
    }

    public List<WebElement> promotedList() {
        return new WebDriverWait(getDriver(), 10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".promoted-documents-list h3")));
    }

    public List<WebElement> dynamicPromotedList(){
        return new WebDriverWait(getDriver(),10).until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".query-search-results div:not(.hide)>h3")));

    }

    public List<String> getDynamicPromotedTitles(){
        final List<String> docTitles = new ArrayList<>();

        do {
            for (final WebElement docTitle : dynamicPromotedList()) {
                docTitles.add(docTitle.getText());
            }
        } while(clickForwardButton());

        return docTitles;
    }

    public List<String> getPromotedTitles() {
        final List<String> docTitles = new ArrayList<>();

        do {
            waitForPromotedTitlesToLoad();
            for (final WebElement docTitle : promotedList()) {
                docTitles.add(docTitle.getText());
            }
        } while(clickForwardButton());

        return docTitles;
    }

    private void waitForPromotedTitlesToLoad() {
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions.refreshed(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver input) {
                List<WebElement> docs = input.findElements(By.cssSelector(".promoted-documents-list h3"));
                return !(docs.isEmpty() || docs.get(0).getText().contains("Unknown Document"));
            }
        }));
    }

    private WebElement forwardButton(){
        return findElement(By.cssSelector(".query-search-results .fa-angle-right"));
    }

    private boolean clickForwardButton(){
        try {
            if(!forwardButton().findElement(By.xpath(".//../..")).getAttribute("class").contains("disabled")) {
                forwardButton().click();
                loadOrFadeWait();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public WebElement promotedDocument(final String title) {
        return findElement(By.cssSelector("ul.promoted-documents-list")).findElement(By.xpath(".//a[contains(text(), '" + title + "')]/../.."));
    }

    public Removable removablePromotedDocument(final String title) {
        return new LabelBox(promotedDocument(title), getDriver());
    }

    public Editable staticPromotedDocumentTitle() {
        return new InlineEdit(findElement(By.className("static-promotion-title-edit")), getDriver());
    }

    public Editable staticPromotedDocumentContent() {
        return new InlineEdit(findElement(By.className("static-promotion-content-edit")), getDriver());
    }

    public Editable queryText() {
        return new InlineEdit(findElement(By.className("promotion-query-edit")), getDriver());
    }

}
