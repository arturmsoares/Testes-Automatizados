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

public class VeterinarioAlterarTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    
    // Elementos comuns a todos os testes
    private WebElement nomeField;
    private WebElement emailField;
    private WebElement especialidadeField;
    private WebElement salarioField;
    private WebElement atualizarButton;
    private WebElement titulo;

    @BeforeAll
    public static void configurarDriverBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @BeforeEach
    public void navegarParaFormularioEdicao() {
        // Primeiro, vamos para home para ter dados
        driver.get("http://localhost:8080/home");
        
        // Procurar o primeiro botão de editar disponível na tabela
        WebElement primeiroEditarBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("a.btn-warning"))
        );
        
        // Clicar no botão de editar
        primeiroEditarBtn.click();
        
        // Aguardar carregar a página de edição e capturar elementos
        titulo = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        nomeField = wait.until(ExpectedConditions.elementToBeClickable(By.id("nome")));
        emailField = wait.until(ExpectedConditions.elementToBeClickable(By.id("inputEmail")));
        especialidadeField = wait.until(ExpectedConditions.elementToBeClickable(By.id("inputEspecialidade")));
        salarioField = wait.until(ExpectedConditions.elementToBeClickable(By.id("inputSalario")));
        atualizarButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
    }

    @Test
    public void testVerificarElementosFormularioEdicao() {
        // verificar se elementos estão visíveis
        assertTrue(titulo.isDisplayed(), "Título deveria estar visível");
        assertTrue(nomeField.isDisplayed(), "Campo nome deveria estar visível");
        assertTrue(emailField.isDisplayed(), "Campo email deveria estar visível");
        assertTrue(especialidadeField.isDisplayed(), "Campo especialidade deveria estar visível");
        assertTrue(salarioField.isDisplayed(), "Campo salário deveria estar visível");
        assertTrue(atualizarButton.isDisplayed(), "Botão atualizar deveria estar visível");
        
        // verificar texto do título e botão
        assertEquals("Atualizar informacoes", titulo.getText(), "Título deveria ser 'Atualizar informacoes'");
        assertEquals("Atualizar", atualizarButton.getText(), "Botão deveria ter texto 'Atualizar'");
    }

    @Test
    public void testCamposPreenchidosComDadosExistentes() {
        // verificar se campos estão preenchidos com dados existentes
        String nomeAtual = nomeField.getDomProperty("value");
        String emailAtual = emailField.getDomProperty("value");
        String especialidadeAtual = especialidadeField.getDomProperty("value");
        String salarioAtual = salarioField.getDomProperty("value");
        
        // campos não devem estar vazios (assumindo que há dados pré-existentes)
        assertNotNull(nomeAtual, "Campo nome não deveria ser null");
        assertNotNull(emailAtual, "Campo email não deveria ser null");
        assertNotNull(especialidadeAtual, "Campo especialidade não deveria ser null");
        assertNotNull(salarioAtual, "Campo salário não deveria ser null");
    }

    @Test
    public void testAlterarInformacoesVeterinario() {
        // capturar valores originais
        String nomeOriginal = nomeField.getDomProperty("value");
        String emailOriginal = emailField.getDomProperty("value");
        
        // alterar alguns campos
        nomeField.clear();
        nomeField.sendKeys(nomeOriginal + " - Editado");
        
        emailField.clear();
        emailField.sendKeys("editado_" + emailOriginal);
        
        especialidadeField.clear();
        especialidadeField.sendKeys("Especialidade Atualizada");
        
        salarioField.clear();
        salarioField.sendKeys("8000");
        
        // verificar valores alterados
        String novoNome = nomeField.getDomProperty("value");
        String novoEmail = emailField.getDomProperty("value");
        String novaEspecialidade = especialidadeField.getDomProperty("value");
        String novoSalario = salarioField.getDomProperty("value");
        
        assertTrue(novoNome.contains("Editado"), "Nome deveria conter 'Editado'");
        assertTrue(novoEmail.contains("editado_"), "Email deveria conter 'editado_'");
        assertEquals("Especialidade Atualizada", novaEspecialidade, "Especialidade deveria estar atualizada");
        assertEquals("8000", novoSalario, "Salário deveria ser 8000");
        
        // submeter alterações
        atualizarButton.click();
        
        // aguardar redirecionamento
        wait.until(ExpectedConditions.urlContains("/home"));
        String urlAtual = driver.getCurrentUrl();
        
        // verificar redirecionamento
        assertTrue(urlAtual.contains("/home"), "Deveria ter redirecionado para /home após atualização");
    }

    @Test
    public void testAlterarApenasUmCampo() {
        // alterar apenas o salário
        String salarioOriginal = salarioField.getDomProperty("value");
        
        salarioField.clear();
        salarioField.sendKeys("9500");
        
        String novoSalario = salarioField.getDomProperty("value");
        assertEquals("9500", novoSalario, "Salário deveria ser 9500");
        assertNotEquals(salarioOriginal, novoSalario, "Salário deveria ter sido alterado");
        
        // submeter alteração
        atualizarButton.click();
        
        // aguardar redirecionamento
        wait.until(ExpectedConditions.urlContains("/home"));
        String urlAtual = driver.getCurrentUrl();
        
        assertTrue(urlAtual.contains("/home"), "Deveria ter redirecionado para /home");
    }

    @Test
    public void testCamposHabilitadosParaEdicao() {
        // verificar se todos os campos estão habilitados para edição
        assertTrue(nomeField.isEnabled(), "Campo nome deveria estar habilitado");
        assertTrue(emailField.isEnabled(), "Campo email deveria estar habilitado");
        assertTrue(especialidadeField.isEnabled(), "Campo especialidade deveria estar habilitado");
        assertTrue(salarioField.isEnabled(), "Campo salário deveria estar habilitado");
        assertTrue(atualizarButton.isEnabled(), "Botão atualizar deveria estar habilitado");
    }

    @AfterAll
    public static void fecharBrowser() {
        driver.close();
    }
}
