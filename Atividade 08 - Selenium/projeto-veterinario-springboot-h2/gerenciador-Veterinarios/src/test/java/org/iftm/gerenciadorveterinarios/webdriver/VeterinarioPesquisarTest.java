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

public class VeterinarioPesquisarTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    
    // Elementos comuns a todos os testes
    private WebElement nomeField;
    private WebElement consultarButton;

    @BeforeAll
    public static void configurarDriverBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @BeforeEach
    public void navegarParaFormularioPesquisa() {
        driver.get("http://localhost:8080/find");
        
        nomeField = wait.until(ExpectedConditions.elementToBeClickable(By.id("nome")));
        consultarButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
    }

    @Test
    public void testPesquisarVeterinarioPorNome() {
        // verificar se campos estão visíveis
        assertTrue(nomeField.isDisplayed(), "Campo nome deveria estar visível");
        assertTrue(consultarButton.isDisplayed(), "Botão consultar deveria estar visível");

        // preencher campo de pesquisa
        nomeField.clear();
        nomeField.sendKeys("João");
        
        // verificar valor preenchido
        String valorNome = nomeField.getDomProperty("value");
        assertEquals("João", valorNome, "Nome deveria estar preenchido");
        
        // submeter pesquisa
        consultarButton.click();
        
        // aguardar redirecionamento para home com parâmetro
        wait.until(ExpectedConditions.urlContains("/home"));
        String urlAtual = driver.getCurrentUrl();
        
        // verificar se foi redirecionado para home com parâmetro de pesquisa
        assertTrue(urlAtual.contains("/home"), "Deveria ter redirecionado para /home");
        assertTrue(urlAtual.contains("nome=Jo"), "URL deveria conter parâmetro de pesquisa");
    }

    @Test
    public void testPesquisarVeterinarioSemNome() {
        // verificar que campo está vazio
        String valorNome = nomeField.getDomProperty("value");
        assertEquals("", valorNome, "Campo nome deveria estar vazio inicialmente");
        
        // submeter pesquisa sem preencher nome
        consultarButton.click();
        
        // aguardar redirecionamento
        wait.until(ExpectedConditions.urlContains("/home"));
        String urlAtual = driver.getCurrentUrl();
        
        // verificar redirecionamento (pesquisa vazia deve listar todos)
        assertTrue(urlAtual.contains("/home"), "Deveria ter redirecionado para /home");
    }

    @Test
    public void testVerificarElementosFormularioPesquisa() {
        // verificar se elementos estão visíveis e habilitados
        assertTrue(nomeField.isDisplayed(), "Campo nome deveria estar visível");
        assertTrue(nomeField.isEnabled(), "Campo nome deveria estar habilitado");
        assertTrue(consultarButton.isDisplayed(), "Botão consultar deveria estar visível");
        assertTrue(consultarButton.isEnabled(), "Botão consultar deveria estar habilitado");
        
        // verificar texto do botão
        assertEquals("Consultar", consultarButton.getText(), "Botão deveria ter texto 'Consultar'");
    }

    @Test
    public void testPesquisarVeterinarioComNomeCompleto() {
        // preencher com nome mais específico
        nomeField.clear();
        nomeField.sendKeys("Dr. João Silva");
        
        String valorNome = nomeField.getDomProperty("value");
        assertEquals("Dr. João Silva", valorNome, "Nome completo deveria estar preenchido");
        
        // submeter pesquisa
        consultarButton.click();
        
        // aguardar redirecionamento
        wait.until(ExpectedConditions.urlContains("/home"));
        String urlAtual = driver.getCurrentUrl();
        
        // verificar se pesquisa foi realizada
        assertTrue(urlAtual.contains("/home"), "Deveria ter redirecionado para /home");
        assertTrue(urlAtual.contains("nome="), "URL deveria conter parâmetro de pesquisa");
    }

    @AfterAll
    public static void fecharBrowser() {
        driver.close();
    }
}
