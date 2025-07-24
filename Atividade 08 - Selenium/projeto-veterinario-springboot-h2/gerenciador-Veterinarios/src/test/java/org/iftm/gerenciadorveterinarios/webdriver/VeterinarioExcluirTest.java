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

public class VeterinarioExcluirTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    
    // Elementos comuns a todos os testes
    private WebElement tabela;
    private List<WebElement> botoesExcluir;
    private List<WebElement> linhasTabela;

    @BeforeAll
    public static void configurarDriverBrowser() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @BeforeEach
    public void navegarParaHomeECapturarElementos() {
        driver.get("http://localhost:8080/home");
        
        // aguardar tabela carregar
        tabela = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table.table")));
        
        // capturar botões de excluir (btn-danger) e linhas da tabela com dados
        botoesExcluir = driver.findElements(By.cssSelector(".btn-danger"));
        linhasTabela = driver.findElements(By.xpath("//tr[td]"));
    }

    @Test
    public void testVerificarPresencaBotoesExcluir() {
        // verificar se tabela existe
        assertNotNull(tabela, "Tabela deveria existir");
        
        // verificar se botões de excluir estão presentes (quando há dados)
        if (linhasTabela.size() > 0) {
            assertTrue(botoesExcluir.size() > 0, "Deveria haver botões de excluir quando há dados");
            
            // verificar se botões estão visíveis e habilitados
            for (WebElement botao : botoesExcluir) {
                assertTrue(botao.isDisplayed(), "Botão excluir deveria estar visível");
                assertTrue(botao.isEnabled(), "Botão excluir deveria estar habilitado");
            }
        }
    }

    @Test
    public void testExcluirVeterinario() {
        // só executar se houver dados para excluir
        if (botoesExcluir.size() > 0) {
            int quantidadeInicial = linhasTabela.size();
            
            // clicar no primeiro botão de excluir
            WebElement primeiroBotaoExcluir = botoesExcluir.get(0);
            primeiroBotaoExcluir.click();
            
            // aguardar retorno à página home
            wait.until(ExpectedConditions.urlContains("/home"));
            
            // verificar se ainda está na página home
            String urlAtual = driver.getCurrentUrl();
            assertTrue(urlAtual.contains("/home"), "Deveria estar na página /home após exclusão");
            
            // recarregar elementos
            navegarParaHomeECapturarElementos();
            
            // verificar se número de linhas diminuiu
            int quantidadeFinal = linhasTabela.size();
            assertEquals(quantidadeInicial - 1, quantidadeFinal, 
                        "Número de linhas deveria ter diminuído em 1 após exclusão");
        } else {
            assertTrue(true, "Nenhum veterinário encontrado para testar exclusão");
        }
    }

    @Test
    public void testExcluirMultiplosVeterinarios() {
        // só executar se houver pelo menos 2 registros
        if (botoesExcluir.size() >= 2) {
            int quantidadeInicial = linhasTabela.size();
            
            // excluir dois veterinários
            for (int i = 0; i < 2; i++) {
                navegarParaHomeECapturarElementos();
                
                if (botoesExcluir.size() > 0) {
                    botoesExcluir.get(0).click();
                    wait.until(ExpectedConditions.urlContains("/home"));
                }
            }
            
            // verificar resultado final
            navegarParaHomeECapturarElementos();
            int quantidadeFinal = linhasTabela.size();
            
            assertEquals(quantidadeInicial - 2, quantidadeFinal, 
                        "Deveria ter excluído 2 veterinários");
        } else {
            assertTrue(true, "Dados insuficientes para testar exclusão múltipla");
        }
    }

    @Test
    public void testVerificarBotaoExcluir() {
        if (botoesExcluir.size() > 0) {
            WebElement botaoExcluir = botoesExcluir.get(0);
            
            // verificar se é um link
            String tagName = botaoExcluir.getTagName();
            assertEquals("a", tagName, "Botão excluir deveria ser um elemento 'a'");
            
            // verificar se tem href
            String href = botaoExcluir.getDomAttribute("href");
            assertNotNull(href, "Botão excluir deveria ter atributo href");
            assertTrue(href.length() > 0, "Href não deveria estar vazio");
            
            // verificar classe CSS
            String classes = botaoExcluir.getDomAttribute("class");
            assertTrue(classes.contains("btn-danger"), "Botão deveria ter classe 'btn-danger'");
        } else {
            assertTrue(true, "Nenhum botão de exclusão encontrado para testar");
        }
    }

    @Test
    public void testEstadoTabela() {
        // verificar se tabela existe
        assertNotNull(tabela, "Tabela deveria existir");
        
        // verificar cabeçalho da tabela
        List<WebElement> cabecalhos = tabela.findElements(By.cssSelector("tr.table-dark th"));
        assertTrue(cabecalhos.size() > 0, "Tabela deveria ter cabeçalho");
        
        // verificar estrutura da tabela
        if (linhasTabela.size() > 0) {
            // se há dados, verificar se cada linha tem células
            for (WebElement linha : linhasTabela) {
                List<WebElement> celulas = linha.findElements(By.tagName("td"));
                assertTrue(celulas.size() > 0, "Linha deveria ter células");
            }
        }
    }

    @AfterAll
    public static void fecharBrowser() {
        driver.close();
    }
}
