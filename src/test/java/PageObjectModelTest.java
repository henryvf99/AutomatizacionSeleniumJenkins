import io.qameta.allure.testng.Tag;
import io.qameta.allure.testng.Tags;
import pages.*;
import utils.DriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.qameta.allure.*;

@Epic("Checkout flow on Saucedemo")
public class PageObjectModelTest {

    private static final String SITE =
            "https://www.saucedemo.com/";

    private WebDriver driver;

    private pages.LoginPage loginPage;
    private ProductsPage productsPage;
    private ProductPage productPage;
    private CartPage cartPage;
    private CheckoutPage checkoutPage;
    private FinalCheckoutPage finalCheckoutPage;
    private pages.OrderCompletionPage orderCompletionPage;


    @BeforeClass
    public void setUp() {
        driver = DriverFactory.createDriver(DriverFactory.BrowserType.CHROME);

        loginPage = new LoginPage(driver);
        productsPage = new ProductsPage(driver);
        productPage = new ProductPage(driver);
        cartPage = new CartPage(driver);
        checkoutPage = new CheckoutPage(driver);
        finalCheckoutPage = new FinalCheckoutPage(driver);
        orderCompletionPage = new OrderCompletionPage(driver);

        driver.get(SITE);
    }

    private static void delay() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Feature("Login flow")
    @Story("Login")
    @Description("Test para verificar la funcionalidad del logn")
    @Link("https://www.saucedemo.com/")
    @Tag("LOGIN")
    @Owner("Henry VF")
    @Flaky
    @Severity(SeverityLevel.BLOCKER)
    @Test
    public void testLogin() {
        loginPage.login("standard_user", "secret_sauce");

        Assert.assertTrue(productsPage.isPageOpened(), "Login failed!");

        delay();
    }

    @Feature("Add products flow")
    @Story("Add products")
    @Description("Test para agregar un producto al carro")
    @Severity(SeverityLevel.NORMAL)
    @Link("https://www.saucedemo.com/inventory.html")
    @Tags({@Tag("ADD PRODUCT"), @Tag("PRODUCTS")})
    @Owner("Henry VF")
    @Flaky
    @Test(dependsOnMethods = "testLogin")
    public void testAddBackpackToCart() {
        productsPage.navigateToProductPage("Sauce Labs Backpack");

        productPage.addToCart();

        Assert.assertEquals(productPage.getButtonText(), "Remove",
                "Button text did not change");

        delay();

        driver.navigate().back();
    }

    @Feature("Add products flow")
    @Story("Add products")
    @Description("Test para agregar un producto al carro")
    @Severity(SeverityLevel.NORMAL)
    @Test(dependsOnMethods = "testAddBackpackToCart")
    public void testAddFleeceJacketToCart() {
        productsPage.navigateToProductPage("Sauce Labs Fleece Jacket");

        productPage.addToCart();

        Assert.assertEquals(productPage.getButtonText(), "Remove",
                "Button text did not change");

        delay();

        driver.navigate().back();
    }

    @Feature("View cart flow")
    @Story("View cart")
    @Description("Test para verificar productos del carro")
    @Severity(SeverityLevel.CRITICAL)
    @Test(dependsOnMethods = {"testAddBackpackToCart", "testAddFleeceJacketToCart"})
    public void testCart() {
        productsPage.navigateToCart();

        Assert.assertTrue(cartPage.isPageOpened(), "Cart page not loaded");
        Assert.assertEquals(cartPage.getCartItemCount(), "2", "Incorrect number of items in the cart");
        Assert.assertEquals(cartPage.getContinueButtonText(), "Checkout",
                "Incorrect button text on the cart page");

        Assert.assertTrue(cartPage.productInCart("Sauce Labs Backpack"));
        Assert.assertTrue(cartPage.productInCart("Sauce Labs Fleece Jacket"));

        delay();
    }

    @Feature("Checkout flow")
    @Story("Checkout")
    @Description("Test para verificar el Checkout")
    @Severity(SeverityLevel.MINOR)
    @Step("Verificar checkout")
    @Test(dependsOnMethods = "testCart")
    public void testCheckout() {

        Allure.step("Navegar al checkout e ingresar detalles", () -> {

            cartPage.continueCheckout();

            Assert.assertTrue(checkoutPage.isPageOpened(), "Checkout page not loaded");
            checkoutPage.enterDetails("Nora", "Jones", "12345");

        });

        Allure.step("Verificar detalles ingresados", () -> {

            Assert.assertEquals(checkoutPage.getFirstNameFieldValue(), "Nora",
                    "First name field value is incorrect");
            Assert.assertEquals(checkoutPage.getLastNameFieldValue(), "Jones",
                    "Last name field value is incorrect");
            Assert.assertEquals(checkoutPage.getZipCodeFieldValue(), "12345",
                    "Zip code field value is incorrect");

        });

        delay();
    }


    @Feature("Checkout flow")
    @Story("Checkout")
    @Description("Test para verificar el final Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Test(dependsOnMethods = "testCheckout")
    public void testFinalCheckout() {
        checkoutPage.continueCheckout();

        Assert.assertTrue(finalCheckoutPage.isPageOpened(),
                "Checkout page not loaded");
        Assert.assertEquals(finalCheckoutPage.getPaymentInfoValue(),
                "SauceCard #31337");
        Assert.assertEquals(finalCheckoutPage.getShippingInfoValue(), "" +
                "Free Pony Express Delivery!");
        Assert.assertEquals(finalCheckoutPage.getTotalLabel(),
                "Total: $86.38");

        delay();
    }

    @Feature("Checkout flow")
    @Story("Order completion")
    @Description("Test para verificar la orden")
    @Severity(SeverityLevel.TRIVIAL)
    @Test(dependsOnMethods = "testFinalCheckout")
    public void testOrderCompletion() {
        finalCheckoutPage.finishCheckout();

        Assert.assertEquals(orderCompletionPage.getHeaderText(), "Thank you for your order!");
        Assert.assertEquals(orderCompletionPage.getBodyText(),
                "Your order has been dispatched, and will arrive just as fast as the pony can get there!");

        delay();
    }

    @Test(dependsOnMethods = "testOrderCompletion")
    public void testDummy(){
        //prueba
        Assert.assertTrue(true);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
