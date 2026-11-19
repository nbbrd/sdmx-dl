package sdmxdl.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import java.net.URL;
import java.time.Duration;
import java.util.List;
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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Tests search.html (SDMX-DL Search) against the in-memory manager of {@link MockedSdmxWebManagerProducer}.
 */
@QuarkusTest
class SearchHtmlTest {

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
                "--force-prefers-reduced-motion",
                "--use-mock-keychain",
                "--password-store=basic",
                "--no-first-run",
                "--disable-background-networking",
                "--disable-component-update",
                "--disable-sync",
                "--host-resolver-rules=MAP * ~NOTFOUND, EXCLUDE " + testUrl.getHost());
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
        driver.get(testUrl + "search.html" + query);
        waitFor("typeof state === 'object' && state.sources.length > 0");
    }

    private Object eval(String expression) {
        return js.executeScript("return " + expression);
    }

    private String text(String expression) {
        return String.valueOf(eval(expression));
    }

    private void waitFor(String condition) {
        wait.until(d -> Boolean.TRUE.equals(js.executeScript("return !!(" + condition + ")")));
    }

    private void search(String text) {
        WebElement input = driver.findElement(By.id("q"));
        input.clear();
        input.sendKeys(text);
        js.executeScript("runSearch()");
        waitFor("document.querySelector('#results .result')");
    }

    private void openExchangeRates() {
        open("?source=ECB&flow=EXR");
        waitFor("document.querySelectorAll('#dimensions [data-dimension]').length === 5");
        waitFor("document.getElementById('match-count').textContent === '2'");
    }

    @Test
    @DisplayName("Landing screen loads its resources and sources without errors")
    void testLanding() {
        open("");

        assertThat(eval("document.body.classList.contains('landing')")).isEqualTo(true);
        assertThat(eval("document.getElementById('source-count').textContent")).isEqualTo("3 sources");
        assertThat(eval("[...document.querySelectorAll('#scope option')].map(o => o.value).join(',')"))
                .isEqualTo(",BBK,ECB,ESTAT");
        assertThat(eval("getComputedStyle(document.getElementById('crumbbar')).display"))
                .isEqualTo("none");

        List<String> errors = driver.manage().logs().get(LogType.BROWSER).getAll().stream()
                .filter(entry -> entry.getLevel().intValue() >= Level.SEVERE.intValue())
                .map(LogEntry::getMessage)
                .filter(message -> !message.contains("favicon"))
                .collect(Collectors.toList());
        assertThat(errors).isEmpty();
    }

    @Test
    @DisplayName("Search lists datasets of all sources with facets, then filters by source")
    void testSearchResults() {
        open("");
        search("exchange");

        assertThat(eval("document.body.classList.contains('landing')")).isEqualTo(false);
        assertThat(eval("window.location.search")).isEqualTo("?q=exchange");
        assertThat(eval("document.querySelectorAll('#results .result').length"))
                .isEqualTo(eval("state.search.items.length"));
        assertThat(text("state.search.items.map(e => e.source + '/' + e.flow.ref).join(' ')"))
                .contains("ECB/ECB,EXR,1.0")
                .contains("BBK/BBK,BBEX3,1.0");
        assertThat(eval("document.querySelector('#results mark').textContent.toLowerCase()"))
                .isEqualTo("exchange");
        assertThat(eval("document.querySelectorAll('#facet-source .form-check').length"))
                .isEqualTo(eval("new Set(state.search.items.map(e => e.source)).size"));

        js.executeScript("toggleSourceFacet('BBK', true)");
        assertThat(eval("document.querySelectorAll('#results .result').length")).isEqualTo(1L);
        assertThat(eval("document.querySelector('#results .source-tile').textContent"))
                .isEqualTo("BBK");

        assertThat(eval("JSON.parse(localStorage.getItem('sdmxdl-search-recents'))[0]"))
                .isEqualTo("exchange");
    }

    @Test
    @DisplayName("Slow sources are searched again in the background and their results are offered without reordering")
    void testSlowSources() {
        open("");
        stubSlowSource("new Promise(resolve => window.releaseSlow = resolve).then(() => real(url, options))");
        search("exchange");

        assertThat(eval("state.search.items.some(e => e.source === 'BBK')")).isEqualTo(false);
        assertThat(eval("flowTimeouts[0]")).isEqualTo("3000");
        waitFor("flowTimeouts.length === 2");
        assertThat(text("document.getElementById('search-status').textContent"))
                .contains("1 slow source")
                .contains("BBK");
        assertThat(eval("document.getElementById('search-progress-bar').style.width"))
                .isNotEqualTo("");
        assertThat(eval("document.getElementById('failures').textContent")).isEqualTo("");

        js.executeScript("releaseSlow()");
        waitFor("document.querySelector('#search-status .alert')");
        assertThat(text("document.getElementById('search-status').textContent")).contains("1 more dataset from BBK");
        assertThat(eval("state.search.items.some(e => e.source === 'BBK')")).isEqualTo(false);

        js.executeScript("applySearchUpdate()");
        assertThat(eval("state.search.items.some(e => e.source === 'BBK')")).isEqualTo(true);
        assertThat(eval("document.querySelectorAll('#results .result').length"))
                .isEqualTo(eval("state.search.items.length"));
        assertThat(eval("document.getElementById('search-status').textContent.trim()"))
                .isEqualTo("");
    }

    @Test
    @DisplayName("Stopping a search keeps the results of the fast sources and reports the slow ones")
    void testStopSearch() {
        open("");
        stubSlowSource(
                "new Promise((resolve, reject) => options.signal.addEventListener('abort', () => reject(new DOMException('Stopped', 'AbortError'))))");
        search("exchange");
        waitFor("flowTimeouts.length === 2");

        js.executeScript("stopSearch()");
        waitFor("!document.querySelector('#search-status .search-progress')");
        assertThat(eval("document.querySelectorAll('#results .result').length"))
                .isEqualTo(eval("state.search.items.length"));
        assertThat(eval("state.search.items.length")).isNotEqualTo(0L);
        assertThat(text("document.getElementById('failures').textContent"))
                .contains("1 source could not be searched")
                .contains("BBK");
    }

    /**
     * Makes BBK time out on the first search pass, then answers the second pass with the given promise.
     */
    private void stubSlowSource(String secondPass) {
        js.executeScript("""
                window.flowTimeouts = [];
                const real = getJSON;
                window.getJSON = async (url, options) => {
                    if (!url.includes('/flows?')) return real(url, options);
                    flowTimeouts.push(new URL(url).searchParams.get('timeout'));
                    if (flowTimeouts.length > 1) return %s;
                    const result = await real(url, options);
                    const kept = result.items.map((entry, index) => index).filter(index => result.items[index].source !== 'BBK');
                    return {
                        items: kept.map(index => result.items[index]),
                        scores: kept.map(index => result.scores[index]),
                        total: kept.length,
                        failures: [{ source: 'BBK', reason: 'UPSTREAM_TIMEOUT', message: 'No answer within PT3S' }]
                    };
                };""".replace("%s", secondPass));
    }

    @Test
    @DisplayName("Peek structure loads the dimensions of a result on demand")
    void testPeekStructure() {
        open("?q=exchange&scope=ECB");
        waitFor("document.querySelector('#results .result')");

        assertThat(eval("document.querySelectorAll('#results .result').length")).isEqualTo(1L);
        js.executeScript("peekStructure(0)");
        waitFor("document.querySelectorAll('#peek-0 .badge').length === 5");
        assertThat(eval("document.querySelector('#peek-0 .badge').textContent")).isEqualTo("Frequency");
    }

    @Test
    @DisplayName("Suggestions offer to open a typed dataset reference")
    void testSuggestions() {
        open("");
        WebElement input = driver.findElement(By.id("q"));
        input.sendKeys("ECB/EXR/M.CHF.EUR.SP00.A");
        waitFor("!document.getElementById('suggest').classList.contains('d-none')");
        assertThat(text("document.querySelector('#suggest .list-group-item-action').textContent"))
                .contains("ECB/EXR");

        input.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
        waitFor("state.view === 'dataset'");
        waitFor("document.getElementById('match-count').textContent === '1'");
        assertThat(eval("datasetKey()")).isEqualTo("M.CHF.EUR.SP00.A");
    }

    @Test
    @DisplayName("Opening a result shows the dataset with dimensions, availability and series")
    void testOpenResult() {
        open("");
        search("exchange rates");
        js.executeScript("openResult(state.search.items.findIndex(e => e.source === 'ECB'))");
        waitFor("document.getElementById('match-count').textContent === '2'");

        assertThat(eval("document.getElementById('dataset-name').textContent")).isEqualTo("Exchange Rates");
        assertThat(eval("document.querySelectorAll('#series tr.series-row').length"))
                .isEqualTo(2L);
        assertThat(eval("document.querySelector('#series tr.series-row .fw-semibold').textContent"))
                .isEqualTo("Swiss franc/Euro");
        assertThat(eval("new URLSearchParams(window.location.search).get('flow')"))
                .isEqualTo("ECB,EXR,1.0");

        driver.navigate().back();
        waitFor("state.view === 'search'");
        waitFor("document.querySelector('#results .result')");
    }

    @Test
    @DisplayName("Picking codes narrows the key, the availability and the series")
    void testNarrowDown() {
        openExchangeRates();

        js.executeScript("toggleDimension(1)");
        waitFor("document.querySelectorAll('#code-list-1 input[disabled]').length === 10");
        js.executeScript("pickCode(1, 'CHF', true)");
        waitFor("document.getElementById('match-count').textContent === '1'");

        assertThat(eval("datasetKey()")).isEqualTo(".CHF...");
        assertThat(eval("document.getElementById('total-count').textContent")).isEqualTo("2");
        assertThat(eval("new URLSearchParams(window.location.search).get('key')"))
                .isEqualTo(".CHF...");
        assertThat(eval("document.querySelectorAll('#keybar .key-part:not(.wild)').length"))
                .isEqualTo(1L);

        js.executeScript("findCodes('yen')");
        assertThat(eval("document.querySelectorAll('#code-hits a').length")).isEqualTo(2L);

        js.executeScript("resetDimensions()");
        waitFor("document.getElementById('match-count').textContent === '2'");
        assertThat(eval("datasetKey()")).isEqualTo("....");
    }

    @Test
    @DisplayName("Series drawer shows observations, dimensions, a chart and a script")
    void testSeriesDrawer() {
        open("?source=ECB&flow=EXR&key=M.CHF.EUR.SP00.A&series=M.CHF.EUR.SP00.A");
        waitFor("document.getElementById('drawer').classList.contains('open')");
        waitFor("document.querySelectorAll('#obs-body tr').length === 312");

        assertThat(eval("document.getElementById('series-title').textContent")).isEqualTo("Swiss franc/Euro");
        assertThat(eval("document.querySelector('#obs-body td').textContent")).isEqualTo("2024-12");
        assertThat(eval("document.querySelector('#obs-body td:last-child').textContent"))
                .isEqualTo("");
        assertThat(eval("document.querySelectorAll('#series-kpis .col-6').length"))
                .isEqualTo(4L);
        assertThat(text("document.getElementById('dimension-list').textContent"))
                .contains("Swiss franc");
        waitFor("document.querySelector('#series-chart.js-plotly-plot')");

        js.executeScript("showSeriesTab('use')");
        waitFor("document.getElementById('snippet').textContent.includes('M.CHF.EUR.SP00.A')");
        assertThat(eval("document.querySelector('#script-languages .active').textContent"))
                .isEqualTo("python");

        js.executeScript("closeSeries()");
        assertThat(eval("document.getElementById('drawer').classList.contains('open')"))
                .isEqualTo(false);
        assertThat(eval("new URLSearchParams(window.location.search).get('series')"))
                .isNull();
        js.executeScript("reopenSeries()");
        waitFor("document.getElementById('drawer').classList.contains('open')");
    }

    @Test
    @DisplayName("Series can be added to the comparison, which persists across reloads")
    void testCompare() {
        open("?source=ECB&flow=EXR&series=M.CHF.EUR.SP00.A");
        waitFor("state.series && state.series.obs");
        js.executeScript("toggleCompare()");
        js.executeScript("openSeries('M.USD.EUR.SP00.A')");
        waitFor("state.series && state.series.obs");
        js.executeScript("toggleCompare()");
        assertThat(eval("document.getElementById('compare-badge').textContent")).isEqualTo("2");

        js.executeScript("showCompare()");
        waitFor("document.querySelector('#compare-chart.js-plotly-plot')");
        assertThat(eval("document.querySelectorAll('#compare-list .list-group-item').length"))
                .isEqualTo(2L);
        assertThat(eval("window.location.search")).isEqualTo("?view=compare");

        driver.navigate().refresh();
        waitFor("document.querySelectorAll('#compare-list .list-group-item').length === 2");
        js.executeScript("removeCompare(0)");
        assertThat(eval("document.getElementById('compare-badge').textContent")).isEqualTo("1");
    }

    @Test
    @DisplayName("Favorite datasets and recent searches show up on the landing screen")
    void testFavoritesAndRecents() {
        openExchangeRates();
        js.executeScript("toggleFavorite()");
        assertThat(text("document.getElementById('favorite-icon').className")).contains("bi-star-fill");

        open("?q=unemployment");
        waitFor("document.querySelector('#results .result')");
        js.executeScript("goHome()");

        assertThat(eval("document.body.classList.contains('landing')")).isEqualTo(true);
        assertThat(text("document.querySelector('#favorites .item').textContent"))
                .contains("Exchange Rates");
        assertThat(text("document.querySelector('#recents .item').textContent")).contains("unemployment");

        js.executeScript("openFavorite(0)");
        waitFor("state.view === 'dataset' && document.getElementById('match-count').textContent === '2'");
    }
}
