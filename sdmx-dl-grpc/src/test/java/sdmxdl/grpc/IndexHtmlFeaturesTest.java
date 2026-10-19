package sdmxdl.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Tests the web UI helpers and the REST v2 features of index.html against the in-memory manager of
 * {@link MockedSdmxWebManagerProducer}.
 */
@QuarkusTest
class IndexHtmlFeaturesTest {

    @TestHTTPResource("/")
    URL testUrl;

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        if ("true".equals(System.getProperty("headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--force-prefers-reduced-motion");
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        js = (JavascriptExecutor) driver;
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void open(String query) {
        driver.get(testUrl + "index.html" + query);
        wait.until(d -> Boolean.TRUE.equals(js.executeScript("return typeof loadFormFromUrl === 'function'")));
    }

    private Object eval(String expression) {
        return js.executeScript("return " + expression);
    }

    private void waitFor(String condition) {
        wait.until(d -> Boolean.TRUE.equals(js.executeScript("return !!(" + condition + ")")));
    }

    // ========================================================================
    // RESOURCES
    // ========================================================================

    @Test
    @DisplayName("Page loads its stylesheet and scripts without errors")
    void testResourcesLoadWithoutErrors() {
        open("?tab=about");
        waitFor("document.readyState === 'complete'");

        assertThat(eval("getComputedStyle(document.querySelector('.container')).maxWidth"))
                .as("app.css should be applied")
                .isEqualTo("1200px");
        assertThat((Long) eval("[...document.scripts].filter(s => s.src.includes('/js/')).length"))
                .isGreaterThan(10L);

        List<String> errors = driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                .filter(entry -> entry.getLevel().intValue() >= Level.SEVERE.intValue())
                .map(LogEntry::getMessage)
                .filter(message -> !message.contains("favicon") && !message.contains("icons.duckduckgo.com"))
                .collect(Collectors.toList());
        assertThat(errors).isEmpty();
    }

    // ========================================================================
    // HELPERS
    // ========================================================================

    @Test
    @DisplayName("escapeHtml escapes markup and quotes")
    void testEscapeHtml() {
        open("?tab=about");

        assertThat(eval("escapeHtml(`<a href=\"x\" title='y'>&</a>`)"))
                .isEqualTo("&lt;a href=&quot;x&quot; title=&#39;y&#39;&gt;&amp;&lt;/a&gt;");
        assertThat(eval("escapeHtml(null)")).isEqualTo("");
        assertThat(eval("escapeHtml(undefined)")).isEqualTo("");
        assertThat(eval("escapeHtml(0)")).isEqualTo("0");
    }

    @Test
    @DisplayName("jsArg produces a safe JavaScript argument for inline handlers")
    void testJsArg() {
        open("?tab=about");

        String tricky = "a'b\"c</script><img src=x onerror=window.__xss=1>\\d";
        js.executeScript(
                "const div = document.createElement('div');"
                        + "div.innerHTML = `<button id=\"js-arg-test\" onclick=\"window.__received = ${jsArg(arguments[0])}\">x</button>`;"
                        + "document.body.appendChild(div);"
                        + "document.getElementById('js-arg-test').click();",
                tricky);

        assertThat(eval("window.__received")).isEqualTo(tricky);
        assertThat(eval("window.__xss")).isNull();
    }

    @Test
    @DisplayName("formatPeriod labels periods according to their frequency")
    void testFormatPeriod() {
        open("?tab=about");

        Map<String, String> expected = Map.of(
                "2024-01-01T00:00:00/P1Y", "2024",
                "2024-07-01T00:00:00/P6M", "2024-S2",
                "2024-04-01T00:00:00/P3M", "2024-Q2",
                "2024-03-01T00:00:00/P1M", "2024-03",
                "2024-03-15T00:00:00/P1D", "2024-03-15",
                "2024-03-15T10:30:00/PT1H", "2024-03-15 10:30:00",
                "2024-02-01T00:00:00/P3M", "2024-02-01");
        expected.forEach((period, label) -> assertThat(js.executeScript("return formatPeriod(arguments[0])", period))
                .as(period)
                .isEqualTo(label));
    }

    @Test
    @DisplayName("getDateFormat matches the frequency of the periods")
    void testGetDateFormat() {
        open("?tab=about");

        assertThat(eval("getDateFormat(['2023-01-01T00:00:00/P1Y', '2024-01-01T00:00:00/P1Y'])"))
                .isEqualTo("%Y");
        assertThat(eval("getDateFormat(['2024-01-01T00:00:00/P1M', '2024-04-01T00:00:00/P3M'])"))
                .isEqualTo("%Y-%m");
        assertThat(eval("getDateFormat(['2024-01-01T00:00:00/P1D'])")).isEqualTo("%Y-%m-%d");
    }

    @Test
    @DisplayName("getSeriesDisplayNames prefers unique titles over keys")
    void testGetSeriesDisplayNames() {
        open("?tab=about");

        assertThat(eval("getSeriesDisplayNames(["
                        + "{key: 'A', meta: {TITLE: 'Alpha'}},"
                        + "{key: 'B', meta: {TITLE: 'Same'}},"
                        + "{key: 'C', meta: {TITLE: 'Same'}},"
                        + "{key: 'D'},"
                        + "{}])"))
                .asList()
                .containsExactly("Alpha", "B", "C", "D", "Series 5");
    }

    @Test
    @DisplayName("Chart label helpers shorten and escape texts")
    void testChartLabelHelpers() {
        open("?tab=about");

        assertThat(eval("shortenLabel('abcdef', 4)")).isEqualTo("abc…");
        assertThat(eval("shortenLabel('abc', 4)")).isEqualTo("abc");
        assertThat(eval("escapePlotlyTemplate('%{y} <b>')")).isEqualTo("%&#123;y} &lt;b&gt;");
    }

    @Test
    @DisplayName("apiUrl encodes segments and skips empty parameters")
    void testApiUrl() {
        open("?tab=about");

        assertThat((String) eval("apiUrl(['A B', 'EXR', 'data'], {key: 'M..EUR', database: '', lastN: 3})"))
                .endsWith("/sdmx-dl/v2/A%20B/EXR/data?key=M..EUR&lastN=3");
        assertThat((String) eval("apiUrl(['health'], {sources: ['ECB', 'BBK']})"))
                .endsWith("/sdmx-dl/v2/health?sources=ECB&sources=BBK");
    }

    // ========================================================================
    // FEATURES
    // ========================================================================

    @Test
    @DisplayName("Tabs are grouped by workflow step and mirrored by a select for small screens")
    void testTabGroups() {
        open("?tab=meta");
        assertThat(eval("[...document.querySelectorAll('.tabs .tab-group-start')].map(e => e.dataset.group)"))
                .asList()
                .containsExactly("Discover", "Browse", "Retrieve");
        assertThat(eval("document.getElementById('tab-select').value")).isEqualTo("meta");

        js.executeScript("const select = document.getElementById('tab-select');"
                + "select.value = 'data'; select.dispatchEvent(new Event('change'));");
        assertThat(eval("document.getElementById('data-tab').classList.contains('active')"))
                .isEqualTo(true);
        assertThat(eval("document.querySelector('.tab[data-tab=data]').classList.contains('active')"))
                .isEqualTo(true);
        assertThat(eval("new URL(location).searchParams.get('tab')")).isEqualTo("data");
    }

    @Test
    @DisplayName("Data tab shows titles, period labels and applies data options")
    void testDataTab() {
        open("?tab=data&source=ECB&flow=EXR&key=M..EUR.SP00.A");
        js.executeScript("document.getElementById('data-lastn').value = '3'; getData();");
        waitFor("document.getElementById('data-chart').data && document.querySelector('.data-table')");

        assertThat((String) eval("window.dataRequestUrl")).contains("lastN=3");
        assertThat(eval("document.getElementById('data-chart').data.map(t => t.name)"))
                .asList()
                .containsExactly("Swiss franc/Euro", "US dollar/Euro");
        assertThat(eval("document.getElementById('data-chart').data[0].customdata"))
                .asList()
                .containsExactly("2024-10", "2024-11", "2024-12");
        assertThat(eval("document.getElementById('data-chart').layout.xaxis.tickformat"))
                .isEqualTo("%Y-%m");
        assertThat(eval("[...document.querySelectorAll('.data-table th .series-title')].map(e => e.textContent)"))
                .asList()
                .containsExactly("Swiss franc/Euro", "US dollar/Euro");
        assertThat(eval("document.querySelector('.data-table tbody td').textContent"))
                .isIn("2024-10", "2024-12");
    }

    @Test
    @DisplayName("Data and Flows copy menus list script targets")
    void testScriptTargets() {
        open("?tab=data");
        waitFor("document.querySelector('#data-script-items .script-item')");

        assertThat((String) eval("document.getElementById('data-script-items').textContent"))
                .contains("python/cli", "python/rest");
        assertThat((String) eval("document.getElementById('flows-script-items').textContent"))
                .contains("python/cli");
    }

    @Test
    @DisplayName("Key Builder filters dimension codes by availability by default")
    void testKeyBuilderAvailability() {
        open("?tab=keybuilder&source=ECB&flow=EXR");
        js.executeScript("loadKeyBuilderStructure()");
        waitFor("document.getElementById('help-CURRENCY')?.textContent.includes(' of ')");

        assertThat(driver.findElement(By.id("kb-use-availability")).isSelected())
                .isTrue();
        assertThat(eval("document.querySelectorAll('.dimension-row').length")).isEqualTo(5L);
        assertThat(eval("document.getElementById('help-CURRENCY').textContent")).isEqualTo("2 of 12 codes available");
        assertThat(eval("document.getElementById('help-CURRENCY_DENOM').textContent"))
                .isEqualTo("1 of 12 codes available");
        assertThat(eval("Object.keys(document.getElementById('dim-CURRENCY').tomselect.options).length"))
                .as("Dropdown should only offer the available codes")
                .isEqualTo(4L);

        driver.findElement(By.id("kb-use-availability")).click();
        waitFor("document.getElementById('help-CURRENCY').textContent === '12 available codes'");

        assertThat(eval("document.getElementById('dim-CURRENCY').options.length"))
                .isEqualTo(14L);
        assertThat(eval("Object.keys(document.getElementById('dim-CURRENCY').tomselect.options).length"))
                .isEqualTo(14L);
    }

    @Test
    @DisplayName("Key fields of the Data and Compare tabs autocomplete available codes")
    void testDataKeyAutocomplete() {
        open("?tab=data&source=ECB&flow=EXR");
        driver.findElement(By.id("data-key")).sendKeys("M.");
        waitFor("document.querySelectorAll('#data-key ~ .autocomplete-dropdown.show .autocomplete-item').length > 1");

        assertThat(eval("[...document.querySelectorAll('#data-key ~ .autocomplete-dropdown .autocomplete-item')]"
                        + ".map(item => item.dataset.code)"))
                .asList()
                .hasSize(3)
                .first()
                .isEqualTo("+");

        driver.findElement(By.id("data-key")).sendKeys(Keys.ARROW_DOWN, Keys.ARROW_DOWN, Keys.ENTER);
        assertThat((String) eval("document.getElementById('data-key').value")).matches("M\\.[A-Z]{3}\\.");

        open("?tab=compare&source=ECB&flow=EXR");
        driver.findElement(By.id("compare-key")).click();
        waitFor(
                "document.querySelectorAll('#compare-key ~ .autocomplete-dropdown.show .autocomplete-item').length > 1");
    }

    @Test
    @DisplayName("Source and flow fields show suggestions from their datalist")
    void testListSuggestions() {
        open("?tab=data");
        waitFor("document.getElementById('sources-list').children.length > 0");

        assertThat(eval("document.getElementById('data-source').hasAttribute('list')"))
                .as("Native datalist popup should be replaced")
                .isEqualTo(false);

        driver.findElement(By.id("data-source")).sendKeys("EC");
        waitFor("document.querySelector('#data-source ~ .autocomplete-dropdown.show .autocomplete-item')");
        assertThat(
                        eval(
                                "document.querySelector('#data-source ~ .autocomplete-dropdown .autocomplete-item').dataset.code"))
                .isEqualTo("ECB");

        driver.findElement(By.id("data-source")).sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
        assertThat(eval("document.getElementById('data-source').value")).isEqualTo("ECB");
        assertThat(eval("document.querySelector('#data-source ~ .autocomplete-dropdown').classList.contains('show')"))
                .isEqualTo(false);

        waitFor("document.getElementById('flows-list-data').children.length > 0");
        driver.findElement(By.id("data-flow")).click();
        waitFor("document.querySelector('#data-flow ~ .autocomplete-dropdown.show .autocomplete-item')");
        assertThat(eval("[...document.querySelectorAll('#data-flow ~ .autocomplete-dropdown .autocomplete-item')]"
                        + ".map(item => item.dataset.code)"))
                .asList()
                .contains("ECB,EXR,1.0");
    }

    @Test
    @DisplayName("Data chart toolbar switches to percent changes and log scale, and exports CSV")
    void testDataChartToolbar() {
        open("?tab=data&source=ECB&flow=EXR&key=M..EUR.SP00.A");
        js.executeScript("document.getElementById('data-lastn').value = '3'; getData();");
        waitFor("document.getElementById('data-chart').data && !document.getElementById('data-chart-toolbar').hidden");

        assertThat(eval("document.getElementById('data-series-visibility').hidden"))
                .isEqualTo(true);

        js.executeScript("document.getElementById('data-transform-change').click()");
        waitFor("document.getElementById('data-chart').layout.yaxis.title.text === '% change'");
        assertThat(eval("document.getElementById('data-chart').data[0].y[0]")).isNull();
        assertThat(eval("document.getElementById('data-chart').data[0].y[1]")).isNotNull();

        js.executeScript("document.getElementById('data-log-scale').click()");
        waitFor("document.getElementById('data-chart').layout.yaxis.type === 'log'");

        List<?> lines = (List<?>) eval("buildDataCsv(window.dataResponseData).split('\\r\\n')");
        assertThat(lines).hasSize(5);
        assertThat((String) lines.get(0)).startsWith("Period,").contains("CHF", "USD");
        assertThat((String) lines.get(1)).startsWith("2024-10,");
    }

    @Test
    @DisplayName("Data chart helpers compute percent changes and limit visible series")
    void testDataChartHelpers() {
        open("?tab=about");
        assertThat(eval("JSON.stringify(toPercentChanges([100, null, 110, 99, 0, 5]))"))
                .isEqualTo("[null,null,10,-10,-100,null]");
        assertThat(eval("getDisplayedTraces(Array.from({length: 12}, () => ({y: [1], hovertemplate: '%{y}'})), true)"
                        + ".filter(t => t.visible === 'legendonly').length"))
                .isEqualTo(2L);
        assertThat(eval(
                        "JSON.stringify(buildDataCsv({data: [{key: 'A', obs: [{period: '2024-01-01T00:00:00/P1M', value: 1.5}]},"
                                + " {key: 'B,C', obs: []}]}))"))
                .isEqualTo("\"Period,A,\\\"B,C\\\"\\r\\n2024-01,1.5,\\r\\n\"");
    }

    @Test
    @DisplayName("Compare series are kept in the local storage and in the URL")
    void testComparePersistence() {
        open("?tab=compare");
        assertThat(eval("document.getElementById('compare-btn').disabled")).isEqualTo(true);
        js.executeScript("document.getElementById('compare-source').value = 'ECB';"
                + "document.getElementById('compare-flow').value = 'EXR';"
                + "document.getElementById('compare-key').value = 'M.USD.EUR.SP00.A';"
                + "document.getElementById('compare-label').value = 'Dollar 50/50';"
                + "addSeriesToComparison();");

        assertThat(eval("new URL(location).searchParams.getAll('series')"))
                .asList()
                .containsExactly("ECB/EXR/M.USD.EUR.SP00.A//Dollar 50%2F50");

        open("?tab=compare");
        assertThat(eval("[...document.querySelectorAll('.compare-series-label')].map(e => e.textContent)"))
                .as("Series should be restored from the local storage")
                .asList()
                .containsExactly("Dollar 50/50");
        assertThat(eval("document.getElementById('compare-btn').disabled")).isEqualTo(false);

        open("?tab=compare&series=BBK/BBEX3/D.USD&series=ECB/EXR/M.CHF.EUR.SP00.A//Franc");
        assertThat(eval("[...document.querySelectorAll('.compare-series-label')].map(e => e.textContent)"))
                .as("Series from the URL should replace the stored ones")
                .asList()
                .containsExactly("BBK/BBEX3/D.USD", "Franc");

        js.executeScript("removeSeriesFromComparison(comparisonSeries[0].id)");
        assertThat(eval("new URL(location).searchParams.getAll('series')"))
                .asList()
                .containsExactly("ECB/EXR/M.CHF.EUR.SP00.A//Franc");
    }

    @Test
    @DisplayName("Dark mode applies to the page and to the charts")
    void testDarkMode() {
        open("?tab=about");
        js.executeScript("localStorage.setItem('sdmxdl-theme', 'dark')");
        open("?tab=data&source=ECB&flow=EXR&key=M..EUR.SP00.A");

        assertThat(eval("document.documentElement.dataset.bsTheme")).isEqualTo("dark");
        assertThat(eval("getComputedStyle(document.body).backgroundColor")).isNotEqualTo("rgb(255, 255, 255)");

        js.executeScript("getData()");
        waitFor("document.getElementById('data-chart').data");
        String darkText =
                (String) eval("getComputedStyle(document.documentElement).getPropertyValue('--bs-body-color').trim()");
        assertThat(eval("document.getElementById('data-chart').layout.font.color"))
                .isEqualTo(darkText);

        js.executeScript("toggleTheme()");
        assertThat(eval("document.documentElement.dataset.bsTheme")).isEqualTo("light");
        waitFor("document.getElementById('data-chart').layout.font.color !== '" + darkText + "'");

        List<String> errors = driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                .filter(entry -> entry.getLevel().intValue() >= Level.SEVERE.intValue())
                .map(LogEntry::getMessage)
                .filter(message -> !message.contains("favicon") && !message.contains("icons.duckduckgo.com"))
                .collect(Collectors.toList());
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Flow autocomplete is loaded when the source is filled from the URL")
    void testFlowAutocompleteFromUrl() {
        open("?tab=keybuilder&source=ECB");
        waitFor("document.getElementById('flows-list-kb').children.length > 0");

        assertThat(eval("[...document.getElementById('flows-list-kb').children].map(o => o.value)"))
                .asList()
                .contains("ECB,EXR,1.0");
    }

    @Test
    @DisplayName("Flow autocomplete is loaded on focus when the source is filled by a card action")
    void testFlowAutocompleteFromCardAction() {
        open("?tab=about");
        js.executeScript("useSourceInTab('BBK', 'meta')");
        driver.findElement(By.id("meta-flow")).click();
        waitFor("document.getElementById('flows-list-meta').children.length > 0");

        assertThat(eval("[...document.getElementById('flows-list-meta').children].map(o => o.value)"))
                .asList()
                .containsExactly("BBK,BBEX3,1.0");
    }

    @Test
    @DisplayName("Health badges are shown and kept when sources are reloaded")
    void testSourceHealth() {
        open("?tab=sources");
        waitFor("document.querySelector('.health-badge[data-source=\"ECB\"]')");

        driver.findElement(By.id("sources-health-btn")).click();
        waitFor("document.querySelector('.health-badge[data-source=\"ECB\"]').textContent === 'UNKNOWN'");
        assertThat((String) eval("document.querySelector('.health-badge[data-source=\"ECB\"]').title"))
                .contains("No monitor defined");

        js.executeScript("window.__oldCard = document.querySelector('.source-card');");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Get Sources')]")))
                .click();
        waitFor("document.querySelector('.source-card') !== window.__oldCard");

        assertThat(eval("document.querySelector('.health-badge[data-source=\"ECB\"]').textContent"))
                .isEqualTo("UNKNOWN");
        assertThat(eval("document.querySelector('.health-badge[data-source=\"ECB\"]').style.display"))
                .isEqualTo("inline-block");
    }

    @Test
    @DisplayName("Source card health button checks live access")
    void testSingleSourceHealth() {
        open("?tab=sources");
        waitFor("document.querySelector('.health-badge[data-source=\"BBK\"]')");

        js.executeScript("[...document.querySelectorAll('.source-card')]"
                + ".find(card => card.querySelector('.source-id').textContent === 'BBK')"
                + ".querySelector('button[title^=\"Check monitor\"]').click()");
        waitFor("document.querySelector('.health-badge[data-source=\"BBK\"]').textContent === 'OK'");

        assertThat((String) eval("document.querySelector('.health-badge[data-source=\"BBK\"]').title"))
                .contains("Access: accessible");
    }

    @Test
    @DisplayName("Source cards escape identifiers and names")
    void testSourceCardEscaping() {
        open("?tab=about");

        String id = "X'\"<img src=x onerror=window.__xss=1>";
        js.executeScript(
                "switchTab('sources');"
                        + "renderPrettySources([{id: arguments[0], names: {en: '<b>bold</b>'}, driver: 'd', endpoint: 'e'}]);",
                id);

        assertThat(eval("document.querySelector('.source-id').textContent")).isEqualTo(id);
        assertThat(eval("document.querySelector('.source-name').textContent")).isEqualTo("<b>bold</b>");
        assertThat(eval("window.__xss")).isNull();

        js.executeScript("document.querySelector('.source-action-btn[title^=\"Use in compare\"]').click()");
        assertThat(eval("document.getElementById('compare-source').value")).isEqualTo(id);
    }
}
