package com.iftm.client.resources;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.ClientService;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ClienteResourceTestSemMockBean {

    @Autowired
    private MockMvc mockMVC;

    @Autowired
    private ObjectMapper objectMapper;

    // Injeta a instância real.
    @Autowired
    private ClientService service;

    @Test
    @DisplayName("findAll deveria retornar uma página com todos os clientes")
    public void findAllShouldReturnPageWithAllClients() throws Exception {
        // Arrange 
        int expectedTotalElements = 12;

        // Act
        ResultActions result = mockMVC.perform(get("/clients/").accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.totalElements").value(expectedTotalElements));
        result.andExpect(jsonPath("$.content[0].name").value("Carolina Maria de Jesus")); // Ordenado por nome (ASC)
        result.andExpect(jsonPath("$.content[1].name").value("Chimamanda Adichie"));
    }

    @Test
    @DisplayName("insert deveria criar um novo cliente no banco de dados")
    public void insertShouldCreateNewClientInDatabase() throws Exception {
        // Arrange
        long expectedId = 13L; // O próximo ID da sequência do H2
        ClientDTO newDto = new ClientDTO(null, "Machado de Assis", "11122233344", 6000.0, Instant.parse("1839-06-21T00:00:00Z"), 0);
        String jsonBody = objectMapper.writeValueAsString(newDto);

        // Act
        ResultActions result = mockMVC.perform(post("/clients")
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isCreated());
        result.andExpect(jsonPath("$.id").value(expectedId));
        result.andExpect(jsonPath("$.name").value("Machado de Assis"));
    }

    @Test
    @DisplayName("delete deveria apagar um cliente quando o ID existir")
    public void deleteShouldDeleteClientWhenIdExists() throws Exception {
        // Arrange
        long existingId = 1L;

        // Act
        ResultActions result = mockMVC.perform(delete("/clients/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("delete deveria retornar Not Found quando o ID não existir")
    public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // Arrange
        long nonExistingId = 100L;

        // Act
        ResultActions result = mockMVC.perform(delete("/clients/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("findByIncome deveria retornar clientes com a renda especificada")
    public void findByIncomeShouldReturnClientsWithSpecifiedIncome() throws Exception {
        // Arrange
        double incomeToSearch = 2500.0;
        int expectedCount = 3; 

        // Act
        ResultActions result = mockMVC.perform(get("/clients/income/")
                .param("income", String.valueOf(incomeToSearch))
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.totalElements").value(expectedCount));
        result.andExpect(jsonPath("$.content[?(@.name == 'Gilberto Gil')]").exists());
        result.andExpect(jsonPath("$.content[?(@.name == 'Jorge Amado')]").exists());
        result.andExpect(jsonPath("$.content[?(@.name == 'Lázaro Ramos')]").exists());
    }

    @Test
    @DisplayName("update deveria atualizar os dados do cliente quando o ID existir")
    public void updateShouldUpdateClientDataWhenIdExists() throws Exception {
        // Arrange
        long existingId = 2L; // ID de 'Lázaro Ramos'
        ClientDTO updatedDto = new ClientDTO(existingId, "Lázaro Ramos Atualizado", "10619244881", 9900.0, Instant.now(), 2);
        String jsonBody = objectMapper.writeValueAsString(updatedDto);

        // Act
        ResultActions result = mockMVC.perform(put("/clients/{id}", existingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isOk());
        result.andExpect(jsonPath("$.id").value(existingId));
        result.andExpect(jsonPath("$.name").value("Lázaro Ramos Atualizado"));
        result.andExpect(jsonPath("$.income").value(9900.0));
    }

    @Test
    @DisplayName("update deveria retornar Not Found quando o ID não existir")
    public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
        // Arrange
        long nonExistingId = 100L;
        ClientDTO dto = new ClientDTO(nonExistingId, "Nome Qualquer", "111", 1.0, Instant.now(), 0);
        String jsonBody = objectMapper.writeValueAsString(dto);

        // Act
        ResultActions result = mockMVC.perform(put("/clients/{id}", nonExistingId)
                .content(jsonBody)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        result.andExpect(status().isNotFound());
    }
}