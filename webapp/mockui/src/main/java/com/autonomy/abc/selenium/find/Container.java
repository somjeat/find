package com.autonomy.abc.selenium.find;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public enum Container {
    LEFT("left-side"),
    MIDDLE("middle"),
    RIGHT("right-side");

    private final String container;

    Container(final String container) {
        this.container = container;
    }

    private String asCssClass() {
        return '.' + container + "-container";
    }

    public void waitForLoad(final WebDriver driver) {
         try {
             new WebDriverWait(driver, 5)
                     .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(asCssClass() + " .loading-spinner")));
         }
         catch (final Exception e) {
             //Noop
         }
        new WebDriverWait(driver, 60)
                .withMessage("Container " + this + " failed to load")
                .until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(final WebDriver driver) {
                        return noLoadingIndicatorsPresent(driver);
                    }
                });
    }

    public WebElement findUsing(final WebDriver driver) {
        return currentTabContents(driver).findElement(By.cssSelector(asCssClass()));
    }

    public static WebElement currentTabContents(final WebDriver driver) {
        return driver.findElement(By.cssSelector(".query-service-view-container > :not(.hide):not(.search-tabs-container), div[data-pagename=search]"));
    }

    public static boolean noLoadingIndicatorsPresent(final WebDriver driver) {
        return driver.findElements(By.cssSelector(".numeric-parametric-loading-indicator:not(.hide)")).isEmpty() &&
                driver.findElements(By.cssSelector(".parametric-processing-indicator:not(.hide)")).isEmpty();
    }
}
