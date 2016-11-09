package com.autonomy.abc.bi;

import com.autonomy.abc.base.IdolFindTestBase;
import com.autonomy.abc.base.Role;
import com.autonomy.abc.selenium.find.FindPage;
import com.autonomy.abc.selenium.find.FindService;
import com.autonomy.abc.selenium.find.IdolFindPage;
import com.autonomy.abc.selenium.find.application.BIIdolFindElementFactory;
import com.autonomy.abc.selenium.find.application.UserRole;
import com.autonomy.abc.selenium.find.bi.TableView;
import com.autonomy.abc.selenium.find.filters.FilterPanel;
import com.autonomy.abc.selenium.find.filters.ParametricFieldContainer;
import com.hp.autonomy.frontend.selenium.config.TestConfig;
import com.hp.autonomy.frontend.selenium.framework.logging.ResolvedBug;
import org.apache.commons.lang3.text.WordUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.*;

import static com.autonomy.abc.selenium.find.bi.TableView.EntryCount.TWENTY_FIVE;
import static com.hp.autonomy.frontend.selenium.framework.state.TestStateAssert.*;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.containsText;
import static com.hp.autonomy.frontend.selenium.matchers.ElementMatchers.disabled;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.openqa.selenium.lift.Matchers.displayed;

@Role(UserRole.BIFHI)
public class TableITCase extends IdolFindTestBase {

    private BIIdolFindElementFactory elementFactory;
    private TableView tableView;
    private FindService findService;
    private static final int NUMBER_PER_PAGE = 10;

    public TableITCase(final TestConfig config) {
        super(config);
    }

    @Before
    public void setUp() {
        elementFactory = (BIIdolFindElementFactory) getElementFactory();
        tableView = elementFactory.getTableView();
        findService = getApplication().findService();
    }

    @Test
    @ResolvedBug("FIND-251")
    public void testTableTabShowsTable(){
        init("s");

        tableView.waitForTable();
        verifyThat("Table element displayed", tableView.tableVisible());
        verifyThat("Parametric selectors appear", tableView.parametricSelectionDropdownsExist());

        elementFactory.getConceptsPanel().removeAllConcepts();
        findService.search("shambolicwolic");

        IdolFindPage findPage = elementFactory.getFindPage();
        findPage.goToListView();
        assumeThat("There are no results for this", findPage.totalResultsNum(), is(0));

        findPage.goToTable();

        final WebElement message = tableView.message();
        final String correctMessage = "Could not display Table View: your search returned no parametric values";
        assertThat("Message appearing when no sunburst & search from Sunburst", message, displayed());
        verifyThat("Message is: " + correctMessage, message, containsText(correctMessage));
    }

    @Test
    public void testSingleFieldGivesCorrectTableValues() {
        init("dog");

        tableView.waitForTable();
        verifyThat("With single field, table has 2 columns", tableView.columnCount(), is(2));

        checkRowNumber(0);
    }

    @Test
    public void testTwoFieldsGiveCorrectTableValues() {
        elementFactory.getFindPage().goToTable();

        final FilterPanel filters = filters();
        final int reasonableFilterNumber = 10;
        final int goodCategory = filters.nthParametricThatSatisfiedCondition(0,
                (Integer x) -> x < reasonableFilterNumber && x > 0);

        assertThat("There is a filter category with between 1 & " + reasonableFilterNumber + " filters", goodCategory, greaterThan(0));

        final String categoryName = filters.parametricField(goodCategory).filterCategoryName();
        final Map<String, Integer> filterCounts = getHighestResultCountForOtherFilters(goodCategory, categoryName);
        tableView.waitForTable();

        tableView.parametricSelectionDropdown(1).select(WordUtils.capitalize(categoryName.toLowerCase()));
        tableView.waitForTable();

        for(String key : filterCounts.keySet()) {
            tableView.parametricSelectionDropdown(2).select(WordUtils.capitalize(key.toLowerCase()));
            tableView.waitForTable();
            verifyThat("Number of columns is: " + tableView.columnCount() + " for main category " + categoryName + " with second category " + key
                    , tableView.columnCount(), greaterThan(filterCounts.get(key)));
        }

        checkRowNumber(goodCategory);
    }

