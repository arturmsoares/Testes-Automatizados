package org.iftm.gerenciadorveterinarios.webdriver;

import java.time.Duration;
import java.util.List;

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

public class VeterinarioListarTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    
    // Elementos comuns a todos os testes
    private WebElement tabela;
    private WebElement tituloVeterinarios;
    private WebElement botaoAdicionar;
    private WebElement botaoConsultar;

    @BeforeAll
    public static void configurarDriverBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @BeforeEach
    public void navegarParaHome() {
        driver.get("http://localhost:8080/home");
        
        tituloVeterinarios = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table")));
        botaoAdicionar = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Adicionar")));
        botaoConsultar = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Consultar")));
    }

    @Test
    public void testVerificarElementosPaginaHome() {
        // verificar se elementos principais estão visíveis
        assertTrue(tituloVeterinarios.isDisplayed(), "Título deveria estar visível");
        assertTrue(tabela.isDisplayed(), "Tabela deveria estar visível");
        assertTrue(botaoAdicionar.isDisplayed(), "Botão Adicionar deveria estar visível");
        assertTrue(botaoConsultar.isDisplayed(), "Botão Consultar deveria estar visível");
        
        // verificar texto específico do título usando assertEquals
        assertEquals("Veterinarios", tituloVeterinarios.getText(), "Título deveria ser exatamente 'Veterinarios'");
        
        // verificar texto dos botões
        assertEquals("Adicionar", botaoAdicionar.getText(), "Texto do botão deveria ser 'Adicionar'");
        assertEquals("Consultar", botaoConsultar.getText(), "Texto do botão deveria ser 'Consultar'");
    }

    @Test
    public void testVerificarCabecalhoTabela() {
        // verificar cabeçalhos da tabela usando assertEquals para textos específicos
        List<WebElement> cabecalhos = tabela.findElements(By.cssSelector("tr.table-dark th"));
        
        assertTrue(cabecalhos.size() >= 5, "Tabela deveria ter pelo menos 5 colunas");
        
        // verificar textos específicos dos cabeçalhos usando assertEquals
        WebElement colunaNome = tabela.findElement(By.xpath("//th[contains(text(), 'Nome')]"));
        assertEquals("Nome", colunaNome.getText(), "Cabeçalho da segunda coluna deveria ser 'Nome'");
        
        WebElement colunaEspecialidade = tabela.findElement(By.xpath("//th[contains(text(), 'Especialidade')]"));
        assertEquals("Especialidade", colunaEspecialidade.getText(), "Cabeçalho da terceira coluna deveria ser 'Especialidade'");
        
        WebElement colunaEmail = tabela.findElement(By.xpath("//th[contains(text(), 'Email')]"));
        assertEquals("Email", colunaEmail.getText(), "Cabeçalho da quarta coluna deveria ser 'Email'");
        
        WebElement colunaSalario = tabela.findElement(By.xpath("//th[contains(text(), 'Salario')]"));
        assertEquals("Salario", colunaSalario.getText(), "Cabeçalho da quinta coluna deveria ser 'Salario'");
    }

    @Test
    public void testVerificarConteudoTabela() {
        // verificar se existem linhas de dados na tabela
        List<WebElement> linhasDados = tabela.findElements(By.xpath("//tr[td]"));
        
        // se houver dados, verificar conteúdo específico das células
        if (linhasDados.size() > 0) {
            WebElement primeiraLinha = linhasDados.get(0);
            List<WebElement> colunas = primeiraLinha.findElements(By.tagName("td"));
            
            assertTrue(colunas.size() >= 5, "Cada linha deveria ter pelo menos 5 colunas");
            
            // verificar que as células não estão vazias (se há dados)
            if (colunas.size() > 1) {
                String nomeTexto = colunas.get(1).getText(); // coluna Nome
                assertNotNull(nomeTexto, "Nome não deveria ser null");
                
                String especialidadeTexto = colunas.get(2).getText(); // coluna Especialidade  
                assertNotNull(especialidadeTexto, "Especialidade não deveria ser null");
                
                String emailTexto = colunas.get(3).getText(); // coluna Email
                assertNotNull(emailTexto, "Email não deveria ser null");
                
                String salarioTexto = colunas.get(4).getText(); // coluna Salario
                assertNotNull(salarioTexto, "Salario não deveria ser null");
                assertTrue(salarioTexto.startsWith("R$"), "Salário deveria começar com 'R$'");
            }
        } else {
            // se não há dados, isso é válido (tabela vazia)
            assertEquals(0, linhasDados.size(), "Tabela deveria estar vazia quando não há dados");
        }
    }

    @Test
    public void testBotoesAcaoTabela() {
        // verificar botões de ação na tabela
        List<WebElement> botoesEditar = tabela.findElements(By.cssSelector("a.btn-warning"));
        List<WebElement> botoesExcluir = tabela.findElements(By.cssSelector("a.btn-danger"));
        
        // número de botões deve ser consistente
        assertEquals(botoesEditar.size(), botoesExcluir.size(), 
                    "Número de botões editar deve ser igual ao número de botões excluir");
        
        // verificar propriedades dos botões se existirem
        if (botoesEditar.size() > 0) {
            WebElement primeiroEditar = botoesEditar.get(0);
            assertTrue(primeiroEditar.isDisplayed(), "Botão editar deveria estar visível");
            
            String classesEditar = primeiroEditar.getDomAttribute("class");
            assertTrue(classesEditar.contains("btn-warning"), "Botão editar deveria ter classe 'btn-warning'");
            
            WebElement primeiroExcluir = botoesExcluir.get(0);
            assertTrue(primeiroExcluir.isDisplayed(), "Botão excluir deveria estar visível");
            
            String classesExcluir = primeiroExcluir.getDomAttribute("class");
            assertTrue(classesExcluir.contains("btn-danger"), "Botão excluir deveria ter classe 'btn-danger'");
        }
    }

    @Test
    public void testNavegacaoParaFormularioCadastro() {
        // verificar URL inicial
        String urlInicial = driver.getCurrentUrl();
        assertTrue(urlInicial.contains("/home"), "Deveria estar na página /home inicialmente");
        
        // clicar no botão Adicionar
        botaoAdicionar.click();
        
        // aguardar redirecionamento
        wait.until(ExpectedConditions.urlContains("/form"));
        String urlAtual = driver.getCurrentUrl();
        
        // verificar redirecionamento usando assertEquals para URL específica
        assertTrue(urlAtual.contains("/form"), "Deveria ter redirecionado para /form");
        assertEquals("http://localhost:8080/form", urlAtual, "URL deveria ser exatamente http://localhost:8080/form");
    }

    @Test
    public void testNavegacaoParaFormularioPesquisa() {
        // verificar URL inicial
        String urlInicial = driver.getCurrentUrl();
        assertTrue(urlInicial.contains("/home"), "Deveria estar na página /home inicialmente");
        
        // clicar no botão Consultar
        botaoConsultar.click();
        
        // aguardar redirecionamento
        wait.until(ExpectedConditions.urlContains("/find"));
        String urlAtual = driver.getCurrentUrl();
        
        // verificar redirecionamento usando assertEquals para URL específica
        assertTrue(urlAtual.contains("/find"), "Deveria ter redirecionado para /find");
        assertEquals("http://localhost:8080/find", urlAtual, "URL deveria ser exatamente http://localhost:8080/find");
    }

    @AfterAll
    public static void fecharBrowser() {
        driver.close();
    }
}
