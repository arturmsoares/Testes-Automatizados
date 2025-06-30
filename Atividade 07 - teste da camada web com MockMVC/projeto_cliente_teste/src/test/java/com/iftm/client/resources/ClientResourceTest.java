package com.iftm.client.resources;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;

import static org.mockito.ArgumentMatchers.any;
//necessário para utilizar o MockMVC
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourceTest {
    @Autowired
    private MockMvc mockMVC;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService service;

    /**
     * Caso de testes : Verificar se o endpoint get/clients/ retorna todos os
     * clientes existentes
     * Arrange:
     * - camada service simulada com mockito
     * - base de dado : 3 clientes
     * new Client(7l, "Jose Saramago", "10239254871", 5000.0,
     * Instant.parse("1996-12-23T07:00:00Z"), 0);
     * new Client(4l, "Carolina Maria de Jesus", "10419244771", 7500.0,
     * Instant.parse("1996-12-23T07:00:00Z"), 0);
     * new Client(8l, "Toni Morrison", "10219344681", 10000.0,
     * Instant.parse("1940-02-23T07:00:00Z"), 0);
     * - Uma PageRequest default
     * 
     * @throws Exception
     */
    @Test
    @DisplayName("Verificar se o endpoint get/clients/ retorna todos os clientes existentes")
    public void testarEndPointListarTodosClientesRetornaCorreto() throws Exception {
        // arrange
        int quantidadeClientes = 3;
        // configurando o Mock ClientService
        List<ClientDTO> listaClientes;
        listaClientes = new ArrayList<ClientDTO>();
        listaClientes.add(new ClientDTO(
                new Client(7L, "Jose Saramago", "10239254871", 5000.0, Instant.parse("1996-12-23T07:00:00Z"), 0)));
        listaClientes.add(new ClientDTO(new Client(4L, "Carolina Maria de Jesus", "10419244771", 7500.0,
                Instant.parse("1996-12-23T07:00:00Z"), 0)));
        listaClientes.add(new ClientDTO(
                new Client(8L, "Toni Morrison", "10219344681", 10000.0, Instant.parse("1940-02-23T07:00:00Z"), 0)));

        Page<ClientDTO> page = new PageImpl<>(listaClientes);

        Mockito.when(service.findAllPaged(Mockito.any())).thenReturn(page);
        // fim configuração mockito

        // act

        ResultActions resultados = mockMVC.perform(get("/clients/").accept(MediaType.APPLICATION_JSON));

        // assign
        resultados
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 7L).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 4L).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]", 8L).exists())
                .andExpect(jsonPath("$.content[?(@.name == '%s')]", "Toni Morrison").exists())
                .andExpect(jsonPath("$.totalElements").exists())
                .andExpect(jsonPath("$.totalElements").value(quantidadeClientes));

    }


    @Test
    @DisplayName("insert deveria retornar 'created' (código 201) usando any() na simulação")
    public void insertShouldReturnCreatedWhenUsingAnyInMock() throws Exception {
        // Arrange
        ClientDTO responseDto = new ClientDTO(13L, "Machado de Assis", "11122233344", 6000.0,
                Instant.parse("1839-06-21T00:00:00Z"), 0);

        // configuração do Mock
        // Quando o método 'insert' do 'service' for chamado com QUALQUER objeto do tipo
        // ClientDTO será retornado o 'responseDto'".
        Mockito.when(service.insert(any(ClientDTO.class))).thenReturn(responseDto);

        ClientDTO requestDto = new ClientDTO(null, "Artur Machado", "11122233344", 6000.0,
                Instant.parse("1839-06-21T00:00:00Z"), 0);
        String jsonBody = objectMapper.writeValueAsString(requestDto); // Serializa o objeto requestDto para JSON

        // Act
        ResultActions result = mockMVC.perform(post("/clients")
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.id").value(13L));
        result.andExpect(jsonPath("$.name").value("Artur Machado"));
    }

    @Test
    @DisplayName("delete deveria retornar 'no content' (código 204) quando o id existir")
    public void deleteQuandoIdExistir() throws Exception {
        // Arrange
        long existingId = 1L;
        Mockito.doNothing().when(service).delete(existingId);

        // Act
        ResultActions result = mockMVC.perform(delete("/clients/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete deveria retornar 'not found' (código 404) quando o id não existir")
    public void deleteQuandoIdNaoExistir() throws Exception {
        // Arrange
        long nonExistingId = 1000L;

        Mockito.doThrow(new ResourceNotFoundException("Id not found"))
               .when(service).delete(nonExistingId);

        // Act
        ResultActions result = mockMVC.perform(delete("/clients/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("findByIncome deveria retornar OK (código 200) e uma página de clientes")
    public void findByIncomeDeveRetornarOkEPaginaDeClientes() throws Exception {
        // Arrange
        double income = 4500.0;

        // Criando a lista de clientes que o service simulado deve retornar
        List<ClientDTO> clientList = new ArrayList<>();
        clientList.add(new ClientDTO(6L, "Djamila Ribeiro", "10619244884", 4500.0, Instant.parse("1975-11-10T07:00:00Z"), 1));
        clientList.add(new ClientDTO(11L, "Silvio Almeida", "10164334861", 4500.0, Instant.parse("1970-09-23T07:00:00Z"), 2));

        // Criando o objeto Page que será o retorno do service
        Page<ClientDTO> page = new PageImpl<>(clientList);

        // Quando o service.findByIncome for chamado com qualquer PageRequest mas com a
        // renda de 4500.0, ele deve retornar a página que criamos.
        Mockito.when(service.findByIncome(Mockito.any(), Mockito.eq(income))).thenReturn(page);

        // Act
        ResultActions result =
                mockMVC.perform(get("/clients/income/")
                        .param("income", String.valueOf(income)) 
                        .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.totalElements").value(2));
        result.andExpect(jsonPath("$.content").isArray());
        result.andExpect(jsonPath("$.content[?(@.name == 'Djamila Ribeiro')]").exists());
        result.andExpect(jsonPath("$.content[?(@.name == 'Silvio Almeida')]").exists());
    }

    @Test
    @DisplayName("update deveria retornar OK (código 200) e o DTO atualizado quando o id existir")
    public void updateComIDExistente() throws Exception {
        // Arrange
        long existingId = 1L;
        ClientDTO updatedDto = new ClientDTO(existingId, "Nome Atualizado", "11122233344", 8000.0, Instant.now(), 3);

        // Quando o service.update for chamado com o ID existente e qualquer DTO,
        // ele deve retornar o nosso DTO atualizado.
        Mockito.when(service.update(Mockito.eq(existingId), Mockito.any(ClientDTO.class)))
               .thenReturn(updatedDto);

               

        // Converte o objeto DTO em uma String JSON para enviar no corpo da requisição
        String jsonBody = objectMapper.writeValueAsString(updatedDto);

        // Act
        ResultActions result =
                mockMVC.perform(put("/clients/{id}", existingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isOk());
        // Verifica se os campos no JSON de resposta foram realmente atualizados
        result.andExpect(jsonPath("$.id").value(existingId));
        result.andExpect(jsonPath("$.name").value("Nome Atualizado"));
        result.andExpect(jsonPath("$.income").value(8000.0));
    }

    @Test
    @DisplayName("update deveria retornar 'not found' (código 404) quando o id não existir")
    public void updateComIdInexistente() throws Exception {
        // Arrange
        long nonExistingId = 1000L;
        ClientDTO requestDto = new ClientDTO(nonExistingId, "Nome Qualquer", "11122233344", 8000.0, Instant.now(), 3);

        Mockito.when(service.update(Mockito.eq(nonExistingId), Mockito.any(ClientDTO.class)))
               .thenThrow(new ResourceNotFoundException("Id not found"));

        String jsonBody = objectMapper.writeValueAsString(requestDto);

        // Act
        ResultActions result =
                mockMVC.perform(put("/clients/{id}", nonExistingId)
                        .content(jsonBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isNotFound());
        // verificação opcional
        result.andExpect(jsonPath("$.error").value("Resource not found"));
    }

}
