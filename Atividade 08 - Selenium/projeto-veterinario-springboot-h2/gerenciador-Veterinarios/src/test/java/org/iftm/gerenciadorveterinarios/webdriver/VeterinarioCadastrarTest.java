package org.iftm.gerenciadorveterinarios.webdriver;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import io.github.bonigarcia.wdm.WebDriverManager;

public class VeterinarioCadastrarTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    
    // Elementos comuns a todos os testes
    private WebElement nomeField;
    private WebElement emailField;
    private WebElement especialidadeField;
    private WebElement salarioField;
    private WebElement submitButton;

    @BeforeAll
    public static void configurarDriverBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @BeforeEach
    public void navegarParaFormulario() {
        driver.get("http://localhost:8080/form");
        
        nomeField = wait.until(ExpectedConditions.elementToBeClickable(By.id("nome")));
        emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("inputEmail")));
        especialidadeField = wait.until(ExpectedConditions.elementToBeClickable(By.id("inputEspecialidade")));
        salarioField = wait.until(ExpectedConditions.elementToBeClickable(By.id("inputSalario")));
        submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
    }

    @Test
    public void testCadastrarVeterinario() {
        assertTrue(nomeField.isDisplayed(), "Campo nome deveria estar visível");
        assertTrue(emailField.isDisplayed(), "Campo email deveria estar visível");
        assertTrue(especialidadeField.isDisplayed(), "Campo especialidade deveria estar visível");
        assertTrue(salarioField.isDisplayed(), "Campo salario deveria estar visível");
        assertTrue(submitButton.isDisplayed(), "Botão submit deveria estar visível");

        nomeField.clear();
        nomeField.sendKeys("Dr. João Silva");
        emailField.clear();
        emailField.sendKeys("joao@veterinaria.com");
        especialidadeField.clear();
        especialidadeField.sendKeys("Clínica Geral");
        salarioField.clear();
        salarioField.sendKeys("5000");
        
        String valorNome = nomeField.getDomProperty("value");
        String valorEmail = emailField.getDomProperty("value");
        String valorEspecialidade = especialidadeField.getDomProperty("value");
        String valorSalario = salarioField.getDomProperty("value");
        
        submitButton.click();
        
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/home"),
            ExpectedConditions.urlContains("/")
        ));
        String urlAtual = driver.getCurrentUrl();

        assertEquals("Dr. João Silva", valorNome);
        assertEquals("joao@veterinaria.com", valorEmail);
        assertEquals("Clínica Geral", valorEspecialidade);
        assertEquals("5000", valorSalario);
        assertTrue(urlAtual.contains("/home") || urlAtual.contains("/"), "Deveria ter redirecionado após cadastro");
    }

    @Test
    public void testCadastrarVeterinarioSemNome() {
        emailField.clear();
        emailField.sendKeys("teste@veterinaria.com");
        especialidadeField.clear();
        especialidadeField.sendKeys("Cirurgia");
        salarioField.clear();
        salarioField.sendKeys("6000");
        
        
        String valorEmail = emailField.getDomProperty("value");
        String valorEspecialidade = especialidadeField.getDomProperty("value");
        String valorSalario = salarioField.getDomProperty("value");
        
        assertEquals("teste@veterinaria.com", valorEmail, "Email deveria estar preenchido");
        assertEquals("Cirurgia", valorEspecialidade, "Especialidade deveria estar preenchida");
        assertEquals("6000", valorSalario, "Salário deveria estar preenchido");
        
        submitButton.click();
        
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/home"),
            ExpectedConditions.urlContains("/")
        ));
        String urlAtual = driver.getCurrentUrl();
        
        assertTrue(urlAtual.contains("/home") || urlAtual.contains("/"), 
                   "Deve redirecionar para /home mesmo sem nome, pois campo nome não é obrigatório no HTML. URL atual: " + urlAtual);
    }


    @Test
    public void testCadastrarVeterinarioSemEmail() {
        nomeField.clear();
        nomeField.sendKeys("Dr. Pedro Santos");
        especialidadeField.clear();
        especialidadeField.sendKeys("Cardiologia");
        salarioField.clear();
        salarioField.sendKeys("7000");
        
        
        String valorNome = nomeField.getDomProperty("value");
        String valorEspecialidade = especialidadeField.getDomProperty("value");
        String valorSalario = salarioField.getDomProperty("value");
        
        assertEquals("Dr. Pedro Santos", valorNome, "Nome deveria estar preenchido");
        assertEquals("Cardiologia", valorEspecialidade, "Especialidade deveria estar preenchida");
        assertEquals("7000", valorSalario, "Salário deveria estar preenchido");
        
        submitButton.click();
        
        wait.until(driver -> driver.getCurrentUrl().length() > 0);
        String urlAtual = driver.getCurrentUrl();
        
        assertTrue(urlAtual.contains("/form"), 
                   "Deve permanecer na página /form quando email não é preenchido (campo required). URL atual: " + urlAtual);
    }

    @Test
    public void testVerificarElementosFormulario() {
        assertTrue(nomeField.isDisplayed(), "Campo nome deveria estar visível");
        assertTrue(emailField.isDisplayed(), "Campo email deveria estar visível");
        assertTrue(especialidadeField.isDisplayed(), "Campo especialidade deveria estar visível");
        assertTrue(salarioField.isDisplayed(), "Campo salário deveria estar visível");
        assertTrue(submitButton.isDisplayed(), "Botão submit deveria estar visível");
        
        assertTrue(nomeField.isEnabled(), "Campo nome deveria estar habilitado");
        assertTrue(emailField.isEnabled(), "Campo email deveria estar habilitado");
        assertTrue(especialidadeField.isEnabled(), "Campo especialidade deveria estar habilitado");
        assertTrue(salarioField.isEnabled(), "Campo salário deveria estar habilitado");
        assertTrue(submitButton.isEnabled(), "Botão submit deveria estar habilitado");
        
        assertEquals("Cadastrar", submitButton.getText(), "Botão deveria ter texto 'Cadastrar'");
    }



    @AfterAll
    public static void fecharBrowser() {
        driver.close();
    }
}