    /* Selects each filter in categoryName in turn & returns a map
    of the other categories and the highest no. of filters they contain
    when any of the filters in categoryName is selected*/
    private Map<String, Integer> getHighestResultCountForOtherFilters(final int goodCategory, final String categoryName) {
        final FilterPanel filters = filters();
        final FindPage findPage = elementFactory.getFindPage();
        final Map<String, Integer> filterCounts = new HashMap<>();

        for(int i = 0; i < filters.parametricField(goodCategory).getFilters().size(); i++) {
            tableView.waitForTable();
            filters.parametricField(goodCategory).getFilters().get(i).check();

            tableView.waitForTable();
            findPage.waitForParametricValuesToLoad();

            for(ParametricFieldContainer cont : filters.parametricFieldContainers()) {
                final String filterCat = cont.filterCategoryName();
                if(!filterCat.equals(categoryName)) {
                    final Integer filterNum = filterCounts.get(filterCat);
                    if(filterNum == null || filterNum < cont.getFilterNumber()) {
                        filterCounts.put(filterCat, cont.getFilterNumber());
                    }
                }
            }
            filters.parametricField(goodCategory).getFilters().get(0).uncheck();
            findPage.waitForParametricValuesToLoad();
        }
        return filterCounts;
    }

    private void checkRowNumber(final int index) {
        final int filterNumber = filters().parametricField(index).getFilterNumber();
        verifyThat("Number of rows equals number of filters in filter type (or max per page)",
                tableView.rowCount(),
                anyOf(is(NUMBER_PER_PAGE), is(filterNumber)));
    }

    @Test
    public void testPagination() {
        elementFactory.getFindPage().goToTable();
        tableView.waitForTable();

        assumeThat(tableView.currentPage(), is(1));

        final String initialText = tableView.text(1, 0);

        assumeThat("There needs to be enough parametric values to have >1 page", tableView.nextButton(), not(disabled()));

        tableView.nextPage();
        verifyThat(tableView.text(1, 0), is(not(initialText)));
        verifyThat(tableView.currentPage(), is(2));

        tableView.previousPage();
        verifyThat(tableView.text(1, 0), is(initialText));
        verifyThat(tableView.currentPage(), is(1));
    }

    @Test
    public void testSorting() {
        init("*");

        tableView.waitForTable();
        tableView.sort(1, TableView.SortDirection.DESCENDING);

        final int rowCount = tableView.rowCount();

        final List<Integer> values = new ArrayList<>(rowCount);

        for (int i = 1; i <= rowCount; i++) {
            values.add(Integer.parseInt(tableView.text(i, 1)));
        }

        final List<Integer> sortedValues = new ArrayList<>(values);

        // sort will give us ascending order
        Collections.sort(sortedValues);
        Collections.reverse(sortedValues);

        verifyThat(values, is(sortedValues));
    }

    @Test
    public void testSearchInResults() {
        init("whirlpool");

        tableView.waitForTable();

        final String searchText = tableView.text(2, 0);
        tableView.searchInResults(searchText);

        verifyThat(tableView.text(1, 0), is(searchText));
    }

    @Test
    public void testShowEntries() {
        init("*");

        tableView.waitForTable();

        assumeThat("Table needs at least " + NUMBER_PER_PAGE + " rows to test increasing the number to view",
                tableView.maxRow(),
                is(NUMBER_PER_PAGE));

        tableView.showEntries(TWENTY_FIVE);

        verifyThat(tableView.maxRow(), is(greaterThan(NUMBER_PER_PAGE)));
    }

    @Test
    @ResolvedBug("FIND-383")
    public void testSideBarFiltersChangeTable(){
        init("lashing");

        tableView.waitForTable();

        FilterPanel filters = filters();
        final String parametricSelectionFirst= tableView.getSelectedFieldName(1);

        ParametricFieldContainer container = filters.parametricContainer(parametricSelectionFirst);
        container.expand();
        container.getFilters().get(0).check();

        tableView.waitForTable();
        assertThat("Parametric selection changed", tableView.getSelectedFieldName(1), not(Matchers.is(parametricSelectionFirst)));
    }

    @Test
    @ResolvedBug("FIND-405")
    public void testParametricSelectors(){
        init("wild horses");

        int index = filters().nonZeroParamFieldContainer(0);
        final String firstParametric = filters().parametricField(index).filterCategoryName();
        verifyThat("Default parametric selection is 1st parametric type", firstParametric, startsWith(tableView.getSelectedFieldName(1).toUpperCase()));

        tableView.parametricSelectionDropdown(2).open();
        verifyThat("1st selected parametric does not appear as choice in 2nd", tableView.getParametricDropdownItems(2), not(contains(firstParametric)));
    }

    private void init(final String searchText) {
        findService.search(searchText);
        elementFactory.getFindPage().goToTable();
    }

    private FilterPanel filters() {
        return elementFactory.getFilterPanel();
    }
}