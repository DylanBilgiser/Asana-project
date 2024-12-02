
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

public class AsanaTestSuite {
    private static final String LOGIN_URL = "https://app.asana.com/-/login";
    private static final String EMAIL = "ben+pose@workwithloop.com";
    private static final String PASSWORD = "Password123";

    public static void main(String[] args) throws Exception {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        BrowserContext context = browser.newContext();
        Page page = context.newPage();


        // Load test cases from JSON
        String jsonContent = new String(Files.readAllBytes(Paths.get("C:\\Users\\bilgiser\\IdeaProjects\\login_Pro_Asana\\src\\test\\java\\zzz.json")));//zzz,json
        JSONArray testCases = new JSONArray(jsonContent);

        // Login once for all test cases
        loginToAsana(page);

        // Execute test cases
        for (int i = 0; i < testCases.length(); i++) {
            JSONObject testCase = testCases.getJSONObject(i);
            runTestCase(page, testCase);
        }

        browser.close();
        playwright.close();
    }

    private static void loginToAsana(Page page) {
        page.navigate(LOGIN_URL);
        page.fill("input[name='e']", EMAIL);
        page.click("xpath=//div[contains(@class, 'LoginButton') and contains(text(), 'Continue')]");
        page.fill("input[name='p']", PASSWORD);
        page.click("xpath=//div[contains(@class, 'LoginButton') and contains(text(), 'Log in')]");
       // page.waitForSelector("#lui_838");

        System.out.println("logged in !!");
    }

    private static void runTestCase(Page page, JSONObject testCase) {
        String navigationPath = testCase.getString("navigationPath");
        String taskName = testCase.getString("taskName");
        String columnName = testCase.getString("columnName");
        JSONArray expectedTags = testCase.getJSONArray("tags");

        // Navigate to the specified project or section
        page.click("text=" + navigationPath);
     //   page.waitForSelector("[data-task-id='1207728107119660']");
        System.out.println("done!!");

        // Verify task location
        String taskSelector = String.format("//span[@class='TypographyPresentation TypographyPresentation--medium BoardCard-taskName' and text()='Draft project brief']", taskName);
        String columnSelector = String.format("css=[data-column-name='%s']", columnName);
        boolean isTaskInColumn = page.locator(taskSelector).isVisible() &&
                page.locator(columnSelector).isVisible();

        if (isTaskInColumn) {
            System.out.printf("Task '%s' is in the column '%s'.%n", taskName, columnName);
        } else {
            System.err.printf("Task '%s' not found in column '%s'.%n", taskName, columnName);
        }

        // Verify tags
        for (int j = 0; j < expectedTags.length(); j++) {
            String tag = expectedTags.getString(j);
            boolean isTagPresent = page.locator(String.format("css=[data-tag-name='%s']", tag)).isVisible();
            if (isTagPresent) {
                System.out.printf("Tag '%s' is present for task '%s'.%n", tag, taskName);
            } else {
                System.err.printf("Tag '%s' is missing for task '%s'.%n", tag, taskName);
            }
        }
    }
}
